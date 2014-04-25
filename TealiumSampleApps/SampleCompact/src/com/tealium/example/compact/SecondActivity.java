// Copyright (c) 2013 Tealium. All rights reserved.

package com.tealium.example.compact;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.tealium.example.compact.controls.DispatchListView;
import com.tealium.example.compact.controls.DispatchListView.DispatchAdapter;
import com.tealium.library.Tealium;

/**
 * Second Activity used to demonstrate view changes with the Library. 
 * */
public class SecondActivity extends Activity implements OnSeekBarChangeListener {

	// Convenient access of the datasource to add data to it.
	private DispatchAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_second);
		
		
		((SeekBar) this.findViewById(R.id.activitysecond_seekbar))
			.setOnSeekBarChangeListener(this);
		
		DispatchListView listview = ((DispatchListView) this.findViewById(R.id.activitymain_disptachlistview));
		this.adapter = (DispatchAdapter) listview.getAdapter();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Tealium.track(this, null, null);
		
		// Only used with lifecycle tracking when compact.
		// We've disabled lifecycle tracking.
		//Tealium.onResume(this);// Does not need to be added if the minimum API is > 14
		
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
		// Only used with lifecycle tracking when compact.
		// We've disabled lifecycle tracking.
		//Tealium.onPause();// Does not need to be added if the minimum API is > 14
	}

	/* 
	 * Multiple onProgressChanged events can occur within a single gesture,
	 * we'll just wait until we've completed the gesture to grab the progress.
	 * */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	// Gesture has completed.
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
		Tealium.track(seekBar, null, null);
		
		// Update the label.
		((TextView)this.findViewById(R.id.activitysecond_label_progress))
			.setText(Integer.toString(seekBar.getProgress()));
		
		// There must have been a event dispatch created; add it to our queue.
		this.adapter.add(Tealium.getLastDispatch());
	}

}
