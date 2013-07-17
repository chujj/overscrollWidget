package com.ds.bitmaputils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.LogPrinter;
import android.util.Printer;

import com.ds.io.DsLog;
import com.ds.theard.WorkThread;

public class BitmapGetter {
	private static final boolean DEBUG_PERFORMANCE = false;
	private static BitmapGetter sInstance;
	private static String sCacheDirPath;
	
	private WorkHandler mWorkHandler;
	private UIHandler mUIHandler;
	
	private HashMap<Object, Bitmap> mBitmapCache;
	private HashMap<Object, BitmapGotCallBack> mFetchTask;
	private static synchronized BitmapGetter getInstance() {
		if (sInstance == null) {
			sInstance = new BitmapGetter();
		}
		return sInstance;
	}
	
	private Printer mTheradprint;
	private BitmapGetter() {
		mWorkHandler = new WorkHandler();
		mUIHandler = new UIHandler();
		mBitmapCache = new HashMap<Object, Bitmap>();
		mFetchTask = new HashMap<Object, BitmapGotCallBack>();
		mTheradprint = new LogPrinter(android.util.Log.ERROR, "count-dump");
	}

	public static void setCacheFileDir(String aDirPath) {
		sCacheDirPath = aDirPath;
	}
	
	public static void releaseBitmap(BitmapTask aTask) {
		BitmapGetter instance = getInstance();
		Bitmap release = instance.mBitmapCache.get(aTask.getTaskKey());
		if (release != null) {
			release.recycle();
			instance.mBitmapCache.remove(aTask.getTaskKey());
		}
		getInstance().mFetchTask.remove(aTask.getTaskKey());
		instance.releasPending(aTask);
	}
	
	final private void releasPending(BitmapTask aTask) {
		mWorkHandler.removeCallbacksAndMessages(aTask);
	}
	
	public static Bitmap tryGetBitmapFromUrlOrCallback(BitmapTask aTask, BitmapGotCallBack aCallback) {
		
		
		Bitmap retval = getInstance().getCachedBitmap(aTask.getTaskKey());
		if (retval == null) {
			if (getInstance().mFetchTask.containsKey(aTask.getTaskKey())) {
				getInstance().mFetchTask.put(aTask.getTaskKey(), aCallback);
			} else {
				DsLog.e("zhujj: " + aTask.getTaskKey());
				getInstance().fetchBitmapOnNet(aTask, aCallback);
			}
		}
		if (DEBUG_PERFORMANCE) {
			DsLog.e(">>>>>>>>>>>>>>>>>>>>>");
			getInstance().mWorkHandler.dump(getInstance().mTheradprint, "count-dump");
			DsLog.e("<<<<<<<<<<<<<<<<<<<<<");
		}
		return retval;
	}
	
	private Bitmap getCachedBitmap(Object aKey) {
		return mBitmapCache.get(aKey);
	}
	
	private void fetchBitmapOnNet(BitmapTask aTask, BitmapGotCallBack aCallBack) {
		mFetchTask.put(aTask.getTaskKey(), aCallBack);
		Message.obtain(getInstance().mWorkHandler,
				WorkHandler.MSG_FETCH_BITMAP, aTask).sendToTarget();
	}
	
	private static class WorkHandler extends Handler {
		public final static int MSG_FETCH_BITMAP = 0;
		public WorkHandler() {
			super(WorkThread.getsWorkLooper());
		}
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_FETCH_BITMAP:
				BitmapTask task = (BitmapTask) msg.obj;
				Bitmap bitmap = null;
				
				// read local
				bitmap = getBitmapFromFile(task);
				
				if (bitmap == null) {
					//remote
					bitmap = getBitmapFromNet(task.getNetUrl());
					if (bitmap != null) {
						String path2File = saveBitmapToFile(task, bitmap);
						task.saveFileSystemPath(path2File);
					}
				}
				getInstance().mBitmapCache.put(task.getTaskKey(), bitmap);
				Message.obtain(getInstance().mUIHandler, UIHandler.MSG_FETCH_BITMAP_DONE, task).sendToTarget();
				break;

			default:
				break;
			}
		}
		
		private String saveBitmapToFile(BitmapTask aTask, Bitmap aBitmap) {
			if (aBitmap == null) {
				return null;
			}
			
			return saveBitmap2Sdcard(sCacheDirPath, aBitmap);
		}
		private Bitmap getBitmapFromFile(BitmapTask aTask) {
			Bitmap retval = null;
			try {
				if (aTask.getFileSystemPath() != null) {
					retval = BitmapFactory.decodeFile(aTask.getFileSystemPath());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return retval;
		}
		
		private Bitmap getBitmapFromNet(String aUrl) {
			Bitmap retval = null;
			try {
				URL url = new URL(aUrl);
				retval = BitmapFactory.decodeStream(url.openStream());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return retval;
		}
		
	}

	private static class UIHandler extends Handler {
		public final static int MSG_FETCH_BITMAP_DONE = 0;
		public UIHandler() {
			super(Looper.getMainLooper());
		}
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_FETCH_BITMAP_DONE:
				BitmapTask task = (BitmapTask) msg.obj;
				if (getInstance().mFetchTask.containsKey(task.getTaskKey())) {
					BitmapGotCallBack run = getInstance().mFetchTask.get(task.getTaskKey());
					getInstance().mFetchTask.remove(task.getTaskKey());
					if (run != null)
						run.onBitmapGot(getInstance().mBitmapCache.get(task.getTaskKey()));
				}
				break;

			default:
				break;
			}
		}
	}

	public interface BitmapGotCallBack {
		public void onBitmapGot(Bitmap aBitmap);
	}
	
	public interface BitmapTask {
		public Object getTaskKey();
		public String getNetUrl();
		public String getFileSystemPath();
		public void saveNetUrl(String aUrl);
		public void saveFileSystemPath(String aPath);
	}
	
	private static String saveBitmap2Sdcard(String aDirPath, Bitmap bitmap) {
		String filePath = null;
		if (aDirPath == null) {
			return filePath;
		}
		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();

			long time = System.currentTimeMillis();
			filePath = aDirPath + "/" + time;
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(out.toByteArray());
			fos.flush();
			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
			return filePath;
		}
		return filePath;
	}

}
