package com.example.waterfall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.ViewGroup;
import bdad.AdContainer;

import com.ds.bitmaputils.BitmapGetter;
import com.ds.jni.JniImpl;
import com.ds.pictureviewer.data.PicturesDatabaseOperator;
import com.ds.theard.WorkThread;
import com.ds.widget.ScrollOverPanel;

public class MainActivity extends Activity {

	private static AdContainer sRootView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		JniImpl.NativeOnCreate(this);
		PicturesDatabaseOperator.init(this.getApplicationContext());
		WorkThread.init();
		BitmapGetter.setCacheFileDir(this.getFilesDir().getAbsolutePath());

	}
	
	@Override
	protected void onResume() {
		super.onResume();

		if (NetUtils.isNetup(this)) {
			if (!NetUtils.isNetWifi(this)) {
				showNetWarningDialog();
			} else {
				initView();
			}
		} else {
			showNonetDialog();
		}
		
		if (sRootView != null && sRootView.getParent() == null) {
			this.setContentView(sRootView);
		}

	}

	private void showNetWarningDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.not_wifi_waring_title);
		builder.setMessage(R.string.not_wifi_warning_msg);
		builder.setPositiveButton(R.string.not_wifi_warning_accept, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				initView();
				dialog.cancel();
			}
		});
		
		builder.setNegativeButton(R.string.not_wifi_warning_deny, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (MainActivity.sRootView == null) {
					finish();
				} else {
					
				}
				dialog.cancel();
			}
		});
		
		builder.create().show();
	}

	private void showNonetDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.no_net_title);
		builder.create().show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		if (sRootView.getParent() != null) {
			((ViewGroup) sRootView.getParent()).removeView(sRootView);
		}
	}

	private void initView() {
		if (sRootView == null) {
			ColumnListView model = new ColumnListView(this);
			ScrollOverPanel panel = new ScrollOverPanel(this, model);
			model.setDisplayingView(panel);

			sRootView = new AdContainer(this);
			sRootView.setBottomView(panel);

			this.setContentView(sRootView);
		}

	}

}
