package com.org.finance.stock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.HashMap;
import java.util.ArrayList;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.SGR;

public class Main {
	private static final String [] BUY_RANK = new String [] {"買一", "買二", "買三", "買四", "買五"};
	private static final String [] SELL_RANK = new String [] {"賣一", "賣二", "賣三", "賣四", "賣五"};
	private static int _big_order_index;

	static private void showEstimate(int index, int y, TextGraphics textGraphics,
			int fee1, int fee2, int tax, float diff, int c, float currentPrice, float advicePrice, String title) {
		textGraphics.putString(0, y, "                                                                          ");
		textGraphics.putString(0, y, String.format("%d. %s:", index, title));

		if (currentPrice == advicePrice)
			textGraphics.putString(9, y, String.format("%.2f", advicePrice), SGR.UNDERLINE);
		else
			textGraphics.putString(9, y, String.format("%.2f", advicePrice));

		if (c > 0) {
			textGraphics.setBackgroundColor(TextColor.ANSI.RED);
			if (currentPrice == advicePrice)
				textGraphics.putString(16, y, String.format("收益: %d", c), SGR.UNDERLINE);
			else
				textGraphics.putString(16, y, String.format("收益: %d", c));
			textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
		} else if (c < 0) {
			textGraphics.setBackgroundColor(TextColor.ANSI.GREEN);
			if (currentPrice == advicePrice)
				textGraphics.putString(16, y, String.format("收益: %d", c), SGR.UNDERLINE);
			else
				textGraphics.putString(16, y, String.format("收益: %d", c));
			textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
		} else {
			if (currentPrice== advicePrice)
				textGraphics.putString(16, y, String.format("收益: %d", c), SGR.UNDERLINE);
			else
				textGraphics.putString(16, y, String.format("收益: %d", c));
		}
	}

	static private void showCost(TextGraphics textGraphics, float myPrice, Stock.Instant instantStock, boolean buyMode, boolean sellMode) {
		if (buyMode == true)
			textGraphics.putString(0, 20, String.format("當沖買入: %.2f", myPrice));
		else if (sellMode == true)
			textGraphics.putString(0, 20, String.format("當沖賣出: %.2f", myPrice));

		int fee1 = (int) (myPrice * 1000.0f * 0.001425f);

		for (int i = 0; i < 5; i++) {
			float diff = 0.0f;
			if (buyMode == true)
				diff = (instantStock._buy_price[i] - myPrice) * 1000.0f;
			else if (sellMode == true)
				diff = (myPrice - instantStock._buy_price[i]) * 1000.0f;

			int fee2 = (int) (instantStock._buy_price[i] * 1000.0f * 0.001425f);

			int tax = 0;
			if (buyMode == true)
				tax = (int) (instantStock._sell_price[i] * 1000.0f * 0.003f / 2.0f);
			else if (sellMode == true)
				tax = (int) (myPrice * 1000.0f * 0.003f / 2.0f);

			int c = (int) (diff - (fee1 + fee2 + tax));

			showEstimate(i + 5, 27 + i, textGraphics, fee1, fee2, tax, diff, c,
					instantStock._current_price, instantStock._buy_price[i], "賣出");

			if (buyMode == true)
				diff = (instantStock._sell_price[i] - myPrice) * 1000;
			else if (sellMode == true)
				diff = (myPrice - instantStock._sell_price[i]) * 1000;

			fee2 = (int) (instantStock._sell_price[i] * 1000.0f * 0.001425f);
			c = (int) (diff - (fee1 + fee2 + tax));

			showEstimate(4 - i, 26 - i, textGraphics, fee1, fee2, tax, diff, c,
					instantStock._current_price, instantStock._sell_price[i], "賣出");
		}
	}

	static void showDailyStock(TextGraphics textGraphics, Stock.Daily s, Stock.Instant i, Stock.Instant lastStock) {
		float diff = 0.0f;

		if (lastStock == null)
			diff = i._current_price - s._yesterday_price;
		else
			diff = i._current_price - lastStock._current_price;

		textGraphics.putString(0, 0, "時間: " + i._time_stamp2);
		textGraphics.putString(25, 0, "股票代號: " + s._id + "(" + s._name + ")");
		textGraphics.putString(0, 1, "======================================================================");
		textGraphics.putString(0, 2, String.format("開盤價: %.2f 昨收: %.2f", s._open_price, s._yesterday_price));
		textGraphics.putString(0, 3, String.format("最高價: %.2f 最低價: %.2f", s._highest_price, s._lowest_price));

		float d2 = i._current_price - s._yesterday_price;
		textGraphics.putString(0, 4, "                               ");

		String msg = null;

		if (d2 > 0) {
			textGraphics.setBackgroundColor(TextColor.ANSI.RED);
			msg = String.format("漲跌: +%.2f", d2);
		} else if (d2 < 0) {
			textGraphics.setBackgroundColor(TextColor.ANSI.GREEN);
			msg = String.format("漲跌: %.2f", d2);;
		} else
			msg = String.format("漲跌: %.2f", d2);

		textGraphics.putString(0, 4, msg);
		textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);

		textGraphics.putString(0, 5, "                                    ");

		if (diff > 0) {
			textGraphics.setBackgroundColor(TextColor.ANSI.RED);
			msg = String.format("現價: %.2f (+%.2f)", i._current_price, diff);
		} else if (diff < 0) {
			textGraphics.setBackgroundColor(TextColor.ANSI.GREEN);
			msg = String.format("現價: %.2f (%.2f)", i._current_price, diff);
		} else
			msg = String.format("現價: %.2f (+%.2f)", i._current_price, diff);

		textGraphics.putString(0, 5, msg);
		textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);

		textGraphics.putString(0, 6, String.format("單量: %d 總量: %d", i._temporal_volume, i._volume));
	}

	private static void showLeading5NicePrice(TextGraphics textGraphics, Stock.Instant i) {
		for (int p = 0; p < 5; p++) {
			textGraphics.putString(33, 2 + p, "                                                 ");
			textGraphics.putString(52, 2 + p, "                                                 ");

			if (i._current_price == i._buy_price[p]) {
				textGraphics.putString(33, 2 + p, String.format("%s: %.2f(%d)", BUY_RANK[p], i._buy_price[p], i._buy_volume[p]),
						SGR.UNDERLINE);
				textGraphics.putString(52, 2 + p, String.format("%s: %.2f(%d)", SELL_RANK[p], i._sell_price[p], i._sell_volume[p]));
			} else if (i._current_price == i._sell_price[p]) {
				textGraphics.putString(33, 2 + p, String.format("%s: %.2f(%d)", BUY_RANK[p], i._buy_price[p], i._buy_volume[p]));
				textGraphics.putString(52, 2 + p, String.format("%s: %.2f(%d)", SELL_RANK[p], i._sell_price[p], i._sell_volume[p]),
						SGR.UNDERLINE);
			} else {
				textGraphics.putString(33, 2 + p, String.format("%s: %.2f(%d)", BUY_RANK[p], i._buy_price[p], i._buy_volume[p]));
				textGraphics.putString(52, 2 + p, String.format("%s: %.2f(%d)", SELL_RANK[p], i._sell_price[p], i._sell_volume[p]));
			}
		}
	}

	static private void showBigOrder(TextGraphics textGraphics, Stock.Daily s, Stock.Instant i, Stock.Instant lastStock, int amountOfOrder) {
		if (lastStock != null && i._time_stamp == lastStock._time_stamp && i._volume == lastStock._volume)
			return;

		if (i._temporal_volume < amountOfOrder)
			return;

		textGraphics.putString(0, 9 + _big_order_index, "                                                                   ");

		if (_big_order_index - 1 < 0)
			textGraphics.putString(0, 9 + 9, "    ");
		else
			textGraphics.putString(0, 9 + _big_order_index - 1, "    ");

		textGraphics.putString(0, 9 + _big_order_index,
				String.format(">>> %d) %s: 大單: %d", _big_order_index, i._time_stamp2, i._temporal_volume));

		float diff = 0.0f;

		if (lastStock == null)
			diff = i._current_price - s._yesterday_price;
		else
			diff = i._current_price - lastStock._current_price;

		String msg = null;

		if (diff > 0) {
			textGraphics.setBackgroundColor(TextColor.ANSI.RED);
			msg = String.format("成交價: %.2f 漲: %.2f", i._current_price, diff);
		} else if (diff < 0) {
			textGraphics.setBackgroundColor(TextColor.ANSI.GREEN);
			msg = String.format("成交價: %.2f 跌: %.2f", i._current_price, diff);
		} else
			msg = String.format("成交價: %.2f 平盤    ", i._current_price);

		textGraphics.putString(33, 9 + _big_order_index, msg);
		textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);

		_big_order_index = (_big_order_index + 1) % 10;
	}

	static public void main(String [] args) {
		try {
			Screen screen = new DefaultTerminalFactory().createScreen();
			screen.startScreen();
			TextGraphics textGraphics = screen.newTextGraphics();

			ArrayBlockingQueue<String> messageQueue = new ArrayBlockingQueue<String>(args.length * 100 * 100);
			HashMap<String, Stock.Daily> stockContainer = new HashMap<String, Stock.Daily>();

			ExecutorService executor = Executors.newFixedThreadPool(args.length + 1);
			executor.execute(new MessageParser(messageQueue, stockContainer));

			String request = String.format("http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_%s.tw&json=1&delay=0&_=%d",
					args[0], System.currentTimeMillis());

			executor.execute(new StockGrabber(messageQueue, request));
			Thread.sleep(150);

			ArrayList <Stock.Instant> instant_stocks = new ArrayList <Stock.Instant>();
			Stock.Instant lastStock = null;
			Keyboard keyboard = new Keyboard();
			KeyFunctionHandler keyFunctionHandler = new KeyFunctionHandler();

			while (keyboard.handle(screen, textGraphics, keyFunctionHandler) == true) {
				synchronized (stockContainer) {
					Stock.Daily [] dailyStocks = stockContainer.values().toArray(new Stock.Daily[0]);

					for (Stock.Daily s: dailyStocks) {
						for (Stock.Instant i: s._instant_stocks) {
							showDailyStock(textGraphics, s, i, lastStock);
							showLeading5NicePrice(textGraphics, i);

							Float myPrice = keyFunctionHandler.getMyPrice();

							if (myPrice != null) {
								showCost(textGraphics, myPrice, i,
										keyFunctionHandler.isBuyMode(), keyFunctionHandler.isSellMode());
							}

							showBigOrder(textGraphics, s, i, lastStock, 10);

							if (lastStock == null || lastStock._time_stamp != i._time_stamp)
								lastStock = i;
						}

						s._instant_stocks.clear();
					}

					screen.refresh();
				}

				Thread.sleep(10);
			};

			executor.shutdown();
			executor.shutdownNow();
			screen.stopScreen();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
