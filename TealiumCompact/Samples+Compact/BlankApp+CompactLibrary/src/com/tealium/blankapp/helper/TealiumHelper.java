package com.tealium.blankapp.helper;

import java.util.Map;

import com.tealium.library.Tealium;

import android.app.Activity;
import android.app.Application;
import android.view.View;

public final class TealiumHelper {

	public static void initialize(Application application) {
		Tealium.initialize(Tealium.Config.create(application, "tealiummobile", "demo", "dev")
				.setLibraryLogLevel(Tealium.LogLevel.DEBUG));
	}
	
	public static void onResume(Activity activity, Map<String, String> data) {
		Tealium.onResume(activity);
		Tealium.track(activity, data, Tealium.VIEW);
	}
	
	public static void onPause(Activity activity) {
		Tealium.onPause(activity);
	}
	
	public static void trackEvent(View widget, Map<String, String> data) {
		Tealium.track(widget, data, Tealium.EVENT);
	}
	
	public static void trackEvent(Throwable throwable, Map<String, String> data) {
		Tealium.track(throwable, data, Tealium.EVENT);
	}
}
