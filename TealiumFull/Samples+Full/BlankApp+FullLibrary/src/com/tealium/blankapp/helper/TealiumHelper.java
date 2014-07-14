package com.tealium.blankapp.helper;

import android.app.Activity;
import android.app.Application;

import com.tealium.library.Tealium;

public final class TealiumHelper {

	public static void initialize(Application application) {
		Tealium.Config config = Tealium.Config.create(application, "tealiummobile", "demo", "dev")
				.setLibraryLogLevel(Tealium.LogLevel.VERBOSE);
		
		Tealium.initialize(config);
	}
	
	public static void onResume(Activity activity) {
		Tealium.onResume(activity);
	}
	
	public static void onPause(Activity activity) {
		Tealium.onPause(activity);
	}
}
