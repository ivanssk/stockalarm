package com.org.finance.stock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.ArrayList;

public class StockFilter {
	static public String composeRequest(BufferedReader br) throws Exception {
		StringBuilder url = new StringBuilder("http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=");
		String line = br.readLine();

		if (line == null)
			return null;

		url.append("tse_" + line + ".tw|");
		return url.substring(0, url.length() - 1) + "&json=1&delay=0&_=" + System.currentTimeMillis();
	}
}
