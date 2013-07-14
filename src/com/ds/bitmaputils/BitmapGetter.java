package com.ds.bitmaputils;

import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapGetter {
	public static Bitmap getBitmapFromNet(String aUrl) {
		Bitmap retval = null;
		try {
			URL url = new URL(aUrl);
			retval = BitmapFactory.decodeStream(url.openStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retval;
	}

	public static Bitmap tryGetBitmapFromUrlOrCallback(String aUrl, Runnable aCallback) {
		return null;
	}
}
