package com.ds.io;

import android.util.Log;

public final class DsLog {

	/** 调试总开关 */
	public static final boolean DEBUG = false;
	/** Log TAG */
	public static final String LOG_TAG = "Display";

	/** DEBUG调试开关 */
	public static final boolean DEBUG_DEBUG = false;
	/** ERROR调试开关 */
	public static final boolean DEBUG_ERROR = true;
	/** INFO调试开关 */
	public static final boolean DEBUG_INFO = false;
	/** VERBOSE调试开关 */
	public static final boolean DEBUG_VERBOSE = false;
	/** WARN调试开关 */
	public static final boolean DEBUG_WARN = true;
	/** EXCEPTION调试开关 */
	public static final boolean DEBUG_EXCEPT = true;

	/** TAG过滤掉FILE_TYPE */
	private static final String FILE_TYPE = ".java";

	/**
	 * LogLevel
	 */
	private enum LogLevel {
		/** DEBUG Level */
		DEBUG,
		/** ERROR Level */
		ERROR,
		/** INFO Level */
		INFO,
		/** VERBOSE Level */
		VERBOSE,
		/** WARN Level */
		WARN,
	}

	/**
	 * Constructor
	 */
	private DsLog() {
	}

	/**
	 * DEBUG信息输出
	 * 
	 * @param aMessage
	 *            输出信息
	 */
	public static void d(String aMessage) {
		if (DEBUG && DEBUG_DEBUG) {
			doLog(LogLevel.DEBUG, aMessage, 2, true, null);
		}
	}

	/**
	 * DEBUG信息输出
	 * 
	 * @param aMessage
	 *            输出信息
	 * @param aThrow
	 *            输出异常
	 */
	public static void d(String aMessage, Throwable aThrow) {
		if (DEBUG && DEBUG_DEBUG) {
			doLog(LogLevel.DEBUG, aMessage, 2, true, aThrow);
		}
	}

	/**
	 * ERROR信息输出
	 * 
	 * @param aMessage
	 *            输出信息
	 */
	public static void e(String aMessage) {
		if (DEBUG && DEBUG_ERROR) {
			doLog(LogLevel.ERROR, aMessage, 2, true, null);
		}
	}

	/**
	 * ERROR信息输出
	 * 
	 * @param aMessage
	 *            输出信息
	 * @param aThrow
	 *            输出异常
	 */
	public static void e(String aMessage, Throwable aThrow) {
		if (DEBUG && DEBUG_ERROR) {
			doLog(LogLevel.ERROR, aMessage, 2, true, aThrow);
		}
	}

	/**
	 * INFO信息输出
	 * 
	 * @param aMessage
	 *            输出信息
	 */
	@SuppressWarnings("unused")
	public static void i(String aMessage) {
		if (DEBUG && DEBUG_INFO) {
			doLog(LogLevel.INFO, aMessage, 2, true, null);
		}
	}

	/**
	 * INFO信息输出
	 * 
	 * @param aMessage
	 *            输出信息
	 * @param aThrow
	 *            输出异常
	 */
	@SuppressWarnings("unused")
	public static void i(String aMessage, Throwable aThrow) {
		if (DEBUG && DEBUG_INFO) {
			doLog(LogLevel.INFO, aMessage, 2, true, aThrow);
		}
	}

	/**
	 * VERBOSE信息输出
	 * 
	 * @param aMessage
	 *            输出信息
	 */
	@SuppressWarnings("unused")
	public static void v(String aMessage) {
		if (DEBUG && DEBUG_VERBOSE) {
			doLog(LogLevel.VERBOSE, aMessage, 2, true, null);
		}
	}

	/**
	 * VERBOSE信息输出
	 * 
	 * @param aMessage
	 *            输出信息
	 * @param aThrow
	 *            输出异常
	 */
	@SuppressWarnings("unused")
	public static void v(String aMessage, Throwable aThrow) {
		if (DEBUG && DEBUG_VERBOSE) {
			doLog(LogLevel.VERBOSE, aMessage, 2, true, aThrow);
		}
	}

	/**
	 * WARN信息输出
	 * 
	 * @param aMessage
	 *            输出信息
	 */
	public static void w(String aMessage) {
		if (DEBUG && DEBUG_WARN) {
			doLog(LogLevel.WARN, aMessage, 2, true, null);
		}
	}

	/**
	 * WARN信息输出
	 * 
	 * @param aMessage
	 *            输出信息
	 * @param aThrow
	 *            输出异常
	 */
	public static void w(String aMessage, Throwable aThrow) {
		if (DEBUG && DEBUG_WARN) {
			doLog(LogLevel.WARN, aMessage, 2, true, aThrow);
		}
	}

	/**
	 * Exception信息输出
	 * 
	 * @param e
	 *            输出异常
	 */
	public static void printStackTrace(Exception e) {
		if (DEBUG && DEBUG_EXCEPT) {
			e.printStackTrace();
		}
	}

	/**
	 * @param aLevel
	 *            Log级别
	 * @param aMessage
	 *            要输出的Log信息
	 * @param aStackTraceLevel
	 *            函数调用栈层级
	 * @param aShowMethod
	 *            是否输出调用log的类方法
	 * @param aThrow
	 *            输出异常栈信息
	 */
	private static void doLog(LogLevel aLevel, String aMessage, int aStackTraceLevel, boolean aShowMethod,
			Throwable aThrow) {
		StackTraceElement stackTrace = (new Throwable()).getStackTrace()[aStackTraceLevel];
		String filename = stackTrace.getFileName();
		String methodname = stackTrace.getMethodName();
		int linenumber = stackTrace.getLineNumber();
		//当心！proguard混淆以后getFileName会是一个null值！
		if (filename != null && filename.contains(FILE_TYPE)) {
			filename = filename.replace(FILE_TYPE, "");
		}

		String output = "";
		if (aShowMethod) {
			output = String.format("at (%s:%d)%s: %s", filename, linenumber, methodname, aMessage);
		} else {
			output = String.format("at (%s:%d)%s", filename, linenumber, aMessage);
		}

		switch (aLevel) {
			case DEBUG:
				if (aThrow == null) {
					Log.d(LOG_TAG, output);
				} else {
					Log.d(LOG_TAG, output, aThrow);
				}
				break;
			case ERROR:
				if (aThrow == null) {
					Log.e(LOG_TAG, output);
				} else {
					Log.e(LOG_TAG, output, aThrow);
				}
				break;
			case INFO:
				if (aThrow == null) {
					Log.i(LOG_TAG, output);
				} else {
					Log.i(LOG_TAG, output, aThrow);
				}
				break;
			case VERBOSE:
				if (aThrow == null) {
					Log.v(LOG_TAG, output);
				} else {
					Log.v(LOG_TAG, output, aThrow);
				}
				break;
			case WARN:
				if (aThrow == null) {
					Log.w(LOG_TAG, output);
				} else {
					Log.w(LOG_TAG, output, aThrow);
				}
				break;
			default:
				break;
		}
	}
}
