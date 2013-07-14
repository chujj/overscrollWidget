package com.ds.bitmaputils;

import java.net.URL;
import java.util.HashMap;

import com.ds.theard.WorkThread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class BitmapGetter {
	private static BitmapGetter sInstance;
	
	private WorkHandler mWorkHandler;
	private UIHandler mUIHandler;
	
	private HashMap<String, Bitmap> mBitmapCache;
	private HashMap<String, BitmapGotCallBack> mFetchTask;
	private static synchronized BitmapGetter getInstance() {
		if (sInstance == null) {
			sInstance = new BitmapGetter();
		}
		return sInstance;
	}
	
	public BitmapGetter() {
		mWorkHandler = new WorkHandler();
		mUIHandler = new UIHandler();
		mBitmapCache = new HashMap<String, Bitmap>();
		mFetchTask = new HashMap<String, BitmapGotCallBack>();
	}

	
	public static Bitmap tryGetBitmapFromUrlOrCallback(String aUrl, BitmapGotCallBack aCallback) {
		
		Bitmap retval = getInstance().getCachedBitmap(aUrl);
		if (retval == null) {
			if (getInstance().mFetchTask.containsKey(aUrl)) {
				getInstance().mFetchTask.put(aUrl, aCallback);
			} else {
				mylog("zhujj: " + aUrl);
				getInstance().fetchBitmapOnNet(aUrl, aCallback);
			}
		}
		return retval;
	}
	
	private Bitmap getCachedBitmap(String aUrl) {
		return mBitmapCache.get(aUrl);
	}
	
	private void fetchBitmapOnNet(String aUrl, BitmapGotCallBack aCallBack) {
		mFetchTask.put(aUrl, aCallBack);
		Message.obtain(getInstance().mWorkHandler,
				WorkHandler.MSG_FETCH_BITMAP_ON_NET, aUrl).sendToTarget();
	}
	
	private static class WorkHandler extends Handler {
		public final static int MSG_FETCH_BITMAP_ON_NET = 0;
		public WorkHandler() {
			super(WorkThread.getsWorkLooper());
		}
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_FETCH_BITMAP_ON_NET:
				Bitmap bitmap = getBitmapFromNet((String) msg.obj);
				getInstance().mBitmapCache.put((String) msg.obj, bitmap);
				Message.obtain(getInstance().mUIHandler, UIHandler.MSG_FETCH_BITMAP_DONE, msg.obj).sendToTarget();
				break;

			default:
				break;
			}
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
				if (getInstance().mFetchTask.containsKey(msg.obj)) {
					BitmapGotCallBack run = getInstance().mFetchTask.get(msg.obj);
					getInstance().mFetchTask.remove(msg.obj);
					if (run != null)
						run.onBitmapGot(getInstance().mBitmapCache.get(msg.obj));
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
	
	private static void mylog(String aMsg) {
		System.out.println(aMsg);
	}
}
