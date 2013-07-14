package com.ds.theard;

import android.os.Looper;

public class WorkThread {
	private static Looper sWorkLooper;
	private static Thread sWorkThread;

	public static void init() {
		synchronized(WorkThread.class) {
			if (sWorkThread == null) {
				try {
					sWorkThread = new Thread(new WorkRunnable());
					sWorkThread.setName("workthread");
					sWorkThread.start();
					WorkThread.class.wait();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Looper getsWorkLooper() {
		if (sWorkLooper == null) {
			throw new RuntimeException("Thread not ready, call init");
		}
		return sWorkLooper;
	}

	public static Thread getsWorkThread() {
		if (sWorkThread == null) {
			throw new RuntimeException("Thread not ready, call init");
		}
		return sWorkThread;
	}

	private static class WorkRunnable implements Runnable {

		@Override
		public void run() {
			Looper.prepare();
			sWorkLooper = Looper.myLooper();
			synchronized (WorkThread.class) {
				WorkThread.class.notifyAll();
			}
			Looper.loop();
		}

	}
}
