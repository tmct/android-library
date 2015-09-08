package com.tealium.example.helper;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import com.tealium.example.BuildConfig;
import com.tealium.library.Tealium;
import com.tealium.library.Tealium.Config;
import com.tealium.library.Tealium.LogLevel;

public final class TealiumHelper {

	private final static String KEY_TEAL_INIT_COUNT = "tealium_init_count";
	private final static String TAG = "TealiumHelper";
	
	// Not instantiatable.
	private TealiumHelper () {}
	
	@SuppressLint("NewApi")
	public static void initialize(Application application) {
		Log.i(TAG, "initialize(" + application.getClass().getSimpleName() + ")");
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
			WebView.setWebContentsDebuggingEnabled(true);
		}
		
		Tealium.initialize(Config.create(application, "tealiummobile", "demo", "dev")
			.setHTTPSEnabled(false)
			.setLibraryLogLevel(LogLevel.VERBOSE));
		
		SharedPreferences sp = Tealium.getGlobalCustomData();
		sp.edit().putInt(KEY_TEAL_INIT_COUNT, sp.getInt(KEY_TEAL_INIT_COUNT, 0) + 1).commit();
	}
	
	public static void onResume(Activity activity) {
		Log.i(TAG, "onResume(" + activity.getClass().getSimpleName() + ")");
		Tealium.onResume(activity);
	}
	
	public static void onPause(Activity activity) {
		Log.i(TAG, "onPause(" + activity.getClass().getSimpleName() + ")");
		Tealium.onPause(activity);
	}
}
