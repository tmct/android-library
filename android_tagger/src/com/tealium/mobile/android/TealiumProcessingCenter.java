package com.tealium.mobile.android;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.Surface;
//import android.graphics.Point;

/**
 * 
 * <p>This class handles all the globally auto tracked data sources. 
 * There is normally no need to instantiate or access it's methods directly.</p>
 */
public class TealiumProcessingCenter {

	// Added 2013-25-09 by Chad Hartman @Tealium
	// Key used to access the per-install UUID of the app's
	//	default shared preferences.
	private final String SHARED_PREF_KEY_UUID = "com.tealium.tealiumtagger.uuid"; 
	
	// PROPERTIES
	Activity activity;
	Context context;
	
	// CONSTRUCTOR
	/*
	 * An instance of the Tealium Processing Center is created automatically by Tealium Tagger.
	 * This class handles the assembly of the automatic data sources that are packaged with all
	 * tracking dispatches.
	 */
	public TealiumProcessingCenter(Activity aActivity, TealiumTagger tealiumTagger) {
		activity = aActivity;
		context = activity.getApplicationContext();
	}
	
	// PUBLIC 
	/**
	 * This method returns all the auto assembled data source objects that remain the same throughout
	 * the app's life.  This could be called manually if you wish to inspect or use one of the key-value 
	 * pairs for your own operations.  Data sources assembled here will be overwritten by key-value 
	 * pairs with the same key from the trackItemClicked and trackScreenViewed methods' dictionary argument.
	 */
	public Map<String, String> staticDataSources(){
		Map<String, String> map = new HashMap<String, String>();
		
		Display display = activity.getWindowManager().getDefaultDisplay();

		String appVers = getAppVersion(context); 
		String appName = getAppName(context);
		String deviceName = getDeviceName();
		String deviceResolution = getDeviceResolution(display);
		String osVersion = "" + android.os.Build.VERSION.SDK_INT;
		String uuid = getUuid();
		String libraryVer = "1.2";
		
		if (appName != null  && appVers != null)			map.put("app_id", appName + " " + appVers);
		if (appName != null && appName != "") 				map.put("app_name", appName);
		if (appVers != null && appVers != "") 				map.put("app_version", appVers);
		if (deviceName != null && deviceName != "") 		map.put("device", deviceName);
		if (deviceResolution != null && deviceResolution != "")	map.put("device_resolution", deviceResolution);
															map.put("platform", "android");
		if (osVersion != null && osVersion != "")			map.put("os_version", osVersion);
		if (uuid != null)									map.put("uuid", uuid);
		if (libraryVer != null)								map.put("library_version", libraryVer);
		
		return map;
	}
	
	/**
	 * This method returns all the auto assembled data source objects that can change throughout
	 * the app's life.  This could be called manually if you wish to inspect or use one of the key-value 
	 * pairs for your own operations.  Data sources assembled here will be overwritten by key-value 
	 * pairs with the same key from the trackItemClicked and trackScreenViewed methods' dictionary argument.
	 */
	public Map<String, String> dynamicDataSources(){
		Map<String, String> map = new HashMap<String, String>();
		
		TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		Display display = activity.getWindowManager().getDefaultDisplay();
		
		String carrier = manager.getNetworkOperatorName();
		String carrierISO = manager.getNetworkCountryIso();
		String carrierMNC = getMNC(manager);
		String connectionType = getNetwork(context);
		String orientation = getOrientation(display);
		Date now = new Date();
		String timestamp = getTimestampAsISO(now);
		String timestampUnix = getTimestampUnix();
		
		if (carrier != null && carrier != "") 				map.put("carrier", carrier);
		if (carrierISO != null && carrierISO != "") 		map.put("carrier_iso", carrierISO);
		if (carrierISO != null && carrierISO != "") 		map.put("carrier_mcc", carrierISO);
		if (carrierMNC != null && carrierMNC != "") 		map.put("carrier_mnc", carrierMNC);
		if (connectionType != null && connectionType != "") map.put("connection_type", connectionType);
		if (orientation != null && orientation != "")		map.put("orientation", orientation);
															map.put("platform", "android");
		if (timestamp != null && timestamp != "")			map.put("timestamp", timestamp);
		if (timestamp != null)								map.put("timestamp_unix", timestampUnix);
		
		return map;
	}
	
	private String getTimestampUnix(){
		String string = null;
		int unix = (int) System.currentTimeMillis()/1000;
		if (unix >= 0) string = "" + unix;
		return string;
	}
	
	@SuppressLint("SimpleDateFormat")
	private String getTimestampAsISO(Date date){
		TimeZone tz = TimeZone.getTimeZone("UTC");
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    df.setTimeZone(tz);
	    String nowAsISO = df.format(date);
	    return nowAsISO;
	}
	
//	private String getTimestampGMT(){
//		// *** Android date format ***
//		// ie: Wed Jul 1- 15:32:50 PDT 2013
//		Date date = new Date();
//		String string = date.toString();		
//		return string;
//	}
	
	private String getOrientation(Display display)
	{
           final int rotation = display.getRotation();
		   switch (rotation) {
            case Surface.ROTATION_0:
                return "Portrait";
            case Surface.ROTATION_90:
                return "Landscape Right";
            case Surface.ROTATION_180:
                return "Portrait UpsideDown";
            default:
                return "Landscape Left";
            }
	}
	
	@SuppressWarnings("deprecation")
	private String getDeviceResolution(Display display){
		int width = 0;
		int height = 0;
//		if (Build.VERSION.SDK_INT >= 13){
//			
//			Point size = new Point();
//			display.getSize(size);
//			width = size.x;
//			height = size.y;
//		} else {
			width = display.getWidth();
			height = display.getHeight();
//		}
		
		String string = "";
		if (width > 0 && height > 0) string = width + "x" + height;
		return string;
	}
    
	private String getAppName(Context context){
		Resources resources = context.getResources();
		CharSequence seq = resources.getText(resources.getIdentifier("app_name", "string", context.getPackageName()));
		String string = seq.toString();
		return string;
	}
	
	private String getAppVersion(Context context){
		String appVers = null;
		try {
			appVers = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return appVers;
	}
	
	private String getMNC(TelephonyManager manager){
		// MNC must be parsed from operator name
		String no = manager.getNetworkOperator();
		String s3 = no.substring(3, no.length());
		return s3;
	}
	
	private String getNetwork(Context context)
    {
		String network_type="UNKNOWN"; //maybe usb reverse tethering
		NetworkInfo active_network=((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (active_network!=null && active_network.isConnectedOrConnecting())
		{if (active_network.getType()==ConnectivityManager.TYPE_WIFI)
            {network_type="WIFI";
            }
         else if (active_network.getType()==ConnectivityManager.TYPE_MOBILE)
              {network_type=((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().getSubtypeName();
              }
        }
     return network_type;
    }
	
	private String getDeviceName() {
		  String manufacturer = Build.MANUFACTURER;
		  String model = Build.MODEL;
		  if (model.startsWith(manufacturer)) {
		    return capitalize(model);
		  } else {
		    return capitalize(manufacturer) + " " + model;
		  }
		}


	private String capitalize(String s) {
	  if (s == null || s.length() == 0) {
	    return "";
	  }
	  char first = s.charAt(0);
	  if (Character.isUpperCase(first)) {
	    return s;
	  } else {
	    return Character.toUpperCase(first) + s.substring(1);
	  }
	} 
		
	private String getUuid(){

		// Modified by Chad Hartman @Tealium.
		// Now a UUID is randomly generated and stored in Shared Preferences. 
		// This reference is recreated if:
		// - The user clears their "App Data"
		// - The user installs a fresh copy. (Uninstall clears the shared preference)
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		
		String uuid = prefs.getString(SHARED_PREF_KEY_UUID, null);

		// Either the app is a fresh install, or the user cleared their app data.
		if (uuid == null) {
			uuid = UUID.randomUUID().toString();
			prefs.edit().putString(SHARED_PREF_KEY_UUID, uuid).commit();
		}
		return uuid;
	}
	
}
