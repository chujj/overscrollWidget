package com.example.waterfall;

import com.ds.widget.ScrollOverPanel;
import com.ds.widget.ScrollOverPanel.IModel;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.ViewGroup;

public class MainActivity extends Activity {

	static ScrollOverPanel mPanel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mPanel == null) {
			IModel model = new ColumnListView(this);
			mPanel = (new ScrollOverPanel(this, model));
		}

		this.setContentView(mPanel);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		((ViewGroup) mPanel.getParent()).removeView(mPanel);
	}

}
