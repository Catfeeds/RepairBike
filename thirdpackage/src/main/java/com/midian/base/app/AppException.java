package com.midian.base.app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import android.content.Context;
import android.os.Environment;
import android.os.Looper;

import com.bishilai.thirdpackage.R;
import com.midian.base.afinal.exception.HttpException;
import com.midian.base.util.UIHelper;

/**
 * 应用程序异常类：用于捕获异常和提示错误信息
 */
public class AppException extends Exception implements UncaughtExceptionHandler {

	private static final long serialVersionUID = 1261659675352264735L;

	/**
	 * 是否保存错误日志
	 */
	private final static boolean Debug = true;

	/** 定义异常类型 */
	public final static byte TYPE_NETWORK = 0x01;
	public final static byte TYPE_SOCKET = 0x02;
	public final static byte TYPE_HTTP_CODE = 0x03;
	public final static byte TYPE_HTTP_ERROR = 0x04;
	public final static byte TYPE_JSON = 0x05;
	public final static byte TYPE_IO = 0x06;
	public final static byte TYPE_RUN = 0x07;
	public final static byte TYPE_SERVER = 0x08;

	private byte type;
	private int code;

	/** 系统默认的UncaughtException处理类 */
	private UncaughtExceptionHandler mDefaultHandler;

	private AppException() {
		this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
	}

	private AppException(byte type, int code, Exception excp) {
		super(excp);
		this.type = type;
		this.code = code;
		if (Debug) {
			this.saveErrorLog(excp);
		}
	}

	public int getCode() {
		return this.code;
	}

	public int getType() {
		return this.type;
	}

	/**
	 * 提示友好的错误信息
	 * 
	 * @param ctx
	 */
	public void makeToast(Context ctx) {
		// 打印错误信息
		this.printStackTrace();
		switch (this.getType()) {
		case TYPE_HTTP_CODE:
			String err = ctx.getString(R.string.http_status_code_error, this.getCode());
			UIHelper.t(ctx, err);
			break;
		case TYPE_HTTP_ERROR:
			UIHelper.t(ctx, ctx.getString(R.string.http_exception_error));
			break;
		case TYPE_SOCKET:
			UIHelper.t(ctx, ctx.getString(R.string.socket_exception_error));
			break;
		case TYPE_NETWORK:
			UIHelper.t(ctx, ctx.getString(R.string.network_not_connected));
			break;
		case TYPE_JSON:
			UIHelper.t(ctx, ctx.getString(R.string.xml_parser_failed));
			break;
		case TYPE_IO:
			UIHelper.t(ctx, ctx.getString(R.string.io_exception_error));
			break;
		case TYPE_RUN:
			UIHelper.t(ctx, ctx.getString(R.string.app_run_code_error));
			break;
		case TYPE_SERVER:
			UIHelper.t(ctx, ctx.getString(R.string.server_run_code_error));
			break;
		}
	}

	/**
	 * 提示友好的错误信息
	 * 
	 * @param ctx
	 */
	public String getMessage(Context ctx) {
		// 打印错误信息
		this.printStackTrace();
		String err = "";
		switch (this.getType()) {
		case TYPE_HTTP_CODE:
			err = ctx.getString(R.string.http_status_code_error, this.getCode());
			break;
		case TYPE_HTTP_ERROR:
			try {
				err = ctx.getString(R.string.http_exception_error, this.getCode());
			} catch (Exception e) {
				// TODO: handle exception
			}
			break;
		case TYPE_SOCKET:
			err = ctx.getString(R.string.socket_exception_error, this.getCode());
			break;
		case TYPE_NETWORK:
			err = ctx.getString(R.string.network_not_connected, this.getCode());
			break;
		case TYPE_JSON:
			err = ctx.getString(R.string.xml_parser_failed, this.getCode());
			break;
		case TYPE_IO:
			err = ctx.getString(R.string.io_exception_error, this.getCode());
			break;
		case TYPE_RUN:
			err = ctx.getString(R.string.app_run_code_error, this.getCode());
			break;
		case TYPE_SERVER:
			err = ctx.getString(R.string.server_run_code_error, this.getCode());
			break;
		}
		return err;
	}

	/**
	 * 保存异常日志
	 * 
	 * @param excp
	 */
	public void saveErrorLog(Exception excp) {
		String errorlog = "errorlog.txt";
		String savePath = "";
		String logFilePath = "";
		FileWriter fw = null;
		PrintWriter pw = null;
		try {
			// 判断是否挂载了SD卡
			String storageState = Environment.getExternalStorageState();
			if (storageState.equals(Environment.MEDIA_MOUNTED)) {
				savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/midian/Log/";
				File file = new File(savePath);
				if (!file.exists()) {
					file.mkdirs();
				}
				logFilePath = savePath + errorlog;
			}
			// 没有挂载SD卡，无法写文件
			if (logFilePath == "") {
				return;
			}
			File logFile = new File(logFilePath);
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			fw = new FileWriter(logFile, true);
			pw = new PrintWriter(fw);
			pw.println("\n--------------------" + (new Date().toLocaleString()) + "---------------------\n");
			excp.printStackTrace(pw);
			pw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
				}
			}
		}

	}

	public static AppException http(int code) {
		return new AppException(TYPE_HTTP_CODE, code, null);
	}

	public static AppException http(Exception e) {
		return new AppException(TYPE_HTTP_ERROR, 0, e);
	}

	public static AppException socket(Exception e) {
		return new AppException(TYPE_SOCKET, 0, e);
	}

	public static AppException io(Exception e) {
		if (e instanceof UnknownHostException || e instanceof ConnectException) {
			return new AppException(TYPE_NETWORK, 0, e);
		} else if (e instanceof IOException) {
			return new AppException(TYPE_IO, 0, e);
		}
		return run(e);
	}

	public static AppException json(Exception e) {
		return new AppException(TYPE_JSON, 0, e);
	}

	public static AppException network(Exception e) {
		if (e instanceof UnknownHostException || e instanceof ConnectException) {
			return new AppException(TYPE_NETWORK, 0, e);
		} else if (e instanceof HttpException) {
			return http(e);
		} else if (e instanceof SocketException) {
			return socket(e);
		}
		return http(e);
	}

	public static AppException run(Exception e) {
		return new AppException(TYPE_RUN, 0, e);
	}

	public static AppException server(Exception e) {
		return new AppException(TYPE_SERVER, 0, e);
	}

	/**
	 * 获取APP异常崩溃处理对象
	 */
	public static AppException getAppExceptionHandler() {
		return new AppException();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, ex);
		}

	}

	/**
	 * 自定义异常处理:收集错误信息&发送错误报告
	 * 
	 * @param ex
	 * @return true:处理了该异常信息;否则返回false
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		final Context context = AppManager.getAppManager().currentActivity();

		if (context == null) {
			return false;
		}
		final String crashReport = getCrashReport(context, ex);
		// 显示异常信息&发送报告
		new Thread() {
			public void run() {
				Looper.prepare();

				Looper.loop();
			}

		}.start();
		return true;
	}

	/**
	 * 获取APP崩溃异常报告
	 */
	private String getCrashReport(Context context, Throwable ex) {
		StringBuffer exceptionStr = new StringBuffer();
		exceptionStr.append("Android: " + android.os.Build.VERSION.RELEASE + "(" + android.os.Build.MODEL + ")\n");
		exceptionStr.append("Exception: " + ex.getMessage() + "\n");
		StackTraceElement[] elements = ex.getStackTrace();
		for (int i = 0; i < elements.length; i++) {
			exceptionStr.append(elements[i].toString() + "\n");
		}
		return exceptionStr.toString();
	}
}