package com.example.waterfall;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.ds.theard.WorkThread;
import com.ds.widget.ScrollOverPanel;
import com.ds.widget.ScrollOverPanel.IModel;

public class MainActivity extends Activity {

	static ScrollOverPanel mPanel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		WorkThread.init();
		if (mPanel == null) {
			ColumnListView model = new ColumnListView(MainActivity.this);
			mPanel = (new ScrollOverPanel(MainActivity.this, model));
			model.setDisplayingView(mPanel);
		}

		this.setContentView(mPanel);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		((ViewGroup) mPanel.getParent()).removeView(mPanel);
	}

}
