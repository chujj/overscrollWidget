package com.example.waterfall;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtils {
	private final static int CHECK_INTERVAL = 5 * 60 * 1000;
	private static NetUtils sInstance;

	private boolean isNetUp, isNetWIFI;
	private String mainNetType;
	private String subNetType;

	private static long lastCheckTimeStamp;

	private static NetUtils getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new NetUtils(context);
		}
		if ((System.currentTimeMillis() - lastCheckTimeStamp) > CHECK_INTERVAL) {
			sInstance.checkNet(context);
		}
		return sInstance;
	}

	private NetUtils(Context context) {
		checkNet(context);
	}

	final private void reset() {
		isNetUp = false;
		isNetWIFI = false;
		mainNetType = null;
		subNetType = null;
	}

	public static boolean isNetup(Context context) {
		NetUtils instance = getInstance(context);

		return instance.isNetUp;
	}

	public static boolean isNetWifi(Context context) {
		NetUtils instance = getInstance(context);

		return instance.isNetWIFI;
	}

	private void checkNet(Context context) {
		reset();
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		//		DsLog.v("activeNetInfo=" + activeNetInfo);

		if (activeNetInfo != null) {
			isNetUp = true;
			mainNetType = activeNetInfo.getTypeName().toLowerCase();
			if (mainNetType != null)
				isNetWIFI = mainNetType.toLowerCase().startsWith("wifi");
			subNetType = activeNetInfo.getSubtypeName() + "-" + activeNetInfo.getExtraInfo();
		}
		
		lastCheckTimeStamp = System.currentTimeMillis();
	}
}
