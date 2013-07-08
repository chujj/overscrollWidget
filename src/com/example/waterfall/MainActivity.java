package com.example.waterfall;

import com.ds.widget.ScrollOverPanel;
import com.ds.widget.ScrollOverPanel.IModel;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IModel model = new ColumnListView(this);
		this.setContentView(new ScrollOverPanel(this, model));
	}

}
