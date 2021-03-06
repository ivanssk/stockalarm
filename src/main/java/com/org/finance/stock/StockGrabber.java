package com.org.finance.stock;

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

	static public String getCookie() throws Exception {
		URL obj = new URL(BaseURL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty("Accept-Language", "zh-TW");

		return con.getHeaderField("Set-Cookie");
	}

	static public String getStock(String url, String cookie) throws Exception {
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

		if (response.length() == 0)
			return null;

		return response.toString();
	}

	public void run() {
		try {
			String cookie = getCookie();

			while (true) {
				for (int i = 0; i < _request.length; i++) {
					String message = getStock(_request[i], cookie);

					while (message == null) {
						cookie = getCookie();
						message = getStock(_request[i], cookie);
					}

					_messageQueue.add(message);
					Thread.sleep(3000);
				}
			}
		} catch (Exception e) {
		}
	}
}


