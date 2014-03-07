package com.tealium.mobile.android;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import com.tealium.mobile.android.TealiumLifecycle.CallTypes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * 
 * <h2>Version 1.2</h2>
 * 
 * <h2>Introduction</h2>
 * 
 * <p>This library provides Tealium customers the means to tag their native
 * Android applications for the purpose of leveraging the vendor-neutral tag management
 * platform offered by Tealium.</p>
 * 
 * <p>It provides:</p>
 * <ul>
 * <li>web-analytics integration via the Tealium platform</li>
 * <li>Android view (activity) tracking, similar to traditional web page tracking, utilizing your favorite analytics vendor. The name of the activity will be used as the 'page name'</li>
 * <li>intelligent network-sensitive caching</li>
 * <li>custom action tracking</li>
 * <li>implemented with the user in mind. All tracking calls are asynchronous as to not interfere or degrade the user experience. </li>
 * </ul>
 * 
 * <h2>Tealium Requirements</h2>
 * 
 * <p>Ensure an active Tealium account exists. You will need the following items:</p>
 * 
 * <ul>
 * <li>Your Tealium Account Id (it will likely be your company name)</li>
 * <li>The Tealium Profile name to be associated with the app (your account may have several profiles, ideally one of them is dedicated to your Android app)</li>
 * <li>The Tealium environment to use (this will be one of these values: 'prod', 'qa', or 'dev')</li>
 * </ul>
 * 
 *  <h2> Requirements </h2>
 *  This Android library requires Android 2.2 (API Level 8) and above.
 *
 *  <h2>Installation - Source</h2>
 *  If directly compiling source is preferred, simply place this source file
 *  into your source path, in the proper package directory.
 *  
 *  <h2>Installation - Jar Library</h2>
 *  If a binary jar file is preferred, get the jar file from the github repo
 *  located at github.com/Tealium/android-tagger and include it utilizing 
 *  your preferred project management strategy (ant, maven, etc). 
 *  <h2>
 *  
 *  <h2>Installation - Use Permissions</h2>
 *  The library requires the following permission be granted in the app manifest file:
 *  <ul>
 *  <li>ACCESS_NETWORK_STATE</li>
 *  <li>INTERNET</li>
 *  </ul>
 *  <h2>
 *  
 * <h2>Usage</h2>
 * An instance of the TealiumTagger must exist *per* Activity. The TealiumTagger instance must
 * be created within the activity's onCreate() lifecycle method, while also calling each of the
 * other lifecycle methods, as followed:
 * <pre>
 * 
 * public void onCreate() {
 * 	//...your custom activity initialization code
 * 
 * 	//remember, the account and profile's must already exist in Tealium
 * 	this.tealiumTagger = new TealiumTagger(this, "company_name", "android_app_name", "dev");
 * }
 * 
 * public void onPause() {
 * 	//...your custom activity pause code
 * 
 * 	this.tealiumTagger.onPause();
 * }
 * 
 * public void onResume() {
 * 	//...your custom activity resume code
 * 
 * 	this.tealiumTagger.onResume();
 * }
 * 
 * public void onDestroy() {
 * 	//...your custom activity destroy code
 * 
 * 	this.tealiumTagger.onDestroy();
 * }

 * </pre>
 * 
 * <h2>Extended Usage</h2>
 * In addition to the auto-tracking features provided by the lifecycle methods above, the TealiumTagger 
 * also exposes two additional methods to be utilized for more fine grained tracking. 
 * 
 * <h4>Dialog Tracking</h4>
 * One of the two additional methods is trackScreenViewed(). This can be called whenever a sub view is displayed, 
 * whether it is a tab page, a dialog (modal or not), etc. An example usage is as followed:
 * <pre>
 * 
 * protected void onPrepareDialog(int id, Dialog dialog) {
 * 		String dialogName = ...//determine the name of this dialog
 * 		super.onPrepareDialog(id, dialog);
 * 		
 * 		this.tealiumTagger.trackScreenViewed(dialogName);
 * }
 * 
 * </pre>
 * 
 * <h4>Custom Variables</h4>
 * This API supports passing custom name/value pairs to the utag platform. The Map<String,String> variables
 * constructor parameter will be set as an internal instance variable, and passed with each subsequent call into the
 * utag platform. You can modify any or all these variables using the setVariables() or setVariable() methods. 
 * 
 * Additionally, each tracking method allows the caller to override these variables just for the single call. 
 *  
 * <h4>Item Click Tracking</h4>
 * The next additional method is trackItemClicked(). This can be called any click action occurs, 
 * and tracking is desirable.
 * 
 * An example usage is as followed:
 * <pre>
 * //...
 * 	item.setOnClickListener(new OnClickListener() {
 * 
 * 			public void onClick(View v) {
 * 				//..custom click handling code
 * 				String itemClickedName = //..determine the name to use for the item
 * 				this.tealiumTagger.trackItemClicked(itemClickedName);
 * 			}
 * 		});
 * //...
 * </pre>
 *
 */

// Created by Charles Glommen
// Extended by Jason Koo

public final class TealiumTagger {

	private static final String TAG = "TEALIUM TAGGER";
	private final Activity activity;
	private final String account;
	private final String profile;
	private final String environment;
	private final Map<String, String> baseVariables;
	private final Handler connectionCheckHandler = new Handler();
	private final Handler queueFlushHandler = new Handler();
	private LinkedList<String> queue;
	private ConcurrentHashMap<String, String> providedVariables;
	private WebView webView;
	private boolean tealiumEnabled;
	private boolean webViewIsReady;
	private boolean hasInternetConnection;
	private boolean loadingVarsReady;
	private boolean queueFlushTimerIsRunning;
	
	private TealiumProcessingCenter processingCenter;
	public TealiumLifecycle lifecycle;

	
	/** 
	 * An instance of TealiumTagger must be created during the onCreate() method of all activities.
	 * Please keep a reference to this tagger instance in your activity, so that it can be used
	 * in other lifecycle methods (and to prevent it from being garbage collected). 
	 * 
	 * NOTE: the creation of this instance assumes the activity will be displayed, and will 
	 * invoke a traditional 'page view' request into the utag platform.
	 * 
	 * OPTIONAL: if you wish to delay the initialization sequence for some additional process to complete
	 * then use the TealiumTagger(Activity, AccountString, ProfileString, TargetString, Map, ReadyBoolean)
	 * method.
	 * 
	 * @param activity - The activity in which this instance is being instantiated
	 * @param account - The tealium account name (likely your company name)
	 * @param profile - The tealium profile being associated with this android app. 
	 * Best practice is to create a profile exclusively for this android app. 
	 * @param environment - must be one of these values: 'dev', 'prod', or 'qa'.
	 */
	public TealiumTagger(Activity activity, String account, String profile, String environment) {
		this(activity, account, profile, environment, null, true);
	}
	
	/** 
	 * This method calls TealiumTagger(Activity, String, String, String) while also allowing 
	 * a custom set of name/value pairs to be passed with each call into the utag platform.   
	 */
	public TealiumTagger(Activity activity, String account, String profile, String environment,
			Map<String, String> variables) {
		this(activity, account, profile, environment, variables, true);
	}
	

	/** 
	 * This method calls TealiumTagger(Activity, String, String, String) while also allowing 
	 * a custom set of name/value pairs to be passed with each call into the utag platform and
	 * provides for the optional ready flag for optionally delay the wake sequence (and the default auto track view call)
	 * for some other process to complete. If the "ready" argument is "false" then the loadingAutotrackScreenViewReady
	 * call MUST be made when the desired data has become available to complete the wake sequence.   
	 */
	public TealiumTagger(Activity activity, String account, String profile, String environment,
			Map<String, String> variables, boolean ready) {
		this.activity = activity;
		this.account = account;
		this.profile = profile;
		this.environment = environment;
		this.loadingVarsReady = ready;
		this.processingCenter = new TealiumProcessingCenter(this.activity, this);
		this.lifecycle = new TealiumLifecycle(this.activity, this);
		baseVariables = new HashMap<String, String>();
		baseVariables.putAll(this.processingCenter.staticDataSources());
		setVariables(variables);
		try {
			initializeWebView();
			wakeUp();
		}
		catch (Exception e) {
			e.printStackTrace();
		}			
	}
	

	/**
	 * This method resets the variables to be passed with each subsequent utag call.
	 * 
	 * NOTE: if it is desirable to pass custom variables to the utag platform with the initial 'page view' 
	 * occurring at the time of the initial display of the activity's view content, ensure the constructor 
	 * is passed these variables, since the constructor will initiate an automatic call into the utag platform. 
	 */
	public void setVariables(Map<String, String> variables) {
		if (variables == null) {
			providedVariables = null;
			return;
		}
		providedVariables = new ConcurrentHashMap<String, String>(variables);
	}

	/**
	 * This method changes the value of a single variable to be passed with each subsequent utag call.
	 * Pass null as the value to remove a name/value pair from the set to be passed to utag. 
	 */
	public void setVariable(String name, String value) {
		if (providedVariables == null)
			providedVariables = new ConcurrentHashMap<String, String>();
		providedVariables.put(name, value);
	}

	/**
	 * This method is for adding additional key-value pairs of data to all utag calls from the activity
	 * called from.
	 * @param variables String Map of data to add
	 */
	public void addVariables(Map<String, String> variables){
		if (variables == null) return;
		if(providedVariables == null) providedVariables = new ConcurrentHashMap<String, String>(variables);
		else providedVariables.putAll(variables);
	}
		
	public void loadingAutotrackScreenViewReady(){
		// Delayed wake up feature provided by Brian Finamore
		if(loadingVarsReady) 
		return;
		loadingVarsReady = true;
		wakeUp();
	}
		
		
	/**
	 * This method must be called in your activity's onPause() method. 
	 * 
	 * Calling this method will allow Tealium to persist the internal request queue for later processing.  
	 */
	public void onPause() {
		sleep();
	}

	/**
	 * This method must be called in your activity's onResume() method.
	 * 
	 * Calling this method will allow any queued requests to be loaded and potentially delivered. 
	 */
	public void onResume() {
		wakeUp();	
	}

	/**
	 * This method must be called in your activity's onDestroy() method.
	 * 
	 * Calling this method will ensure all tealium activity ceases and properly cleaned up. 
	 */
	public void onDestroy() {
		sleep();
	}
	
	
	/**
	 * This method should only be called once when your application starts, either from your application's onCreate method
	 * or your initial activity's onCreate method.
	 * 
	 * All the lifecycle tracking methods (applicationCreate, applicationResume, applicationPause, applicationDestroy)
	 * should be called by your application subclass or by your main activity if
	 * you application has only one activity.  In the event you have multiple activities, these methods may have to 
	 * be wrapped in methods that first check to see if your application's activities are active or being masked
	 * by another application's activities. Note: Successive calls of the same type are automatically suppressed.
	 * So, if applicationCreate() is called by multiple methods, only the first call will be accepted until another
	 * application lifecycle method is called (applicationResume(), applicationPause(), applicationDestroy()).
	 */
	public void applicationCreate(){
		Map<String,String> lifecycleData = lifecycle.lifecycleDataForType(CallTypes.CREATE);
		if (lifecycleData != null) trackItemClicked("lifecycle", lifecycleData);	}
	
	/**
	 * 
	 * Call this method when your application returns to the foreground. 
	 * Note: Successive applicationResume() calls are automatically suppressed until
	 * another application lifecycle call is made.
	 */
	public void applicationResume(){
		Map<String,String> lifecycleData = lifecycle.lifecycleDataForType(CallTypes.RESUME);
		if (lifecycleData != null) trackItemClicked("lifecycle", lifecycleData);	}
	
	/**
	 * Call this method when your application goes into the background.
	 * Note: Successive applicationPause() calls are automatically suppressed until
	 * another application lifecycle call is made.
	 */
	public void applicationPause(){
		Map<String,String> lifecycleData = lifecycle.lifecycleDataForType(CallTypes.PAUSE);
		if (lifecycleData != null) trackItemClicked("lifecycle", lifecycleData);
	}
	
	/**
	 * Call this method when your application is being destroyed.
	 * Note: Successive applicationDestroy() calls are automatically suppressed until
	 * another application lifecycle call is made.
	 */
	public void applicationDestroy(){
		Map<String,String> lifecycleData = lifecycle.lifecycleDataForType(CallTypes.DESTROY);
		if (lifecycleData != null) trackItemClicked("lifecycle", lifecycleData);	}

	/**
	 * Use this method if tracking clicks on objects is desirable. Call this method 
	 * within any click handler code and pass a non-null item name.  
	 * @param itemName the name of the button, image, layout or other object being clicked
	 */
	public void trackItemClicked(String itemName) {
		trackItemClicked(itemName, null);
	}

	/**
	 * This call is similar to trackItemClicked(String) except it allows any custom variables to be passed.
	 * 
	 * NOTE: the variables parameter will be utilized to override those passed into the constructor 
	 * or the setVariables() method. 
	 * 
	 * @param itemName  the name of the item to be tracked. This is similar to a 'link' being tracked
	 * by traditional web analytics
	 * @param variables any set of name/value pairs that need to be passed to the utag platform.
	 */
	public void trackItemClicked(String itemName, Map<String, String> variables) {
		if (variables == null) {
			variables = new HashMap<String, String>();
		}
		variables.put("link_id", itemName);
		trackCustomEvent("link", variables);
	}

	/**
	 * This method can be used to track the display of sub-screens within a single activity, such as tab content 
	 * or a dialog. 
	 *
	 * NOTE: do NOT call this method to track the display of the Activity itself, as this is already performed by 
	 * the lifecycle methods automatically.
	 *  
	 * @param viewName the name of the screen. This is similar to a 'page' in traditional web analytics
	 */
	public void trackScreenViewed(String viewName) {
		trackScreenViewed(viewName, null);
	}

	/**
	 * This call is similar to trackScreenViewed(String) except it allows any custom variables to be passed.
	 * NOTE: the variables parameter will be utilized to override those passed into the constructor 
	 * or the setVariables() method. 
	 * 
	 * @param viewName the name of the screen. This is similar to a 'page' in traditional web analytics
	 * @param variables any set of name/value pairs that need to be passed to the utag platform.
	 */
	public void trackScreenViewed(String viewName, Map<String, String> variables) {
		if (variables == null) {
			variables = new HashMap<String, String>();
		}
		variables.put("screen_title", viewName);
		trackCustomEvent("view", variables);
	}

	public void trackCustomEvent(String eventName, Map<String, String> variables) {
		if (!tealiumEnabled) {
			Log.i(TAG,
					"A Tealium Tagger track message was called, but this tealium object has already been put to sleep. Maybe you just forgot to send the 'wakeUp' message to the tealium instance?");
			return;
		}

		Map<String, String> variablesToSend = new HashMap<String, String>();
		variablesToSend.putAll(processingCenter.dynamicDataSources());
		variablesToSend.putAll(baseVariables);
		if (providedVariables != null) {
			variablesToSend.putAll(providedVariables);
		}
		if (variables != null) {
			variablesToSend.putAll(variables);
		}
		JSONObject json = new JSONObject(variablesToSend);
		String jsonStr = json.toString();
		String utagCommand = "javascript:utag.track('" + eventName + "'," + jsonStr
				+ ", function() {TealiumTaggerCallback.callback();});";
		synchronized (queue) {
			queue.add(utagCommand);
		}
		trySendingQueuedUTagItems();
	}

	private synchronized void sleep() {
		if (!tealiumEnabled) {
			return;
		}
		tealiumEnabled = false;
		Log.d(TAG, "going to sleep by persisting " + queue.size() + " queued items");
		StringBuilder builder = new StringBuilder();
		for (String queueItem : queue) {
			if (builder.length() > 0) {
				builder.append('\n');
			}
			builder.append(queueItem);

		}
		queue.clear();
		SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(activity);
		Editor storageEditor = storage.edit();
		storageEditor.putString("_tealium_cache_", builder.toString());
		storageEditor.commit();
		
	}

	private synchronized void wakeUp() {
		if (tealiumEnabled) {
			return;
		}
		if (!loadingVarsReady){
			return;
		}

		SharedPreferences storage = PreferenceManager.getDefaultSharedPreferences(activity);
		String cacheStr = storage.getString("_tealium_cache_", null);
		if (cacheStr == null) {
			cacheStr = "";
		}
		cacheStr = cacheStr.trim();
		if (cacheStr.length() == 0) {
			queue = new LinkedList<String>();
		}
		else {
			String[] cacheItems = cacheStr.split("\n");
			queue = new LinkedList<String>(Arrays.asList(cacheItems));
		}
		tealiumEnabled = true;

		Log.d(TAG, "woke up and loaded " + queue.size() + " persisted queue items");

		Log.d(TAG, "automatically firing a page view for this activity");
		trackScreenViewed(activity.getTitle().toString());
		detectNetworkConnection();

	}

	public void callback() {
		System.out.println("CALLBACK worked");
	}

	@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
	private void initializeWebView() {
		webView = new WebView(activity);
		webView.setEnabled(true);
		webView.addJavascriptInterface(this, "TealiumTaggerCallback");
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {

			private int webViewPreviousState;
			private final int PAGE_STARTED = 0x1;
			private final int PAGE_REDIRECTED = 0x2;

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
				webViewPreviousState = PAGE_REDIRECTED;
				view.loadUrl(urlNewString);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				webViewPreviousState = PAGE_STARTED;
			}

			@Override
			public void onPageFinished(WebView view, String url) {

				if (webViewPreviousState == PAGE_STARTED) {
					if (!webViewIsReady) {
						webViewIsReady = true;
						Log.d(TAG, "the web view has been initialized");
						trySendingQueuedUTagItems();
					}
				}
			}
		});

		webView.loadUrl("https://tags.tiqcdn.com/utag/" + account + "/" + profile + "/" + environment
				+ "/mobile.html");
	}
	
	/**
	 * Zeros out lifecycle log data.
	 */
	public void resetLifecycleLog(){
		lifecycle.resetLifecycleLog();
	}

	private synchronized void scheduleNextNetworkDetection() {
		if (!tealiumEnabled) {
			Log.d(TAG, "not going to check for network connectivity");
			return;
		}

		connectionCheckHandler.postDelayed(new Runnable() {
			
			public void run() {
				detectNetworkConnection();
			}

		}, 2000);

	}

	private synchronized void detectNetworkConnection() {
		boolean didHaveConnection = hasInternetConnection;
		hasInternetConnection = hasInternetConnection();

		//if we just regained connection, try flushing the queue
		if (!didHaveConnection && hasInternetConnection) {
			Log.d(TAG, "regained network connectivity, will try to flush the queue");
			trySendingQueuedUTagItems();
		}
		else {
			queueFlushTimerIsRunning = false;
			if (didHaveConnection && !hasInternetConnection) {
				Log.d(TAG, "lost network connectivity");
			}
		}
		scheduleNextNetworkDetection();
	}

	private boolean hasInternetConnection() {
		ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable()
				&& cm.getActiveNetworkInfo().isConnected()) {
			return true;
		}
		else {
			return false;
		}
	}

	private void trySendingQueuedUTagItems() {
		//first check if the timer is already running
		//If so, just exit so it doesn't double up on itself
		if (queueFlushTimerIsRunning) {
			Log.d(TAG, "queue flush timer is already running, don't want to overlap it.");

			return;
		}

		Log.d(TAG, "Will schedule next item to send if all true: Enabled-" + tealiumEnabled + ":"
				+ "Connected-" + hasInternetConnection + ":" + "Webview-" + webViewIsReady + ":" + "Queue Ready-" + (queue!=null? "true":"false"));

		//start the background timer if the network is reachable and there
		//are events queued. This timer will quickly send all the pixels in a controlled manner.
		if (tealiumEnabled && hasInternetConnection && webViewIsReady && (queue != null) && queue.size() > 0) {
			scheduleNextQueuedUTagItem();
		}
	}

	private void scheduleNextQueuedUTagItem() {
		if (queue == null || queue.size() == 0) {
			queueFlushTimerIsRunning = false;
			return;
		}
		queueFlushTimerIsRunning = true;
		Log.d(TAG, "scheduling next queue tag item to be removed");
		queueFlushHandler.postDelayed(new Runnable() {

			public void run() {
				if (queue == null || queue.size() == 0) {
					queueFlushTimerIsRunning = false;
					return;
				}

				//pop the first item off the queue
				String itemToSend = null;
				synchronized (queue) {
					itemToSend = queue.removeFirst();
				}

				//fire the item into the utag javascript code for processing
				if (tealiumEnabled && itemToSend != null && hasInternetConnection && webViewIsReady) {
					Log.d(TAG, "UTAG CALL: " + itemToSend);
					//webView stringByEvaluatingJavaScriptFromString:itemToSend];
					webView.loadUrl(itemToSend);
				}

				//now schedule the next one
				scheduleNextQueuedUTagItem();
			}

		}, 200);
	}
}
