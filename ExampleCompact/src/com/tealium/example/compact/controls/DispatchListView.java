// Copyright (c) 2013 Tealium. All rights reserved.

package com.tealium.example.compact.controls;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tealium.example.compact.R;
import com.tealium.library.Key;
import com.tealium.library.Tealium;

/**
 * A ListView used in both activities.
 * 
 * It's inherited to display Tealium dispatches. 
 * */
public class DispatchListView extends ListView {
	
	// Singleton data source used by both activities.
	private static DispatchAdapter adapter = null;
	
	/* 
	 * We want to ensure that this.getContext() is populated, 
	 * so this explicit initializer was created.
	*/
	private void initialize() {
		// Initialize the singleton if necessary.
		if(adapter == null) {
			adapter = new DispatchAdapter(
					this.getContext(), 
					R.layout.disptachlistview_item); 
		}
		this.setAdapter(adapter);
	}
	
	// Overloaded constructor.
	public DispatchListView(Context context) {
		super(context);
		this.initialize();
	}
	
	// Overloaded constructor.
	public DispatchListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.initialize();
	}
	
	// Overloaded constructor.
	public DispatchListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.initialize();
	}	

	
	// === classes === //
	
	// Singleton datasource for the listview.
	public static class DispatchAdapter extends ArrayAdapter<JSONObject> implements View.OnClickListener {

		// Only need a single constructor this time.
		public DispatchAdapter(Context context, int resource) {
			super(context, resource);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// Cast to be more usable.
			RelativeLayout item = (RelativeLayout) convertView;
			JSONObject o = this.getItem(position);
			
			// Create the object if it didn't exist before.
			if(item == null) {
				item = (RelativeLayout) LayoutInflater.from(parent.getContext())
						.inflate(R.layout.disptachlistview_item, null);
				item.setOnClickListener(this);
			}
			
			// Store the JSONObject to be viewed with a button click.
			item.setTag(o);
			
			// Set the letter label.
			TextView letterLabel = ((TextView)item.findViewById(R.id.dispatchlistviewitem_label_letter));
			
			String letter = o.optString(Key.CALL_TYPE, null);
			
			if(Tealium.EVENT_NAME_LINK.equals(letter)) {
				letterLabel.setText("L");
				letterLabel.setTextColor(Color.rgb(0, 0, 128));
			} else if (Tealium.EVENT_NAME_VIEW.equals(letter)) {
				letterLabel.setText("V");
				letterLabel.setTextColor(Color.rgb(0, 128, 0));
			} else {
				letterLabel.setText("?");
				letterLabel.setTextColor(Color.rgb(128, 0, 0));
			}			
			
			// Set the remaining labels.
			((TextView)item.findViewById(R.id.dispatchlistviewitem_label_library))
				.setText(o.optString(Key.LIBRARY_VERSION)); 
			((CheckBox)item.findViewById(R.id.dispatchlistviewitem_checkbox_autotracked))
				.setChecked(o.optBoolean(Key.AUTOTRACKED));
			((TextView)item.findViewById(R.id.dispatchlistviewitem_label_class))
				.setText(o.optString(Key.OBJECT_CLASS));
			((TextView)item.findViewById(R.id.dispatchlistviewitem_label_tealiumid))
				.setText(o.optString(Key.TEALIUM_ID));
			((TextView)item.findViewById(R.id.dispatchlistviewitem_label_timestamp))
				.setText(o.optString(Key.TIMESTAMP_LOCAL));
			
			return item;
		}

		/* 
		 * To diminish code size, we put null value checks here. 
		 * 
		 * We want added objects to be put in the first position, 
		 * so it was convenient to simply overload the add() method to do this.
		 * */ 
		@Override
		public void add(JSONObject object) {
			if(object != null) {
				this.insert(object, 0);
			}
		}
		
		// Display the dispatch to the user if an item is clicked.
		@Override
		public void onClick(View view) {
			JSONObject o = (JSONObject) view.getTag();
			String msg;
			try {
				msg = o.toString(4);
			} catch(Throwable t) {
				// This should never happen, track because this would be a WTF kind of failure. 
				msg = o.toString();
				Tealium.track(t, null, null);
			}
			
			// Show the dispatch.
			new AlertDialog.Builder(view.getContext())
				.setMessage(msg)
				.create()
				.show();
		}
	}
}
