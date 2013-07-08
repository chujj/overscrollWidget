package com.example.waterfall;

import com.ds.widget.ScrollOverPanel;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(new ScrollOverPanel(this));
		new ColumnListView(this, 400);
	}

}
