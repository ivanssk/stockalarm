package com.org.finance.stock;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONArray;

public class MessageParser implements Runnable {
	private ArrayBlockingQueue<String> _messageQueue;
	private HashMap<String, Stock.Daily> _stockContainer; 
	public MessageParser(ArrayBlockingQueue<String> messageQueue, HashMap<String, Stock.Daily> stockContainer) {
		_messageQueue = messageQueue;
		_stockContainer = stockContainer;
	}

	private void parse(String message, HashMap<String, Stock.Daily> stockContainer) {
		JSONObject jsonObject = new JSONObject(message);
		JSONArray msgArray = (JSONArray) jsonObject.get("msgArray");
		int msgLength = msgArray.length();

		for (int i = 0; i < msgLength; i++) {
			JSONObject msg = (JSONObject) msgArray.get(i);
			Stock.Instant instantStock = new Stock.Instant();

			instantStock._time_stamp = Long.valueOf(msg.get("tlong").toString());
			instantStock._time_stamp2 = msg.get("t").toString();
			instantStock._current_price = msg.has("z") ? Float.valueOf(msg.get("z").toString()) : 0;
			instantStock._temporal_volume = msg.has("tv") ? Integer.valueOf(msg.get("tv").toString()) : 0;
			instantStock._volume = msg.has("v") ? Integer.valueOf(msg.get("v").toString()) : 0;

			String [] s = msg.get("a").toString().split("_");
			String [] sv = msg.get("f").toString().split("_");

			for (int j = 0; j < s.length; j++) {
				if (s[j].equals("-") == false)
					instantStock._sell_price[j] = Float.valueOf(s[j]);

				if (sv[j].equals("-") == false)
					instantStock._sell_volume[j] = Integer.valueOf(sv[j]);
			}

			String [] b = msg.get("b").toString().split("_");
			String [] bv = msg.get("g").toString().split("_");

			for (int j = 0; j < b.length; j++) {
				if (b[j].equals("-") == false)
					instantStock._buy_price[j] = Float.valueOf(b[j]);

				if (bv[j].equals("-") == false)
					instantStock._buy_volume[j] = Integer.valueOf(bv[j]);
			}

			synchronized (_stockContainer) {
				String stock_id = msg.get("c").toString();
				Stock.Daily dailyStock = stockContainer.get(stock_id);

				if (dailyStock == null) {
					dailyStock = new Stock.Daily();
					stockContainer.put(stock_id, dailyStock);
				}

				dailyStock._id = stock_id;
				dailyStock._name = msg.get("n").toString();
				dailyStock._today = msg.get("d").toString();
				dailyStock._highest_price = msg.has("h") == true ? Float.valueOf(msg.get("h").toString()) : 0;
				dailyStock._lowest_price = msg.has("l") == true ? Float.valueOf(msg.get("l").toString()) : 0.0f;
				dailyStock._open_price = msg.has("o") == true ? Float.valueOf(msg.get("o").toString()) : 0;
				dailyStock._up_stop_price = msg.has("u") == true ? Float.valueOf(msg.get("u").toString()) : 0;
				dailyStock._down_stop_price = msg.has("w") == true ? Float.valueOf(msg.get("w").toString()) : 0;
				dailyStock._yesterday_price = msg.has("y") == true ? Float.valueOf(msg.get("y").toString()) : 0;

				dailyStock.add(instantStock);
			}
		}
	}

	public void run() {
		try {
			while (true) {
				String message = _messageQueue.poll(10L, TimeUnit.MILLISECONDS); 

				if (message == null)
					continue;

				parse(message, _stockContainer);
			}
		} catch (Exception e) {
		}
	}
}
