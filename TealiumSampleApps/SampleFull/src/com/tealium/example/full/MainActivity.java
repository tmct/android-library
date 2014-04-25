// Copyright (c) 2013 Tealium. All rights reserved.

package com.tealium.example.full;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.tealium.example.full.controls.DispatchListView;
import com.tealium.example.full.controls.DispatchListView.DispatchAdapter;
import com.tealium.library.Tealium;

/**
 * Entry point of the application. 
 * 
 * If we put Tealium.initialize(...) here we would have to check if it already 
 * is initialized using Tealium.getStatus() since Activities are destroyed and 
 * created for any number of reasons. 
 * */
public class MainActivity extends Activity implements View.OnClickListener {

	// Convenient access of the datasource to add data to it.
	private DispatchAdapter adapter; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		DispatchListView listview = ((DispatchListView) this.findViewById(R.id.activitymain_disptachlistview));
		this.adapter = (DispatchAdapter) listview.getAdapter();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Tealium.onResume(this); // Does not need to be added if the minimum API is > 14
		
		/* 
		 * Adding what should be the view track dispatch,
		 * it should be populated within a second.
		 * */ 
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				adapter.add(Tealium.getLastDispatch());	
			}
		}, 1000);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Tealium.onPause(); // Does not need to be added if the minimum API is >= 14
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
		case R.id.activitymain_button_clear:
			this.adapter.clear();
			break;
		case R.id.activitymain_button_next:
			this.startActivity(new Intent(this, SecondActivity.class));
			break;
		}
		
		// A button track must have been generated; add it to the datasource.
		this.adapter.add(Tealium.getLastDispatch());
	}	
}
