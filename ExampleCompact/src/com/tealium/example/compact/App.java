// Copyright (c) 2013 Tealium. All rights reserved.

package com.tealium.example.compact;

import com.tealium.library.Tealium;

import android.app.Application;

/**
 * Overridden Application layer used to initialize the Tealium Library.
 * 
 * This location is ideal when initializing since Applications have multiple 
 * entry points but share the same Application layer.
 * 
 * */
public class App extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		
		/* 
		 * Tealium options used to affect its behavior, in this case we're disabling 
		 * lifecycle and toggling the visibility of some logs. 
		 * */
		int opts = Tealium.OPT_DISABLE_LIFECYCLE_TRACKING | // Don't need lifecycle tracking for this example.
			Tealium.OPT_VOL_INFO | Tealium.OPT_VOL_DEBUG; // Convenient to see logs in LogCat.
	
		// In order to do anything with the Tealium Library, it needs to be initialized.
		Tealium.initialize(this, "your-account", "your-profile", "dev", opts);
		
		// Add global custom data to every dispatch here on out.
		Tealium.addGlobalCustomData(
				Tealium.map("description", "Example application using the full Tealium Library."));
		
	}
}
