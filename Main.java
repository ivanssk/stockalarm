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

public class Main {
	private static final String [] BUY_LEVEL = new String [] {"買一", "買二", "買三", "買四", "買五"};
	private static final String [] SELL_LEVEL = new String [] {"賣一", "賣二", "賣三", "賣四", "賣五"};

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[41m";
	public static final String ANSI_GREEN = "\u001B[42m";

	static public void main(String [] args) {
		try {
			ExecutorService executor = Executors.newFixedThreadPool(args.length + 1);
			//BufferedReader br = new BufferedReader(new FileReader("stocks.txt"));

			ArrayBlockingQueue<String> messageQueue = new ArrayBlockingQueue<String>(args.length * 100 * 100);
			HashMap<String, Stock.Daily> stockContainer = new HashMap<String, Stock.Daily>();

			executor.execute(new MessageParser(messageQueue, stockContainer));

			String request = "http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_" + args[0] + ".tw&json=1&delay=0&_=" + System.currentTimeMillis();
			executor.execute(new StockGrabber(messageQueue, request));
			Thread.sleep(150);

			Stock.Instant last_stock = null;

			while (true) {
				synchronized (stockContainer) {
					Stock.Daily [] dailyStocks = stockContainer.values().toArray(new Stock.Daily[0]);

					for (Stock.Daily s: dailyStocks) {
						int size = s._instant_stocks.size();

						for (int x = size - 1; x >= 0; x--) {
							Stock.Instant i = s._instant_stocks.get(x);

							if (last_stock != null && last_stock._time_stamp == i._time_stamp)
								continue;

							if (i._temporal_volume >= 50) {
								System.out.print("股票代號: " + s._id + " (" + s._name + ")   ");
								System.out.println("時間: " + i._time_stamp2);
								System.out.print("現價: " + i._current_price + "   ");

								float diff = 0.0f;
								
								if (last_stock == null)
									diff = i._current_price - s._yesterday_price;
								else
									diff = i._current_price - last_stock._current_price;

								if (diff > 0)
									System.out.println(String.format("漲跌: " + ANSI_RED + "%.2f" + ANSI_RESET, diff));
								else if (diff < 0)
									System.out.println(String.format("漲跌: " + ANSI_GREEN + "%.2f" + ANSI_RESET, Math.abs(diff)));
								else
									System.out.println(String.format("漲跌: %.2f", diff));

								System.out.print("今日最高價: " + s._highest_price + "   ");
								System.out.println("今日最低價: " + s._lowest_price);

								System.out.print("單量: " + ANSI_RED + i._temporal_volume + ANSI_RESET + "   ");
								System.out.println("累積量: " + i._volume);

								StringBuilder buy_ranking = new StringBuilder();

								System.out.println("");

								for (int p = 0; p < 5; p++) {
									System.out.println(String.format("%s: %.2f(%3d)  %s: %.2f(%3d)",
												BUY_LEVEL[p],
												i._buy_price[p], i._buy_volume[p],
												SELL_LEVEL[p],
												i._sell_price[p], i._sell_volume[p]));
								}

								System.out.println("-------------------------------------\n");
							}

							if (last_stock == null || last_stock._time_stamp != i._time_stamp)
								last_stock = i;
						}

						s._instant_stocks.clear();

						/*
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
						   */
					}
				}

				Thread.sleep(1000);
			}

			/*
			Thread.sleep(Long.MAX_VALUE);
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
