import java.util.ArrayList;

public class Stock {
	static public class Daily {
		public String _today;
		public String _id;
		public String _name;
		public float _highest_price;
		public float _lowest_price;
		public float _open_price;
		public float _up_stop_price;
		public float _down_stop_price;
		public float _yesterday_price;
		public ArrayList<Instant> _instant_stocks = new ArrayList<Instant>();

		public void add(Instant instantStock) {
			synchronized (_instant_stocks) {
				_instant_stocks.add(instantStock);
			}
		}
	}

	static public class Instant {
		public long _time_stamp;
		public String _time_stamp2;
		public float _current_price;
		public int _temporal_volume;
		public int _volume;
		public float [] _sell_price = new float[5];
		public int [] _sell_volume = new int[5];
		public float [] _buy_price = new float[5];
		public int [] _buy_volume = new int[5];
	}
}
