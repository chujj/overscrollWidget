package com.example.waterfall;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.ds.pictureviewer.data.PicturesDatabaseOperator;
import com.ds.theard.WorkThread;
import com.ds.widget.ScrollOverPanel;
import com.ds.widget.ScrollOverPanel.IModel;

public class MainActivity extends Activity {

	static ScrollOverPanel mPanel;
	private ColumnListView mModel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		PicturesDatabaseOperator.init(this.getApplicationContext());
		WorkThread.init();

		if (mPanel == null) {
			mModel = new ColumnListView(this);
			mPanel = (new ScrollOverPanel(this, mModel));
			mModel.setDisplayingView(mPanel);
		}

		this.setContentView(mPanel);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mModel.saveLastVisitIndex();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		
		((ViewGroup) mPanel.getParent()).removeView(mPanel);
	}

}
