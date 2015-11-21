package tdbapi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

import cn.com.wind.td.tdb.CYCTYPE;
import cn.com.wind.td.tdb.Code;
import cn.com.wind.td.tdb.Future;
import cn.com.wind.td.tdb.FutureAB;
import cn.com.wind.td.tdb.KLine;
import cn.com.wind.td.tdb.OPEN_SETTINGS;
import cn.com.wind.td.tdb.Order;
import cn.com.wind.td.tdb.OrderQueue;
import cn.com.wind.td.tdb.REFILLFLAG;
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
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	public static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
	public static String SHI = "000001.SZ"; //"000001.SH";

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

	//String m_testCode = "600309.SH";
	//int m_testBeginDate = 20151119;
	//int m_testEndDate = 20151119;
	int m_testBeginTime = 0;
	int m_testEndTime = -1;

	int nextTaskLine = 0;

	int m_nMaxOutputCount = Integer.MAX_VALUE;
	private File dirDate, dirBase;
	//private File dataFile;
	//private Code[] codes;

	private List<Code> codeList = new ArrayList<>();
	private List<String> allTasks;

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

	void getCodeTables(String market) {
		Code[] codes = client.getCodeTable(market);

		File codeFile = new File(dirBase, market + "_CodeTable.txt");
		Writer out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(codeFile)));

			int nIndex = 0;
			for (Code code : codes) {
				if (nIndex++ > m_nMaxOutputCount)
					break;

				if (code.getType() >= 0x20)
					continue;

				codeList.add(code);

				StringBuffer sb = new StringBuffer();
				sb.append(code.getWindCode()) //
						.append(" ").append(code.getCode())//
						.append(" ").append(code.getMarket())//
						.append(" ").append(code.getCNName())//
						.append(" ").append(code.getENName())//
						.append(" ").append(code.getType());

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

	boolean getKLine(String code, int date) {
		ReqKLine req = new ReqKLine();

		req.setCode(code);
		req.setCycType(CYCTYPE.CYC_DAY);
		req.setCycDef(1); //60
		req.setCQFlag(REFILLFLAG.REFILL_BACKWARD);
		req.setBeginDate(date);
		req.setEndDate(date);

		KLine[] kline = client.getKLine(req);
		if (kline == null) {
			System.out.println("getKline failed: " + date + " " + code);
			return false;
		}

		System.out.println("Success to call getKline(?) " + kline.length);

		BufferedWriter out = null;
		try {
			File dir = new File(dirDate, "KLine");
			dir.mkdir();

			File dataFile = new File(dir, code + ".txt");

			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile)));

			int nIndex = 0;
			for (KLine k : kline) {
				if (nIndex++ > m_nMaxOutputCount)
					break;
				StringBuilder sb = new StringBuilder();
				sb.append(k.getWindCode()).append(" ")//
						.append(k.getCode())//
						.append(" ").append(k.getDate())//
						.append(" ").append(k.getTime())//
						.append(" ").append(k.getOpen())//
						.append(" ").append(k.getHigh())//
						.append(" ").append(k.getLow())//
						.append(" ").append(k.getClose())//
						.append(" ").append(k.getVolume())//
						.append(" ").append(k.getTurover())//
						.append(" ").append(k.getItems())//
						.append(" ").append(k.getInterest());
				String line = sb.toString();
				//System.out.println(line);

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

		return true;
	}

	//	void test_getTick() {
	//		ReqTick req = new ReqTick();
	//
	//		req.setCode(m_testCode);
	//		req.setBeginDate(m_testBeginDate);
	//		req.setEndDate(m_testEndDate);
	//		req.setBeginTime(m_testBeginTime);
	//		req.setEndTime(m_testEndTime);
	//
	//		Tick[] tick = client.getTick(req);
	//		if (tick == null) {
	//			System.out.println("getTick failed!");
	//			return;
	//		}
	//		System.out.println("Success to call getTick(?) " + tick.length);
	//		int nIndex = 0;
	//		for (Tick k : tick) {
	//			if (nIndex++ > m_nMaxOutputCount)
	//				break;
	//			StringBuilder sb = new StringBuilder();
	//			sb.append(k.getWindCode()).append(" ").append(k.getCode()).append(" ").append(k.getDate()).append(" ").append(k.getTime())//
	//					.append(" ").append(k.getOpen()).append(" ").append(k.getHigh()).append(" ").append(k.getLow()) //
	//					.append(" ").append(k.getPreClose()).append(" ").append(k.getVolume()).append(" ").append(k.getTurover()) //
	//					.append(" ").append(k.getItems()).append(" ").append(k.getInterest()).append(" ").append(k.getPrice()).append(" ")//
	//					.append(k.getTradeFlag()).append(" ").append(k.getBSFlag()).append(" ").append(k.getAccVolume()).append(" ")//
	//					.append(k.getAccTurover()).append(" ");
	//
	//			System.out.println(sb.toString());
	//		}
	//	}

	void getTickAB(String code, int date) {
		ReqTick req = new ReqTick();
		req.setCode(code);
		req.setBeginDate(date);
		req.setEndDate(date);
		req.setBeginTime(m_testBeginTime);
		req.setEndTime(m_testEndTime);

		TickAB[] tick = client.getTickAB(req);

		if (tick == null) {
			System.out.println("Fail to call getTickAB(?)");
			return;
		}
		System.out.println("Success to call getTickAB(?) " + tick.length);

		BufferedWriter out = null;
		try {
			File dir = new File(dirDate, "TickAB");
			dir.mkdir();

			File dataFile = new File(dir, code + ".txt");

			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile)));
			int nIndex = 0;
			for (TickAB k : tick) {
				if (nIndex++ > m_nMaxOutputCount)
					break;

				StringBuilder sb = new StringBuilder();
				sb.append(k.getWindCode())//
						.append(" ").append(k.getCode())//
						.append(" ").append(k.getDate())//
						.append(" ").append(k.getTime()) //
						.append(" ").append(k.getPrice()) //
						.append(" ").append(k.getVolume()) //
						.append(" ").append(k.getTurover()) //
						.append(" ").append(k.getItems()) //
						.append(" ").append(k.getInterest()) //
						.append(" ").append(k.getTradeFlag()) //
						.append(" ").append(k.getBSFlag()) //
						.append(" ").append(k.getAccVolume())//
						.append(" ").append(k.getAccTurover())//
						.append(" ").append(k.getHigh())//
						.append(" ").append(k.getLow())//
						.append(" ").append(k.getOpen())//
						.append(" ").append(k.getPreClose())//
						.append(" ").append(arrayToStr(k.getAskPrice()))//
						.append(" ").append(arrayToStr(k.getAskVolume()))//
						.append(" ").append(arrayToStr(k.getBidPrice()))//
						.append(" ").append(arrayToStr(k.getBidVolume()))//
						.append(" ").append(k.getAskAvPrice())//
						.append(" ").append(k.getBidAvPrice())//
						.append(" ").append(k.getTotalAskVolume())//
						.append(" ").append(k.getTotalBidVolume())//
						.append(" ").append(k.getIndex())//
						.append(" ").append(k.getStocks())//
						.append(" ").append(k.getUps())//
						.append(" ").append(k.getDowns())//
						.append(" ").append(k.getHoldLines());

				String line = sb.toString();
				//System.out.println(line);

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

	//	void test_getFuture() {
	//		ReqFuture req = new ReqFuture();
	//
	//		req.setCode("IF1404.CF");
	//		req.setBeginDate(m_testBeginDate);
	//		req.setEndDate(m_testEndDate);
	//		req.setBeginTime(m_testBeginTime);
	//		req.setEndTime(m_testEndTime);
	//		req.setAutoComplete(1);
	//
	//		Future[] objs = client.getFuture(req);
	//
	//		if (objs != null) {
	//			System.out.println("Success to call getFuture(?) " + objs.length);
	//			for (int i = 0; i < objs.length && i < m_nMaxOutputCount; ++i) {
	//				StringBuilder sb = new StringBuilder();
	//
	//				sb.append(objs[i].getCode()).append(" ").append(objs[i].getWindCode()).append(" ").append(objs[i].getDate()).append(" ").append(objs[i].getTime()).append(" ").append(objs[i].getVolume()).append(" ").append(objs[i].getTurover()).append(" ").append(objs[i].getSettle()).append(" ").append(objs[i].getPosition()).append(" ").append(objs[i].getCurDelta()).append(" ").append(objs[i].getTradeFlag()).append(" ").append(objs[i].getAccVolume()).append(" ").append(objs[i].getAccTurover()).append(" ").append(objs[i].getHigh()).append(" ").append(objs[i].getLow()).append(" ").append(objs[i].getOpen()).append(" ").append(objs[i].getPrice()).append(" ").append(objs[i].getPreClose()).append(" ").append(objs[i].getPrePosition()).append(" ").append(objs[i].getPreSettle()).append(" ");
	//
	//				System.out.println(sb.toString());
	//			}
	//		} else {
	//			System.out.print("Fail to call getFuture(?)\n");
	//		}
	//	}

	//	void test_getFutureAB() {
	//		ReqFuture req = new ReqFuture();
	//
	//		req.setCode("IF1404.CF");
	//		req.setBeginDate(m_testBeginDate);
	//		req.setEndDate(m_testEndDate);
	//		req.setBeginTime(m_testBeginTime);
	//		req.setEndTime(m_testEndTime);
	//		req.setAutoComplete(1);
	//
	//		FutureAB[] objs = client.getFutureAB(req);
	//
	//		if (objs == null) {
	//			System.out.print("Fail to call getFutureAB(?)\n");
	//			return;
	//		}
	//
	//		System.out.println("Success to call getFutureAB(?) " + objs.length);
	//
	//		for (int i = 0; i < objs.length && i < m_nMaxOutputCount; ++i) {
	//			StringBuilder sb = new StringBuilder();
	//
	//			sb.append(objs[i].getCode()).append(" ").append(objs[i].getWindCode()).append(" ").append(objs[i].getDate()).append(" ").append(objs[i].getTime()).append(" ").append(objs[i].getVolume()).append(" ").append(objs[i].getTurover()).append(" ").append(objs[i].getSettle()).append(" ").append(objs[i].getPosition()).append(" ").append(objs[i].getCurDelta()).append(" ").append(objs[i].getTradeFlag()).append(" ").append(objs[i].getAccVolume()).append(" ").append(objs[i].getAccTurover()).append(" ").append(objs[i].getHigh()).append(" ").append(objs[i].getLow()).append(" ").append(objs[i].getOpen()).append(" ").append(objs[i].getPrice()).append(" ").append(objs[i].getPreClose()).append(" ").append(objs[i].getPrePosition()).append(" ").append(objs[i].getPreSettle()).append(" ");
	//
	//			System.out.println(sb.toString());
	//		}
	//	}

	//	void test_getCodeInfo() {
	//
	//		Code objs = client.getCodeInfo(m_testCode);
	//
	//		if (objs == null) {
	//			System.out.print("Fail to call getCodeInfo(?)\n");
	//			return;
	//		}
	//
	//		System.out.print("Success to call getCodeInfo(?)\n");
	//		StringBuilder sb = new StringBuilder();
	//
	//		sb.append(objs.getCode()).append(" ").append(objs.getWindCode()).append(" ").append(objs.getMarket()).append(" ").append(objs.getCNName()).append(" ").append(objs.getENName()).append(" ").append(objs.getType()).append(" ");
	//
	//		System.out.println(sb.toString());
	//
	//	}

	void getTransaction(String windCode, int date) {
		ReqTransaction req = new ReqTransaction();

		req.setCode(windCode);
		req.setBeginDate(date);
		req.setEndDate(date);
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
			File dir = new File(dirDate, "Transaction");
			dir.mkdir();

			File dataFile = new File(dir, windCode + ".txt");

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
				//System.out.println(line);

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

	void getOrder(String code, int date) {
		if (!code.endsWith(".SZ"))
			return;

		ReqTransaction req = new ReqTransaction();

		req.setCode(code);
		req.setBeginDate(date);
		req.setEndDate(date);
		req.setBeginTime(m_testBeginTime);
		req.setEndTime(m_testEndTime);

		Order[] objs = client.getOrder(req);

		if (objs == null) {
			System.out.print("Fail to call getOrder(?)\n");
			return;
		}
		System.out.println("Success to call getOrder(?) " + objs.length);

		BufferedWriter out = null;
		try {
			File dir = new File(dirDate, "Order");
			dir.mkdir();

			File dataFile = new File(dir, code + ".txt");

			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile)));

			for (int i = 0; i < objs.length && i < m_nMaxOutputCount; ++i) {
				StringBuilder sb = new StringBuilder();

				sb.append(objs[i].getWindCode()) //
						.append(" ").append(objs[i].getCode()) //
						.append(" ").append(objs[i].getDate())//
						.append(" ").append(objs[i].getTime())//
						.append(" ").append(objs[i].getIndex())//
						.append(" ").append(objs[i].getOrder())//
						.append(" ").append(objs[i].getOrderKind())//
						.append(" ").append(objs[i].getFunctionCode())//
						.append(" ").append(objs[i].getOrderPrice())//
						.append(" ").append(objs[i].getOrderVolume());

				String line = sb.toString();
				//System.out.println(line);
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

	void getOrderQueue(String code, int date) {
		ReqTransaction req = new ReqTransaction();

		req.setCode(code);
		req.setBeginDate(date);
		req.setEndDate(date);
		req.setBeginTime(m_testBeginTime);
		req.setEndTime(m_testEndTime);

		OrderQueue[] objs = client.getOrderQueue(req);

		if (objs == null) {
			System.out.print("Fail to call getOrderQueue(?)\n");
			return;
		}
		System.out.println("Success to call getOrderQueue(?) " + objs.length);

		BufferedWriter out = null;

		try {
			File dir = new File(dirDate, "OrderQueue");
			dir.mkdir();

			File dataFile = new File(dir, code + ".txt");

			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile)));

			for (int i = 0; i < objs.length && i < m_nMaxOutputCount; ++i) {
				StringBuilder sb = new StringBuilder();

				sb.append(objs[i].getWindCode())//
						.append(" ").append(objs[i].getCode())//
						.append(" ").append(objs[i].getDate())//
						.append(" ").append(objs[i].getTime())//
						.append(" ").append(objs[i].getSide())//
						.append(" ").append(objs[i].getPrice())//
						.append(" ").append(objs[i].getOrderItems())//
						.append(" ").append(objs[i].getABItems())//
						.append(" ").append(arrayToStr(objs[i].getABVolume()));

				String line = sb.toString();
				//System.out.println(line);
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

	void run() {
		initBaseDir();

		getCodeTables("SH");
		getCodeTables("SZ");

		initTaskList();

		initCurrentTask();

		//for (int i = 0; i < 10; i++) {
		for (;;) {
			nextTask();
		}

		//		String date = "20151119";
		//		int dateAsInt = Integer.parseInt(date);
		//		initDateDir(date);
		//
		//		long startTime = System.currentTimeMillis();
		//
		//		for (Code code : codeList) {
		//			String windCode = code.getWindCode();
		//			//if ("600309.SH".equals(windCode)) {
		//			processCode(code, dateAsInt);
		//			//}
		//		}
		//
		//		logDayJob(date, startTime);
	}

	private void nextTask() {
		String taskLine = allTasks.get(nextTaskLine);
		String[] taskTokens = taskLine.split(" ");
		if ("H".equals(taskTokens[0])) {
			nextTaskLine++;
			return;
		}

		long startTime = System.currentTimeMillis();
		String date = taskTokens[1];
		String windCode = taskTokens[2];

		initDateDir(date);

		processCode(windCode, Integer.parseInt(date));

		nextTaskLine++;
		logDlTask(windCode, date, nextTaskLine, startTime);
	}

	private void processCode(String windCode, int date) {
		try {
			System.out.println("---------- Processing download task: " + date + " " + windCode + " ----------");
			//			File dataFile = new File(dir, code.getCode() + ".txt");
			//
			//			dataFile.createNewFile();

			try {
				getKLine(windCode, date);
			} catch (Exception ex) {
				System.out.println("Fail to call getKLine(?). Exception: " + ex.getMessage());
			}

			//		test_getTick();
			try {
				getTickAB(windCode, date);
			} catch (Exception ex) {
				System.out.println("Fail to call getTickAB(?). Exception: " + ex.getMessage());
			}
			//
			//		test_getFuture();
			//		test_getFutureAB();
			//		test_getCodeInfo();

			try {
				getTransaction(windCode, date);
			} catch (Exception ex) {
				System.out.println("Fail to call getTransaction(?). Exception: " + ex.getMessage());
			}

			try {
				getOrder(windCode, date);
			} catch (Exception ex) {
				System.out.println("Fail to call getOrder(?). Exception: " + ex.getMessage());
			}

			try {
				getOrderQueue(windCode, date);
			} catch (Exception ex) {
				System.out.println("Fail to call getOrderQueue(?). Exception: " + ex.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initDateDir(String date) {
		dirDate = new File("/usr/wddl/" + date);

		if (!dirDate.exists()) {
			if (dirDate.mkdir()) {
				System.out.println("创建日期目录成功");
			} else {
				System.out.println("创建日期目录失败.....");
			}
		}
	}

	private void initBaseDir() {
		dirBase = new File("/usr/wddl");

		if (!dirBase.exists()) {
			if (dirDate.mkdir()) {
				System.out.println("创建数据根目录成功");
			} else {
				System.out.println("创建数据根目录失败.....");
			}
		}
	}

	public void initTaskList() {
		File tasksFile = new File(dirBase, "tasklist.txt");
		if (tasksFile.exists())
			return;

		Writer out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tasksFile)));

			DateTime startDate = new DateTime(2015, 1, 1, 0, 0);
			DateTime endDate = new DateTime(2015, 11, 20, 0, 0);

			DateTime jobDate = startDate;
			while (jobDate.isBefore(endDate)) {
				String jobDateAsText = dayFormat.format(jobDate.toDate());
				initDateDir(jobDateAsText);

				if (!getKLine(SHI, Integer.parseInt(jobDateAsText))) {
					out.write("H " + jobDateAsText + "\n");
					jobDate = jobDate.plusDays(1);
					continue;
				}

				for (Code code : codeList) {
					out.write("T " + jobDateAsText + " " + code.getWindCode() + "\n");
				}

				jobDate = jobDate.plusDays(1);
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

	public void logDayJob(String date, long startTime) {
		File codeFile = new File(dirBase, "log.txt");
		Writer out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(codeFile, true)));

			long endTime = System.currentTimeMillis();
			out.write(date + " " + dateFormat.format(new Date(startTime)) + " " // 
					+ dateFormat.format(new Date(endTime)) + " " + (endTime - startTime) + "\n");
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

	public void initCurrentTask() {
		try {
			allTasks = FileUtils.readLines(new File(dirBase, "tasklist.txt"));

			File taskLogFile = new File(dirBase, "tasklog.txt");

			if (!taskLogFile.exists())
				return;

			String log = FileUtils.readFileToString(taskLogFile);

			if (log == null || log.isEmpty())
				return;

			String[] parts = log.split("");
			nextTaskLine = Integer.parseInt(parts[0]);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	public void logDlTask(String windCode, String date, int nextTaskLine, long startTime) {
		File taskLogFile = new File(dirBase, "tasklog.txt");

		try {
			long endTime = System.currentTimeMillis();
			FileUtils.writeStringToFile(taskLogFile, nextTaskLine + " " + date + " " + windCode + " " + dateFormat.format(new Date(endTime)) + " " + (endTime - startTime) + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("usage:  Wddl ip port user password");
			System.exit(1);
		}

		Wddl d = new Wddl(args[0], Integer.parseInt(args[1]), args[2], args[3]);

		System.out.println("*******************************begin download****************************");

		d.run();

		System.out.println("*******************************end download****************************");
	}
}
