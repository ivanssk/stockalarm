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
import com.googlecode.lanterna.TerminalPosition;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.SGR;

public class Main {
	private static final String [] BUY_LEVEL = new String [] {"買一", "買二", "買三", "買四", "買五"};
	private static final String [] SELL_LEVEL = new String [] {"賣一", "賣二", "賣三", "賣四", "賣五"};

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

							textGraphics.putString(0, 6, "單量: " + i._temporal_volume);
							textGraphics.putString(0, 7, "累積量: " + i._volume);

							if (i._current_price == i._buy_price[0]) {
								textGraphics.putString(33, 2, "買一: ");
								textGraphics.putString(39, 2,
										String.format("%.2f(%3d)", i._buy_price[0], i._buy_volume[0]),
										SGR.UNDERLINE);

								textGraphics.putString(52, 2,
										String.format("賣一: %.2f(%3d)",
											i._sell_price[0], i._sell_volume[0]));
							} else if (i._current_price == i._sell_price[0]) {
								textGraphics.putString(33, 2,
										String.format("買一: %.2f(%3d)", i._buy_price[0], i._buy_volume[0]));

								textGraphics.putString(52, 2, "賣一: ");
								textGraphics.putString(57, 2,
										String.format("%.2f(%3d)", i._sell_price[0], i._sell_volume[0]),
										SGR.UNDERLINE);
							} else {
								textGraphics.putString(33, 2,
										String.format("買一: %.2f(%3d)", i._buy_price[0], i._buy_volume[0]));
								textGraphics.putString(52, 2,
										String.format("賣一: %.2f(%3d)",
											i._sell_price[0], i._sell_volume[0]));
							}

							for (int p = 1; p < 5; p++) {
								textGraphics.putString(33, 2 + p, String.format("%s: %.2f(%3d)  %s: %.2f(%3d)",
											BUY_LEVEL[p],
											i._buy_price[p], i._buy_volume[p],
											SELL_LEVEL[p],
											i._sell_price[p], i._sell_volume[p]));
							}

							if (i._temporal_volume >= 5) {
								textGraphics.putString(0, 9 + (big_c % 10), "                                                       ");
								textGraphics.putString(0, 9 + (big_c % 10), big_c + ") " + i._time_stamp2 + ": 大單: " + i._temporal_volume);
								
								if (diff > 0) {
									textGraphics.setBackgroundColor(TextColor.ANSI.RED);
									String msg = String.format("成交價: %.2f 漲: %.2f", i._current_price, diff);
									textGraphics.putString(24, 9 + (big_c % 10), msg);
									textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
								} else if (diff < 0) {
									textGraphics.setBackgroundColor(TextColor.ANSI.GREEN);
									String msg = String.format("成交價: %.2f 跌: %.2f", i._current_price, diff);
									textGraphics.putString(24, 9 + (big_c % 10), msg);
									textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
								} else {
									String msg = String.format("成交價: %.2f 平盤    ", i._current_price);
									textGraphics.putString(24, 9 + (big_c % 10), msg);
								}

								big_c++;
							}

							if (last_stock == null || last_stock._time_stamp2.equals(i._time_stamp2) == false)
								last_stock = i;
						}

						s._instant_stocks.clear();
					}
					screen.refresh();
				}

				for (int i = 0; i < 50; i++) {
					KeyStroke k = screen.pollInput();

					if (k != null && k.getCharacter().equals('q') == true) {
						screen.stopScreen();

						executor.shutdown();
						executor.shutdownNow();

						if (!executor.awaitTermination(5, TimeUnit.SECONDS))
							System.out.println("Pool did not terminate");

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
