package com.ds.jni;

import android.content.Context;

public class JniImpl {
	private static final String LIBNAME = "dsjni";
	
	static {
		try {
			System.loadLibrary(LIBNAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static native void NativeOnCreate(Context context);
}
