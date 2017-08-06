import java.util.concurrent.ArrayBlockingQueue;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class StockGrabber implements Runnable {
	private static final String BaseURL = "http://mis.twse.com.tw/stock/index.jsp";

	private String [] _request;
	private ArrayBlockingQueue<String> _messageQueue;

	public StockGrabber(ArrayBlockingQueue<String> messageQueue, String ...request) {
		_request = request;
		_messageQueue = messageQueue;
	}

	private String getCookie() throws Exception {
		URL obj = new URL(BaseURL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty("Accept-Language", "zh-TW");

		return con.getHeaderField("Set-Cookie");
	}

	private String getStock(String url, String cookie) throws Exception {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty("Accept-Language", "zh-TW");
		con.setRequestProperty("Cookie", cookie);

		int responseCode = con.getResponseCode();

		if (responseCode != 200) {
			System.out.println("ResponseCode: " + responseCode);
			return null;
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		StringBuffer response = new StringBuffer();

		for (String inputLine = null; (inputLine = in.readLine()) != null;)
			response.append(inputLine);

		in.close();

		if (response.length() == 0) {
			System.out.println("no data");
			return null;
		}

		return response.toString();
	}

	public void run() {
		try {
			String cookie = getCookie();

			while (true) {
				for (int i = 0; i < _request.length; i++) {
					long time1 = System.currentTimeMillis();
					String message = getStock(_request[i], cookie);
					long time2 = System.currentTimeMillis();

					while (message == null) {
						time1 = System.currentTimeMillis();
						cookie = getCookie();
						message = getStock(_request[i], cookie);
						time2 = System.currentTimeMillis();
					}

					System.out.println(Thread.currentThread().getId() + ": good - " + i + "(" + ((float) (time2 - time1) / 1000) + ")");
					_messageQueue.add(message);
				}
			}
		} catch (Exception e) {
		}
	}
}


