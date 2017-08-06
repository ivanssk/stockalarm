import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class Main {
	static private String composeRequest(BufferedReader br) throws Exception {
		StringBuilder url = new StringBuilder("http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=");
		String line = null;

		for (int i = 0; i < 100 && (line = br.readLine()) != null; i++)
			url.append("tse_" + line + ".tw|");

		return url.substring(0, url.length() - 1) + "&json=1&delay=0&_=" + System.currentTimeMillis();
	}

	static public void main(String [] args) {
		try {
			ExecutorService executor = Executors.newFixedThreadPool(7);
			BufferedReader br = new BufferedReader(new FileReader("上市.txt"));

			ArrayBlockingQueue<String> messageQueue = new ArrayBlockingQueue<String>(6 * 100 * 100);
			HashMap<String, Stock.Daily> stockContainer = new HashMap<String, Stock.Daily>();

			executor.execute(new MessageParser(messageQueue, stockContainer));

			String [] requests = new String[4];

			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 4; j++)
					requests[j] = composeRequest(br);

				executor.execute(new StockGrabber(messageQueue, requests));
				Thread.sleep(150);
			}

			/*
			while (true) {
				Stock.Daily [] dailyStocks = null;

				synchronized (stockContainer) {
					dailyStocks = stockContainer.values().toArray(new Stock.Daily [0]);
				}

				for (Stock.Daily s: dailyStocks) {
					System.out.println("日期: " + s._today);
					System.out.println("代碼: " + s._id);
					System.out.println("今日最高價: " + s._highest_price);
					System.out.println("今日最低價: " + s._lowest_price);
					System.out.println("開盤價: " + s._open_price);
					System.out.println("漲停點: " + s._up_stop_price);
					System.out.println("跌停點: " + s._down_stop_price);
					System.out.println("昨收: " + s._yesterday_price);

					System.out.print("現價: ");

					for (Stock.Instant i: s._instant_stocks) {
						System.out.print(i._current_price + "(" + i._time_stamp + ") ");
					}

					System.out.println("");
				}

				Thread.sleep(100);
			}
			*/

			/*
			Thread.sleep(Long.MAX_VALUE);
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			*/
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
