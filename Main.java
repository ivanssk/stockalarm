import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.lang.Math;

import org.json.JSONObject;
import org.json.JSONArray;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;

public class Main {
	private static final String [] BUY_LEVEL = new String [] {"買一", "買二", "買三", "買四", "買五"};
	private static final String [] SELL_LEVEL = new String [] {"賣一", "賣二", "賣三", "賣四", "賣五"};
	private static String _text_input = null;
	private static boolean _buy_mode;
	private static boolean _sell_mode;
	private static Float _my_stock_price = null;

	static private void showCost(TextGraphics textGraphics, float myPrice, Stock.Instant instantStock) {
		if (_buy_mode == true) {
			textGraphics.putString(0, 20, String.format("當沖買入價格: %.2f", myPrice));
			float buy_fee = myPrice * 1000.0f * 0.001425f;

			for (int i = 0; i < 5; i++) {
				float diff1 = (instantStock._buy_price[i] - myPrice) * 1000;
				float sell_fee1 = instantStock._buy_price[i] * 1000.0f * 0.001425f;
				float tax1 = instantStock._buy_price[i] * 1000.0f * 0.003f;
				float c = diff1 - (buy_fee + sell_fee1 + tax1);

				textGraphics.putString(0, 27 + i, "                                                                          ");
				textGraphics.putString(0, 27 + i, String.format("%d. 賣出價格: %.2f 收益:", i + 5, instantStock._buy_price[i]));

				if (c > 0) {
					textGraphics.setBackgroundColor(TextColor.ANSI.RED);
					textGraphics.putString(26, 27 + i, String.format("%.2f", c));
					textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
				} else if (c < 0) {
					textGraphics.setBackgroundColor(TextColor.ANSI.GREEN);
					textGraphics.putString(26, 27 + i, String.format("%.2f", c));
					textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
				} else
					textGraphics.putString(26, 27 + i, String.format("%.2f", c));

				float diff2 = (instantStock._sell_price[i] - myPrice) * 1000;
				float sell_fee2 = instantStock._sell_price[i] * 1000.0f * 0.001425f;
				float tax2 = instantStock._sell_price[i] * 1000.0f * 0.003f;
				c = diff2 - (buy_fee + sell_fee2 + tax2);

				textGraphics.putString(0, 26 - i, "                                                                          ");
				textGraphics.putString(0, 26 - i, String.format("%d. 賣出價格: %.2f 收益:", 4 - i, instantStock._sell_price[i]));

				if (c > 0) {
					textGraphics.setBackgroundColor(TextColor.ANSI.RED);
					textGraphics.putString(26, 26 - i, String.format("%.2f", c));
					textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
				} else if (c < 0) {
					textGraphics.setBackgroundColor(TextColor.ANSI.GREEN);
					textGraphics.putString(26, 26 - i, String.format("%.2f", c));
					textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
				} else
					textGraphics.putString(26, 26 - i, String.format("%.2f", c));
			}
		} else if (_sell_mode == true) {
			textGraphics.putString(0, 20, String.format("當沖賣出價格: %.2f", myPrice));
			float sell_fee = myPrice * 1000.0f * 0.001425f;

			for (int i = 0; i < 5; i++) {
				float diff1 = (myPrice - instantStock._buy_price[i]) * 1000;
				float buy_fee1 = instantStock._buy_price[i] * 1000.0f * 0.001425f;
				float tax1 = instantStock._buy_price[i] * 1000.0f * 0.003f;
				float c = diff1 - (sell_fee + buy_fee1 + tax1);

				textGraphics.putString(0, 27 + i, "                                                                        ");
				textGraphics.putString(0, 27 + i, String.format("%d. 買入價格: %.2f 收益:", i + 5, instantStock._buy_price[i]));

				if (c > 0) {
					textGraphics.setBackgroundColor(TextColor.ANSI.RED);
					textGraphics.putString(26, 27 + i, String.format("%.2f", c));
					textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
				} else if (c < 0) {
					textGraphics.setBackgroundColor(TextColor.ANSI.GREEN);
					textGraphics.putString(26, 27 + i, String.format("%.2f", c));
					textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
				} else
					textGraphics.putString(26, 27 + i, String.format("%.2f", c));

				float diff2 = (myPrice - instantStock._sell_price[i]) * 1000;
				float buy_fee2 = instantStock._sell_price[i] * 1000.0f * 0.001425f;
				float tax2 = instantStock._sell_price[i] * 1000.0f * 0.003f;
				c = diff2 - (sell_fee + buy_fee2 + tax2);

				textGraphics.putString(0, 26 - i, "                                                                        ");
				textGraphics.putString(0, 26 - i, String.format("%d. 買入價格: %.2f 收益:", 4 - i, instantStock._sell_price[i]));

				if (c > 0) {
					textGraphics.setBackgroundColor(TextColor.ANSI.RED);
					textGraphics.putString(26, 26 - i, String.format("%.2f", c));
					textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
				} else if (c < 0) {
					textGraphics.setBackgroundColor(TextColor.ANSI.GREEN);
					textGraphics.putString(26, 26 - i, String.format("%.2f", c));
					textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
				} else
					textGraphics.putString(26, 26 - i, String.format("%.2f", c));
			}
		}
	}

	static public void main(String [] args) {
		try {
			Screen screen = new DefaultTerminalFactory().createScreen();
			screen.startScreen();
			TextGraphics textGraphics = screen.newTextGraphics();

			ExecutorService executor = Executors.newFixedThreadPool(args.length + 1);

			ArrayBlockingQueue<String> messageQueue = new ArrayBlockingQueue<String>(args.length * 100 * 100);
			HashMap<String, Stock.Daily> stockContainer = new HashMap<String, Stock.Daily>();

			executor.execute(new MessageParser(messageQueue, stockContainer));

			String request = "http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_" + args[0] + ".tw&json=1&delay=0&_=" + System.currentTimeMillis();
			executor.execute(new StockGrabber(messageQueue, request));
			Thread.sleep(150);

			ArrayList <Stock.Instant> instant_stocks = new ArrayList <Stock.Instant>();
			Stock.Instant last_stock = null;

			int big_c = 0;
			Float buy_cost = null;
			KeyInputHandler keyInputHandler = new KeyInputHandler();

			while (true) {
				synchronized (stockContainer) {
					Stock.Daily [] dailyStocks = stockContainer.values().toArray(new Stock.Daily[0]);

					for (Stock.Daily s: dailyStocks) {
						for (Stock.Instant i: s._instant_stocks) {
							float diff = 0.0f;

							if (last_stock == null)
								diff = i._current_price - s._yesterday_price;
							else
								diff = i._current_price - last_stock._current_price;

							textGraphics.putString(0, 0, "時間: " + i._time_stamp2);
							textGraphics.putString(25, 0, "股票代號: " + s._id + "(" + s._name + ")");
							textGraphics.putString(0, 1, "======================================================================");
							textGraphics.putString(0, 2, String.format("開盤價: %.2f 昨收: %.2f",
										s._open_price, s._yesterday_price));
							textGraphics.putString(0, 3, String.format("最高價: %.2f 最低價: %.2f",
										s._highest_price, s._lowest_price));

							float d2 = i._current_price - s._yesterday_price;

							if (d2 > 0) {
								textGraphics.setBackgroundColor(TextColor.ANSI.RED);
								textGraphics.putString(0, 4, String.format("漲跌: +%.2f", d2));
								textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
							} else if (d2 < 0) {
								textGraphics.setBackgroundColor(TextColor.ANSI.GREEN);
								textGraphics.putString(0, 4, String.format("漲跌: %.2f", d2));
								textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
							} else {
								textGraphics.putString(0, 4, String.format("漲跌: %.2f", d2));
							}

							textGraphics.putString(0, 5, "                                    ");

							if (diff > 0) {
								textGraphics.setBackgroundColor(TextColor.ANSI.RED);
								String msg = String.format("現價: %.2f (+%.2f)", i._current_price, diff);
								textGraphics.putString(0, 5, msg);
								textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
							} else if (diff < 0) {
								textGraphics.setBackgroundColor(TextColor.ANSI.GREEN);
								String msg = String.format("現價: %.2f (%.2f)", i._current_price, diff);
								textGraphics.putString(0, 5, msg);
								textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
							} else {
								String msg = String.format("現價: %.2f (+%.2f)", i._current_price, diff);
								textGraphics.putString(0, 5, msg);
							}

							textGraphics.putString(0, 6, String.format("單量: %d 總量: %d", i._temporal_volume,
										i._volume));

							if (_my_stock_price != null)
								showCost(textGraphics, _my_stock_price, i);

							if (i._current_price == i._buy_price[0]) {
								textGraphics.putString(33, 2, "買一: ");
								textGraphics.putString(39, 2,
										String.format("%.2f(%d)", i._buy_price[0], i._buy_volume[0]),
										SGR.UNDERLINE);

								textGraphics.putString(52, 2,
										String.format("賣一: %.2f(%d)",
											i._sell_price[0], i._sell_volume[0]));
							} else if (i._current_price == i._sell_price[0]) {
								textGraphics.putString(33, 2,
										String.format("買一: %.2f(%d)", i._buy_price[0], i._buy_volume[0]));

								textGraphics.putString(52, 2, "賣一: ");
								textGraphics.putString(57, 2,
										String.format("%.2f(%d)", i._sell_price[0], i._sell_volume[0]),
										SGR.UNDERLINE);
							} else {
								textGraphics.putString(33, 2,
										String.format("買一: %.2f(%d)", i._buy_price[0], i._buy_volume[0]));
								textGraphics.putString(52, 2,
										String.format("賣一: %.2f(%d)",
											i._sell_price[0], i._sell_volume[0]));
							}

							for (int p = 1; p < 5; p++) {
								textGraphics.putString(33, 2 + p, String.format("%s: %.2f(%d)",
											BUY_LEVEL[p],
											i._buy_price[p], i._buy_volume[p]));

								textGraphics.putString(52, 2 + p, String.format("%s: %.2f(%d)",
											SELL_LEVEL[p],
											i._sell_price[p], i._sell_volume[p]));
							}

							if (i._temporal_volume >= 10) {
								textGraphics.putString(0, 9 + big_c,
										"                                                                   ");

								if (big_c - 1 < 0)
									textGraphics.putString(0, 9 + 9, "    ");
								else
									textGraphics.putString(0, 9 + big_c - 1, "    ");

								textGraphics.putString(0, 9 + big_c, ">>> " + big_c + ") " +
										i._time_stamp2 + ": 大單: " + i._temporal_volume);
								
								if (diff > 0) {
									textGraphics.setBackgroundColor(TextColor.ANSI.RED);
									String msg = String.format("成交價: %.2f 漲: %.2f", i._current_price, diff);
									textGraphics.putString(33, 9 + big_c, msg);
									textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
								} else if (diff < 0) {
									textGraphics.setBackgroundColor(TextColor.ANSI.GREEN);
									String msg = String.format("成交價: %.2f 跌: %.2f", i._current_price, diff);
									textGraphics.putString(33, 9 + big_c, msg);
									textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
								} else {
									String msg = String.format("成交價: %.2f 平盤    ", i._current_price);
									textGraphics.putString(33, 9 + big_c, msg);
								}

								big_c = (big_c + 1) % 10;
							}

							if (last_stock == null || last_stock._time_stamp2.equals(i._time_stamp2) == false)
								last_stock = i;
						}

						s._instant_stocks.clear();
					}
					screen.refresh();
				}

				for (int i = 0; i < 50; i++) {
					boolean should_continue = keyInputHandler.handle(screen, textGraphics, new KeyInputHandler.IListener() {
						public void onStartTextInputModeEnter(Screen screen, TextGraphics textGraphics,
								boolean buyMode, boolean sellMode) throws Exception {
							_text_input = new String();

							if (buyMode == true)
								textGraphics.putString(0, 21, "當沖買入: ");
							else
								textGraphics.putString(0, 21, "當沖賣出: ");

							screen.setCursorPosition(new TerminalPosition(10, 21));
							screen.refresh();

							_buy_mode = buyMode;
							_sell_mode = sellMode;
						}

						public void onLeaveTextInputModeEnter(Screen screen,
								TextGraphics textGraphics, boolean isFinished) throws Exception {
							if (isFinished == true)
								_my_stock_price = Float.valueOf(_text_input);
							else {
								_my_stock_price = null;
								_text_input = null;
							}

							screen.setCursorPosition(new TerminalPosition(0, 0));
							textGraphics.putString(0, 21, "                    ");
							screen.refresh();
						}

						public void onCharacter(Screen screen, TextGraphics textGraphics, Character c) throws Exception {
							if (c != '.' && Character.isDigit(c) == false)
								return;

							_text_input += c;
							screen.setCursorPosition(new TerminalPosition(10 + _text_input.length(), 21));

							if (_buy_mode == true)
								textGraphics.putString(0, 21, "當沖買入: " + _text_input);
							else if (_sell_mode == true)
								textGraphics.putString(0, 21, "當沖賣出: " + _text_input);

							screen.refresh();
						}

						public void onBackspace(Screen screen, TextGraphics textGraphics) throws Exception {
							if (_text_input.length() > 0) {
								textGraphics.putString(0, 21, "                    ");

								_text_input = _text_input.substring(0, _text_input.length() - 1);
								screen.setCursorPosition(new TerminalPosition(10 + _text_input.length(), 21));

								if (_buy_mode == true)
									textGraphics.putString(0, 21, "當沖買入: " + _text_input);
								else if (_sell_mode == true)
									textGraphics.putString(0, 21, "當沖賣出: " + _text_input);

								screen.refresh();
							}
						}
					});

					if (should_continue == false) {
						screen.stopScreen();

						executor.shutdown();
						executor.shutdownNow();

						System.out.println("bye");
						return;
					}

					Thread.sleep(10);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
