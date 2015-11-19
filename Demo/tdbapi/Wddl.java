package tdbapi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import cn.com.wind.td.tdb.CYCTYPE;
import cn.com.wind.td.tdb.Code;
import cn.com.wind.td.tdb.Future;
import cn.com.wind.td.tdb.FutureAB;
import cn.com.wind.td.tdb.KLine;
import cn.com.wind.td.tdb.OPEN_SETTINGS;
import cn.com.wind.td.tdb.Order;
import cn.com.wind.td.tdb.OrderQueue;
import cn.com.wind.td.tdb.ReqFuture;
import cn.com.wind.td.tdb.ReqKLine;
import cn.com.wind.td.tdb.ReqTick;
import cn.com.wind.td.tdb.ReqTransaction;
import cn.com.wind.td.tdb.ResLogin;
import cn.com.wind.td.tdb.TDBClient;
import cn.com.wind.td.tdb.Tick;
import cn.com.wind.td.tdb.TickAB;
import cn.com.wind.td.tdb.Transaction;

public class Wddl {

	TDBClient client = new TDBClient();

	static String arrayToStr(long[] array) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (long v : array) {
			sb.append(v).append(",");
		}
		sb.append("]");
		return sb.toString();
	}

	static String arrayToStr(int[] array) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int v : array) {
			sb.append(v).append(",");
		}
		sb.append("]");
		return sb.toString();
	}

	String m_testCode = "600309.SH";
	int m_testBeginDate = 20151119;
	int m_testEndDate = 20151119;
	int m_testBeginTime = 0;
	int m_testEndTime = -1;

	int m_nMaxOutputCount = 15;
	private File dir;
	private File dataFile;

	Wddl(String ip, int port, String username, String password) {
		OPEN_SETTINGS setting = new OPEN_SETTINGS();
		setting.setIP(ip);
		setting.setPort(Integer.toString(port));
		setting.setUser(username);
		setting.setPassword(password);
		setting.setRetryCount(0);
		setting.setRetryGap(5);
		setting.setTimeOutVal(0);

		ResLogin res = client.open(setting);
		if (res == null) {
			System.out.println("Can't connect to " + ip);
			System.exit(-1);
		} else {
			System.out.println(res.getMarkets());
			int count = res.getMarkets();
			String[] market = res.getMarket();
			int[] dyndate = res.getDynDate();

			for (int i = 0; i < count; i++) {
				System.out.println(market[i] + " " + dyndate[i]);
			}
		}
	}

	void test_getCodeTable() {

		Code[] codes = client.getCodeTable("SH");

		int nIndex = 0;
		for (Code code : codes) {
			if (nIndex++ > m_nMaxOutputCount)
				break;
			StringBuffer sb = new StringBuffer();
			sb.append("CODE: ").append(code.getWindCode()).append(" ").append(code.getMarket()).append(" ").append(code.getCode()).append(" ").append(code.getENName()).append(" ").append(code.getCNName()).append(" ").append(code.getType());

			System.out.println(sb.toString());
		}

	}

	void test_getKLine() {
		ReqKLine req = new ReqKLine();

		req.setCode(m_testCode);
		req.setCycType(CYCTYPE.CYC_MINUTE);
		req.setCycDef(60);
		req.setBeginDate(m_testBeginDate);
		req.setEndDate(m_testEndDate);

		KLine[] kline = client.getKLine(req);
		if (kline == null) {
			System.out.println("getKline failed!");
			return;
		}
		System.out.println("Success to call getKline(?) " + kline.length);
		int nIndex = 0;
		for (KLine k : kline) {
			if (nIndex++ > m_nMaxOutputCount)
				break;
			StringBuilder sb = new StringBuilder();
			sb.append(k.getWindCode()).append(" ").append(k.getCode()).append(" ").append(k.getDate()).append(" ").append(k.getTime()).append(" ").append(k.getOpen()).append(" ").append(k.getHigh()).append(" ").append(k.getLow()).append(" ").append(k.getClose()).append(" ").append(k.getVolume()).append(" ").append(k.getTurover()).append(" ").append(k.getItems()).append(" ").append(k.getInterest());
			System.out.println(sb.toString());
		}
	}

	void test_getTick() {
		ReqTick req = new ReqTick();

		req.setCode(m_testCode);
		req.setBeginDate(m_testBeginDate);
		req.setEndDate(m_testEndDate);
		req.setBeginTime(m_testBeginTime);
		req.setEndTime(m_testEndTime);

		Tick[] tick = client.getTick(req);
		if (tick == null) {
			System.out.println("getTick failed!");
			return;
		}
		System.out.println("Success to call getTick(?) " + tick.length);
		int nIndex = 0;
		for (Tick k : tick) {
			if (nIndex++ > m_nMaxOutputCount)
				break;
			StringBuilder sb = new StringBuilder();
			sb.append(k.getWindCode()).append(" ").append(k.getCode()).append(" ").append(k.getDate()).append(" ").append(k.getTime()).append(" ").append(k.getOpen()).append(" ").append(k.getHigh()).append(" ").append(k.getLow()).append(" ").append(k.getPreClose()).append(" ").append(k.getVolume()).append(" ").append(k.getTurover()).append(" ").append(k.getItems()).append(" ").append(k.getInterest()).append(" ").append(k.getPrice()).append(" ").append(k.getTradeFlag()).append(" ").append(k.getBSFlag()).append(" ").append(k.getAccVolume()).append(" ").append(k.getAccTurover()).append(" ");

			System.out.println(sb.toString());
		}
	}

	void test_getTickAB() {
		ReqTick req = new ReqTick();
		req.setCode(m_testCode);
		req.setBeginDate(m_testBeginDate);
		req.setEndDate(m_testEndDate);
		req.setBeginTime(m_testBeginTime);
		req.setEndTime(m_testEndTime);

		TickAB[] tick = client.getTickAB(req);

		if (tick == null) {
			System.out.println("Fail to call getTickAB(?)");
			return;
		}
		System.out.println("Success to call getTickAB(?) " + tick.length);
		for (int i = 0; i < tick.length && i < m_nMaxOutputCount; ++i) {
			StringBuilder sb = new StringBuilder();
			sb.append(tick[i].getCode()).append(" ").append(tick[i].getAccTurover()).append(" ").append(tick[i].getHigh()).append(" ").append(tick[i].getLow()).append(" ").append(tick[i].getOpen()).append(" ").append(tick[i].getPreClose()).append(" ").append(arrayToStr(tick[i].getAskPrice())).append(" ").append(arrayToStr(tick[i].getAskVolume())).append(" ").append(arrayToStr(tick[i].getBidPrice())).append(" ").append(arrayToStr(tick[i].getBidVolume())).append(" ").append(tick[i].getAskAvPrice()).append(" ").append(tick[i].getBidAvPrice()).append(" ").append(tick[i].getTotalAskVolume()).append(" ").append(tick[i].getStocks()).append(" ").append(tick[i].getUps()).append(" ").append(tick[i].getIndex()).append(" ").append(tick[i].getDowns()).append(" ").append(tick[i].getHoldLines())
					.append(" ");

			System.out.println(sb.toString());
		}
	}

	void test_getFuture() {
		ReqFuture req = new ReqFuture();

		req.setCode("IF1404.CF");
		req.setBeginDate(m_testBeginDate);
		req.setEndDate(m_testEndDate);
		req.setBeginTime(m_testBeginTime);
		req.setEndTime(m_testEndTime);
		req.setAutoComplete(1);

		Future[] objs = client.getFuture(req);

		if (objs != null) {
			System.out.println("Success to call getFuture(?) " + objs.length);
			for (int i = 0; i < objs.length && i < m_nMaxOutputCount; ++i) {
				StringBuilder sb = new StringBuilder();

				sb.append(objs[i].getCode()).append(" ").append(objs[i].getWindCode()).append(" ").append(objs[i].getDate()).append(" ").append(objs[i].getTime()).append(" ").append(objs[i].getVolume()).append(" ").append(objs[i].getTurover()).append(" ").append(objs[i].getSettle()).append(" ").append(objs[i].getPosition()).append(" ").append(objs[i].getCurDelta()).append(" ").append(objs[i].getTradeFlag()).append(" ").append(objs[i].getAccVolume()).append(" ").append(objs[i].getAccTurover()).append(" ").append(objs[i].getHigh()).append(" ").append(objs[i].getLow()).append(" ").append(objs[i].getOpen()).append(" ").append(objs[i].getPrice()).append(" ").append(objs[i].getPreClose()).append(" ").append(objs[i].getPrePosition()).append(" ").append(objs[i].getPreSettle()).append(" ");

				System.out.println(sb.toString());
			}
		} else {
			System.out.print("Fail to call getFuture(?)\n");
		}
	}

	void test_getFutureAB() {
		ReqFuture req = new ReqFuture();

		req.setCode("IF1404.CF");
		req.setBeginDate(m_testBeginDate);
		req.setEndDate(m_testEndDate);
		req.setBeginTime(m_testBeginTime);
		req.setEndTime(m_testEndTime);
		req.setAutoComplete(1);

		FutureAB[] objs = client.getFutureAB(req);

		if (objs == null) {
			System.out.print("Fail to call getFutureAB(?)\n");
			return;
		}

		System.out.println("Success to call getFutureAB(?) " + objs.length);

		for (int i = 0; i < objs.length && i < m_nMaxOutputCount; ++i) {
			StringBuilder sb = new StringBuilder();

			sb.append(objs[i].getCode()).append(" ").append(objs[i].getWindCode()).append(" ").append(objs[i].getDate()).append(" ").append(objs[i].getTime()).append(" ").append(objs[i].getVolume()).append(" ").append(objs[i].getTurover()).append(" ").append(objs[i].getSettle()).append(" ").append(objs[i].getPosition()).append(" ").append(objs[i].getCurDelta()).append(" ").append(objs[i].getTradeFlag()).append(" ").append(objs[i].getAccVolume()).append(" ").append(objs[i].getAccTurover()).append(" ").append(objs[i].getHigh()).append(" ").append(objs[i].getLow()).append(" ").append(objs[i].getOpen()).append(" ").append(objs[i].getPrice()).append(" ").append(objs[i].getPreClose()).append(" ").append(objs[i].getPrePosition()).append(" ").append(objs[i].getPreSettle()).append(" ");

			System.out.println(sb.toString());
		}
	}

	void test_getCodeInfo() {

		Code objs = client.getCodeInfo(m_testCode);

		if (objs == null) {
			System.out.print("Fail to call getCodeInfo(?)\n");
			return;
		}

		System.out.print("Success to call getCodeInfo(?)\n");
		StringBuilder sb = new StringBuilder();

		sb.append(objs.getCode()).append(" ").append(objs.getWindCode()).append(" ").append(objs.getMarket()).append(" ").append(objs.getCNName()).append(" ").append(objs.getENName()).append(" ").append(objs.getType()).append(" ");

		System.out.println(sb.toString());

	}

	void test_getTransaction() {
		ReqTransaction req = new ReqTransaction();

		req.setCode(m_testCode);
		req.setBeginDate(m_testBeginDate);
		req.setEndDate(m_testEndDate);
		req.setBeginTime(m_testBeginTime);
		req.setEndTime(m_testEndTime);

		Transaction[] objs = client.getTransaction(req);

		if (objs == null) {
			System.out.print("Fail to call getTransaction(?)\n");
			return;
		}
		System.out.println("Success to call getTransaction(?) " + objs.length);

		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile)));

			for (int i = 0; i < objs.length; ++i) {//&& i < m_nMaxOutputCount
				StringBuilder sb = new StringBuilder();

				Transaction trans = objs[i];
				sb.append(trans.getWindCode()) //
						.append(" ").append(trans.getCode())//
						.append(" ").append(trans.getDate())//
						.append(" ").append(trans.getTime())//
						.append(" ").append(trans.getIndex())//
						.append(" ").append(trans.getFunctionCode())//
						.append(" ").append(trans.getOrderKind())//
						.append(" ").append(trans.getBSFlag())//
						.append(" ").append(trans.getTradePrice())//
						.append(" ").append(trans.getTradeVolume())//
						.append(" ").append(trans.getAskOrder())//
						.append(" ").append(trans.getBidOrder());

				String line = sb.toString();
				System.out.println(line);

				out.write(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	void test_getOrder() {
		ReqTransaction req = new ReqTransaction();

		req.setCode(m_testCode);
		req.setBeginDate(m_testBeginDate);
		req.setEndDate(m_testEndDate);
		req.setBeginTime(m_testBeginTime);
		req.setEndTime(m_testEndTime);

		Order[] objs = client.getOrder(req);

		if (objs == null) {
			System.out.print("Fail to call getOrder(?)\n");
			return;
		}
		System.out.println("Success to call getOrder(?) " + objs.length);
		for (int i = 0; i < objs.length && i < m_nMaxOutputCount; ++i) {
			StringBuilder sb = new StringBuilder();

			sb.append(objs[i].getWindCode()) //
					.append(" ").append(objs[i].getCode()) //
					.append(" ").append(objs[i].getDate())//
					.append(" ").append(objs[i].getTime())//
					.append(" ").append(objs[i].getIndex())//
					.append(" ").append(objs[i].getFunctionCode())//
					.append(" ").append(objs[i].getOrderKind())//
					.append(" ").append(objs[i].getOrder())//
					.append(" ").append(objs[i].getOrderPrice())//
					.append(" ").append(objs[i].getOrderVolume())//
					.append(" ");

			System.out.println(sb.toString());
		}
	}

	void test_getOrderQueue() {
		ReqTransaction req = new ReqTransaction();

		req.setCode(m_testCode);
		req.setBeginDate(m_testBeginDate);
		req.setEndDate(m_testEndDate);
		req.setBeginTime(m_testBeginTime);
		req.setEndTime(m_testEndTime);

		OrderQueue[] objs = client.getOrderQueue(req);

		if (objs == null) {
			System.out.print("Fail to call getOrderQueue(?)\n");
			return;
		}
		System.out.println("Success to call getOrderQueue(?) " + objs.length);
		for (int i = 0; i < objs.length && i < m_nMaxOutputCount; ++i) {
			StringBuilder sb = new StringBuilder();

			sb.append(objs[i].getCode()).append(" ").append(objs[i].getWindCode()).append(" ").append(objs[i].getDate()).append(" ").append(objs[i].getTime()).append(" ").append(objs[i].getSide()).append(" ").append(objs[i].getPrice()).append(" ").append(objs[i].getOrderItems()).append(" ").append(objs[i].getABItems()).append(" ");

			System.out.println(sb.toString());
		}
	}

	void run() {
		dir = new File("/usr/wddl/" + "20151119");
		if (!dir.exists()) {
			if (dir.mkdir()) {
				System.out.println("创建目录成功");
			} else {
				System.out.println("创建目录失败.....");
			}
		}

		dataFile = new File(dir, "600309.txt");

		//		test_getKLine();
		//		test_getTick();
		//		try {
		//			test_getTickAB();
		//		} catch (Exception ex) {
		//			System.out.println("Fail to call getTickAB(?). Exception: " + ex.getMessage());
		//		}
		//
		//		test_getFuture();
		//		test_getFutureAB();
		//		test_getCodeInfo();
		test_getTransaction();
		//		test_getOrder();
		//		test_getOrderQueue();
	}

	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("usage:  Wddl ip port user password");
			System.exit(1);
		}

		Wddl d = new Wddl(args[0], Integer.parseInt(args[1]), args[2], args[3]);

		System.out.println("*******************************begin test****************************");

		d.run();

		System.out.println("*******************************end test****************************");
	}
}
