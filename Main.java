import java.io.BufferedReader;
import java.io.FileReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class Main {
	static class StockDayInfo {
		public String _today;
		public float _highest_price;
		public float _lowest_price;
		public float _open_price;
		public float _up_stop_price;
		public float _down_stop_price;
		public float _yesterday_price;
		public ArrayList<StockImmediateInfo> _stocks = new ArrayList<StockImmediateInfo>();

		public void add(StockImmediateInfo stockImmediateInfo) {
			synchronized (_stocks) {
				_stocks.add(stockImmediateInfo);
			}
		}

		public StockImmediateInfo get() {
			synchronized (_stocks) {
			}
			return null;
		}
	}

	static class StockImmediateInfo {
		public long _time_stamp;
		public float _current_price;
		public int _temporal_volume;
		public int _volume;
		public float [] _sell_price = new float[5];
		public int [] _sell_volume = new int[5];
		public float [] _buy_price = new float[5];
		public int [] _buy_volume = new int[5];
	}

	static class StockCollectionWorker implements Runnable {
		private String _request;
		private HashMap<String, StockDayInfo> _stockContainer;

		public StockCollectionWorker(String request, HashMap<String, StockDayInfo> stockContainer) {
			_request = request;
			_stockContainer = stockContainer;
		}

		private String getCookie(String url) throws Exception {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("GET");
			con.setRequestProperty("Accept-Language", "zh-TW");

			return con.getHeaderField("Set-Cookie");
		}

		private boolean getStock(String url, String cookie) throws Exception {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("GET");
			con.setRequestProperty("Accept-Language", "zh-TW");
			con.setRequestProperty("Cookie", cookie);

			int responseCode = con.getResponseCode();

			if (responseCode != 200) {
				System.out.println("ResponseCode: " + responseCode);
				return false;
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuffer response = new StringBuffer();

			for (String inputLine = null; (inputLine = in.readLine()) != null;)
				response.append(inputLine);

			in.close();

			if (response.length() == 0) {
				System.out.println("no data");
				return false;
			}

			JSONObject json = new JSONObject(response.toString());
			JSONArray msgArray = (JSONArray) json.get("msgArray");

			for (int i = 0; i < msgArray.length(); i++) {
				JSONObject stock = (JSONObject) msgArray.get(i);
				StockImmediateInfo stockImmediateInfo = new StockImmediateInfo();

				stockImmediateInfo._time_stamp = Long.valueOf(stock.get("tlong").toString());
				stockImmediateInfo._current_price = stock.has("z") ? Float.valueOf(stock.get("z").toString()) : 0;
				stockImmediateInfo._temporal_volume = stock.has("tv") ? Integer.valueOf(stock.get("tv").toString()) : 0;
				stockImmediateInfo._volume = stock.has("v") ? Integer.valueOf(stock.get("v").toString()) : 0;

				String [] s = stock.get("a").toString().split("_");
				String [] sv = stock.get("f").toString().split("_");

				for (int j = 0; j < s.length; j++) {
					if (s[j].equals("-") == false)
						stockImmediateInfo._sell_price[j] = Float.valueOf(s[j]);

					if (sv[j].equals("-") == false)
						stockImmediateInfo._sell_volume[j] = Integer.valueOf(sv[j]);
				}

				String [] b = stock.get("b").toString().split("_");
				String [] bv = stock.get("g").toString().split("_");

				for (int j = 0; j < b.length; j++) {
					if (b[j].equals("-") == false)
						stockImmediateInfo._buy_price[j] = Float.valueOf(b[j]);

					if (bv[j].equals("-") == false)
						stockImmediateInfo._buy_volume[j] = Integer.valueOf(bv[j]);
				}

				synchronized (_stockContainer) {
					String stock_no = stock.get("c").toString();
					StockDayInfo stockDayInfo = _stockContainer.get(stock_no);

					if (stockDayInfo == null) {
						stockDayInfo = new StockDayInfo();
						_stockContainer.put(stock_no, stockDayInfo);
					}

					stockDayInfo._today = stock.get("d").toString();
					stockDayInfo._highest_price = stock.has("h") == true ? Float.valueOf(stock.get("h").toString()) : 0;
					stockDayInfo._lowest_price = stock.has("l") == true ? Float.valueOf(stock.get("l").toString()) : 0.0f;
					stockDayInfo._open_price = stock.has("o") == true ? Float.valueOf(stock.get("o").toString()) : 0;
					stockDayInfo._up_stop_price = stock.has("u") == true ? Float.valueOf(stock.get("u").toString()) : 0;
					stockDayInfo._down_stop_price = stock.has("w") == true ? Float.valueOf(stock.get("w").toString()) : 0;
					stockDayInfo._yesterday_price = stock.has("y") == true ? Float.valueOf(stock.get("y").toString()) : 0;
					stockDayInfo._stocks.add(stockImmediateInfo);

					System.out.println(stock_no + ": " + stockImmediateInfo._current_price);
				}
			}

			return true;
		}

		public void run() {
			try {
				String baseURL = "http://mis.twse.com.tw/stock/index.jsp";
				String cookie = getCookie(baseURL);

				while (true) {
					if (getStock(_request, cookie) == false) {
						cookie = getCookie(baseURL);
						continue;
					}

					Thread.sleep(5 * 1000);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	static private String composeRequest(BufferedReader br) throws Exception {
		StringBuilder url = new StringBuilder("http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=");
		String line = null;

		for (int i = 0; i < 100 && (line = br.readLine()) != null; i++)
			url.append("tse_" + line + ".tw|");

		return url.substring(0, url.length() - 1) + "&json=1&delay=0&_=" + System.currentTimeMillis();
	}

	static public void main(String [] args) {
		try {
			ExecutorService executor = Executors.newFixedThreadPool(6);
			BufferedReader br = new BufferedReader(new FileReader("上市.txt"));
			HashMap<String, StockDayInfo> stockContainer = new HashMap<String, StockDayInfo>();

			for (int i = 0; i < 6; i++) {
				String request = composeRequest(br);
				executor.execute(new StockCollectionWorker(request, stockContainer));
			}

			Thread.sleep(Long.MAX_VALUE);

			executor.shutdown();
			while (!executor.isTerminated()) {
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
