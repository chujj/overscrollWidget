package com.example.waterfall;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import bdad.AdContainer;

import com.ds.bitmaputils.BitmapGetter;
import com.ds.jni.JniImpl;
import com.ds.pictureviewer.data.PicturesDatabaseOperator;
import com.ds.theard.WorkThread;
import com.ds.widget.ScrollOverPanel;
import com.ds.widget.ScrollOverPanel.IModel;

public class MainActivity extends Activity {

	static AdContainer sRootView;
	static ScrollOverPanel sPanel;
	private ColumnListView mModel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		JniImpl.NativeOnCreate(this);
		PicturesDatabaseOperator.init(this.getApplicationContext());
		WorkThread.init();
		BitmapGetter.setCacheFileDir(this.getFilesDir().getAbsolutePath());

		if (sPanel == null) {
			mModel = new ColumnListView(this);
			sPanel = (new ScrollOverPanel(this, mModel));
			mModel.setDisplayingView(sPanel);
			
			sRootView = new AdContainer(this);
			sRootView.setBottomView(sPanel);

			this.setContentView(sRootView);
		}
	}
}
