package com.example.waterfall;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import bdad.AdContainer;

import com.ds.bitmaputils.BitmapGetter;
import com.ds.io.DsLog;
import com.ds.jni.JniImpl;
import com.ds.pictureviewer.data.PicturesDatabaseOperator;
import com.ds.theard.WorkThread;
import com.ds.widget.ScrollOverPanel;

public class MainActivity extends Activity {

	private static AdContainer sRootView;
	
	private ScrollOverPanel mPanel;
	private ColumnListView mModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		JniImpl.NativeOnCreate(this);
		PicturesDatabaseOperator.init(this.getApplicationContext());
		WorkThread.init();
		BitmapGetter.setCacheFileDir(this.getFilesDir().getAbsolutePath());

		if (sRootView == null) {
			mModel = new ColumnListView(this);
			mPanel = new ScrollOverPanel(this, mModel);
			mModel.setDisplayingView(mPanel);

			sRootView = new AdContainer(this);
			sRootView.setBottomView(mPanel);

			this.setContentView(sRootView);
		}
	}

}
