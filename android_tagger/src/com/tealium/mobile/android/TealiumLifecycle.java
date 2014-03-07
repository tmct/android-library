package com.tealium.mobile.android;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.util.Log;

/**
 * 
 * <p>This class is for tracking your application's lifecycle NOT activity lifecycles.
 * Currently lifecycle public methods must be called manually through the applicationCreate(),
 * applicationResume(), applicationPause(), applicationDestroy() calls through TealiumTagger.
 * Normally, there is no need to instantiate or call the lifecycle class or it's methods directly.
 * The lifecycle log is a persistent map with minimal string-object data that's stored and loaded as needed.
 * When loaded, it's values are used to populate properties of this class which are in turn used in the various component
 * processing methods to calculate the data interested in. Because of vendor requirements, the library must make these
 * calculations and parse data into the numerous similar but different data sources found in the Constants - Data Sources listing.</p>
 */
public class TealiumLifecycle {
	
	// CONSTANTS - LOG KEY
	private static final String TAG = "TEALIUM TAGGER";

	// CONSTANTS - PERSISTENCE FILENAME
	private static final String TEALIUM_LIFECYCLE_LOG = "TealiumLifecycleLog";
	
	// CONSTANTS - LIFECYCLE LOG KEYS
	private static final String KEY_FIRST_LAUNCH_DATE = 	"fLaD";
	private static final String KEY_FIRST_LAUNCH_VERSION = 	"fLaV";
	private static final String KEY_PRIOR_CALL_TYPE = 		"pCaT";
	private static final String KEY_RECENT_UPDATE_DATE = 	"rUpD";
	private static final String KEY_RECENT_WAKE_DATE =		"rWaD";
	private static final String KEY_RECENT_LAUNCH_DATE = 	"rLaD";
	private static final String KEY_RECENT_SLEEP_DATE = 	"rSlD";
	private static final String KEY_RECENT_TERMINATE_DATE =	"rTeD";
	private static final String KEY_RECENT_CRASH_DATE =		"rCrD";
	private static final String KEY_RECENT_VERSION =		"rApV";
	private static final String KEY_RECENT_WAKE_COUNT =		"rWaC";
	private static final String KEY_RECENT_LAUNCH_COUNT = 	"rLaC";
	private static final String KEY_RECENT_SLEEP_COUNT =	"rSlC";
	private static final String KEY_RECENT_TERMINATE_COUNT ="rTeC";
	private static final String KEY_RECENT_CRASH_COUNT =	"rCrC";
	private static final String KEY_TOTAL_WAKE_COUNT =		"tWaC";
	private static final String KEY_TOTAL_LAUNCH_COUNT =	"tLaC";
	private static final String KEY_TOTAL_SLEEP_COUNT = 	"tSlC";
	private static final String KEY_TOTAL_TERMINATE_COUNT =	"tTeC";
	private static final String KEY_TOTAL_CRASH_COUNT =		"tCrC";
	private static final String KEY_TOTAL_SECONDS_AWAKE =	"tSeA";
	private static final String KEY_TOTAL_SECONDS_AWAKE_LAUNCH= "tSAL";  //reset at every launch
	
	// CONSTANTS - DATA SOURCES (final output keys)
	private static final String DS_LIFECYCLE_TYPE = 			"lifecycle_type";
	private static final String DS_HOUR_OF_DAY_LOCAL =			"lifecycle_hourofday_local";
	private static final String DS_IS_FIRST_LAUNCH = 			"lifecycle_isfirstlaunch";	
	private static final String DS_IS_FIRST_UPDATE_LAUNCH =		"lifecycle_isfirstlaunchupdate";	
	private static final String DS_IS_FIRST_WAKE_TODAY =		"lifecycle_isfirstwaketoday";
	private static final String DS_IS_FIRST_WAKE_MONTH =		"lifecycle_isfirstwakemonth";
	private static final String DS_LAST_SIMILAR_CALL_DATE =		"lifecycle_lastsimilarcalldate";
	private static final String DS_FIRST_LAUNCH_DATE =			"lifecycle_firstlaunchdate";
	private static final String DS_FIRST_LAUNCH_DATE_MMDDYYYY =	"lifecycle_firstlaunchdate_MMDDYYYY";
	private static final String DS_UPDATED_LAUNCH_DATE =		"lifecycle_updatelaunchdate";
	private static final String DS_SECONDS_AWAKE_SINCE_WAKE = 	"lifecycle_secondsawake";
	private static final String DS_SECONDS_AWAKE_SINCE_LAUNCH = "lifecycle_priorsecondsawake";	
	private static final String DS_RECENT_WAKE_COUNT =			"lifecycle_wakecount";
	private static final String DS_RECENT_LAUNCH_COUNT =		"lifecycle_launchcount";
	private static final String DS_RECENT_SLEEP_COUNT =			"lifecycle_sleepcount";
	private static final String DS_RECENT_TERMINATE_COUNT =		"lifecycle_terminatecount";
	private static final String DS_RECENT_CRASH_COUNT =			"exception_crashcount";
	private static final String DS_TOTAL_WAKE_COUNT =			"lifecycle_totalwakecount";
	private static final String DS_TOTAL_LAUNCH_COUNT =			"lifecycle_totallaunchcount";
	private static final String DS_TOTAL_SLEEP_COUNT =			"lifecycle_totalsleepcount";
	private static final String DS_TOTAL_TERMINATE_COUNT =		"lifecycle_totalterminatecount";
	private static final String DS_TOTAL_SECONDS_AWAKE =		"lifecycle_totalsecondsawake";
	private static final String DS_DAY_OF_WEEK =				"lifecycle_dayofweek_local";
	private static final String DS_DAYS_SINCE_LAUNCH =			"lifecycle_dayssincelaunch";
	private static final String DS_DAYS_SINCE_UPDATE =			"lifecycle_dayssinceupdate";
	private static final String DS_DAYS_SINCE_WAKE =			"lifecycle_dayssincelastwake";
	
	
	// PROPERTIES
	Context 	_context;
	Intent		_intent;
//	Activity 	activity;			// current active activity
	CallTypes	_priorCallType;		// prior call type
	
	Date	_currentCallDate;
	Date 	_firstLaunchDate;
	String 	_firstLaunchVersion;
	String	_recentVersion;
	Date	_recentUpdateDate;
	Date	_recentWakeDate;		// onResume
	Date	_recentLaunchDate;		// onCreate
	Date	_recentSleepDate;		// onPause
	Date	_recentTerminateDate;	// onDestroy
	Date	_recentCrashDate;
	int		_recentWakeCount;
	int		_recentLaunchCount;
	int		_recentSleepCount;
	int		_recentTerminateCount;
	int		_recentCrashCount;
	int		_totalWakeCount;
	int		_totalLaunchCount;
	int		_totalSleepCount;
	int		_totalTerminateCount;
	int		_totalCrashCount;
	long	_totalSecondsAwake;			// aggregate lifetime
	long	_totalSecondsAwakeLaunch;	// aggregate since last launch
	
	// CONSTRUCTOR
	/**
	 * An instance of the Tealium Lifecycle object is created by Tealium Tagger and 
	 * handles lifecycle tracking calculations and file management for persisting and
	 * retrieving the lifecycle log.  There should be no need to call this method directly.
	 */
	public TealiumLifecycle(Activity aActivity, TealiumTagger tealiumTagger) {
//		activity = aActivity;
		_context = aActivity.getApplicationContext();
		_intent = aActivity.getIntent();
		Map<String, Object> map = loadLifecycleLog();
		if (map != null) setLifecycleDataFrom(map);		
	}

	// ENUMS - Note the following key for android => platform standardization:
	/*
	 * Call type enums used and their matching standardized data source value types.
	 * 
	 * 	Crash = Crash*
	 *	Create = Launch
	 * 	Resume = Wake
	 * 	Pause = Sleep
	 * 	Destroy = Terminate
	 * 
	 * 	Crash tracking not yet implemented
	 */
	protected enum CallTypes{
		CRASH,
		CREATE,
		RESUME,
		PAUSE,
		DESTROY
	}
	
	// TESTING METHODS
//	private void setTestDate(Date dateProperty){
//		// Manually alter the first launch date to check testing of date methods
//		Calendar knownCal = Calendar.getInstance();
//		knownCal.setTimeZone(TimeZone.getDefault());
//		// Android Cal Months start at 0 = January
//		knownCal.set(2013,7,29,12,0);
//		Date newDate = knownCal.getTime();
//		dateProperty = newDate;
//	}
	
	// PUBLIC METHODS
	/**
	 * This method is used by the Tealium Tagger to update & retrieve current lifecycle
	 * data in a format usable by the dispatch process. There should be no need to call this method directly.
	 */
	public Map<String, String> lifecycleDataForType(CallTypes type){
		
		_currentCallDate = new Date();
		Map<String, String> map = null;
		
		if (shouldSuppressCallForType(type) == false) {
			map = new HashMap<String, String>();

			// Uncomment to overwrite a date entry for testing
			//setTestDate(_firstLaunchDate);
			
			updateCounts(type);

			Map<String, Object> log = lifecycleLog();
			// General lifecycle data
			map = convertMapForOutput(log);
			
			// Call type dependent lifecycle data -- This needs to be refactored
			String typeString = lifecycleTypeString(type);
			if (typeString != null) {
				map.put(DS_LIFECYCLE_TYPE, typeString);
				map.put("link_id", typeString);
			}
			
			String lscd = getTimestampAsISO(lastSimilarCallDate(type));
			if (lscd != null) map.put(DS_LAST_SIMILAR_CALL_DATE, lscd);

			
			// Session tracking
			long sa = getSecondsAwake(type, _recentWakeDate, _currentCallDate);
			if (_totalSecondsAwake >=0){map.put(DS_TOTAL_SECONDS_AWAKE, Long.toString(_totalSecondsAwake) );}
			
			if (type == CallTypes.CREATE){
				map.put(DS_SECONDS_AWAKE_SINCE_LAUNCH, Long.toString(_totalSecondsAwakeLaunch));
				_totalSecondsAwakeLaunch = 0;
			}
			else if ((_priorCallType != CallTypes.PAUSE && _priorCallType != CallTypes.DESTROY) &&
					(type == CallTypes.PAUSE || type == CallTypes.DESTROY)) {
				map.put(DS_SECONDS_AWAKE_SINCE_WAKE, Long.toString(sa));
				_totalSecondsAwake = _totalSecondsAwake + sa;
				_totalSecondsAwakeLaunch = _totalSecondsAwakeLaunch + sa;
			}
			
			updateLastSimilarCallDate(type);
			
			if (map != null){
				int flags = _intent.getFlags();       
				if ((flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
				    // The activity was launched from history
					Log.i(TAG, "Activity was launched from history");
				}
				if ((flags & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0){
					Log.i(TAG, "Activity was brought to front");
				}
				if ((flags & Intent.FLAG_FROM_BACKGROUND) != 0){
					Log.i(TAG, "Activity from background");
				}
				Log.i(TAG, "Tealium lifecycle data for call at " + _currentCallDate + " ISO8601: " + getTimestampAsISO(_currentCallDate) + " of type " + type + ": " + map);
			}
		}
		
		// Save and update if new call type
		if (_priorCallType != type) {
			_priorCallType = type; 
			saveLifecycleLog(lifecycleLog());
		}

		return map;
	}

	
	// PRIVATE METHODS
	
	// FORMATTING
	
	private Boolean shouldSuppressCallForType(CallTypes type){
		// OnCreate is always followed by onResume, suppress the resume to prevent a double call
		if (_priorCallType == CallTypes.CREATE && type == CallTypes.RESUME){ 
			Log.i(TAG, "Suppressing a resume call following a create call");
			return true;
		}
//		if (type == CallTypes.PAUSE && isApplicationSentToBackground(_context) == false){
			// requires permissions: GET_TASKS
//			Log.i(TAG, "Application Pause request received but application still running");
//			return true;
//		}
		if (_priorCallType == type){
			Log.i(TAG, "Suppressing duplicate " + type + " call");
			return true;
		}
		return false;
	}
	
	/*
	 * The ConvertMapForOutput method filters and processes the Lifecycle log, 
	 * made up of raw tracking data, into human readable data source objects
	 * that are used for mapping in TealiumIQ
	 */
	private Map<String, String> convertMapForOutput(Map<String, Object> map){
		// We're only passing some of the raw data stored in the lifecycle log
		// and mostly passing back a lot of data calculated from the lifecycle log 
		Map<String, String> newMap = new HashMap<String, String>();
		
		Date callDate = _currentCallDate; //(Date)map.get(KEY_RECENT_CALL_DATE);
		
		if (map.get(KEY_FIRST_LAUNCH_DATE) != null) { 
			Date date = (Date)map.get(KEY_FIRST_LAUNCH_DATE);
			String stringISO = getTimestampAsISO(date);
			newMap.put(DS_FIRST_LAUNCH_DATE,stringISO);
			String stringMMDDYYYY = getTimestampAsMMDDYYY(date);
			newMap.put(DS_FIRST_LAUNCH_DATE_MMDDYYYY, stringMMDDYYYY);
			}
		if (map.get(KEY_RECENT_WAKE_COUNT) != null) { newMap.put(DS_RECENT_WAKE_COUNT, map.get(KEY_RECENT_WAKE_COUNT).toString()); }
		if (map.get(KEY_RECENT_LAUNCH_COUNT) != null) { newMap.put(DS_RECENT_LAUNCH_COUNT, map.get(KEY_RECENT_LAUNCH_COUNT).toString()); }
		if (map.get(KEY_RECENT_SLEEP_COUNT) != null) { newMap.put(DS_RECENT_SLEEP_COUNT, map.get(KEY_RECENT_SLEEP_COUNT).toString()); }		
		if (map.get(KEY_RECENT_TERMINATE_COUNT) != null) { newMap.put(DS_RECENT_TERMINATE_COUNT, map.get(KEY_RECENT_TERMINATE_COUNT).toString()); }
		if (map.get(KEY_RECENT_CRASH_COUNT) != null) {map.put(DS_RECENT_CRASH_COUNT, map.get(KEY_RECENT_CRASH_COUNT));}

		if (map.get(KEY_TOTAL_WAKE_COUNT) != null) { newMap.put(DS_TOTAL_WAKE_COUNT, map.get(KEY_TOTAL_WAKE_COUNT).toString()); }
		if (map.get(KEY_TOTAL_LAUNCH_COUNT) != null) { newMap.put(DS_TOTAL_LAUNCH_COUNT, map.get(KEY_TOTAL_LAUNCH_COUNT).toString()); }
		if (map.get(KEY_TOTAL_SLEEP_COUNT) != null) { newMap.put(DS_TOTAL_SLEEP_COUNT, map.get(KEY_TOTAL_SLEEP_COUNT).toString()); }
		if (map.get(KEY_TOTAL_TERMINATE_COUNT) != null) { newMap.put(DS_TOTAL_TERMINATE_COUNT, map.get(KEY_TOTAL_TERMINATE_COUNT).toString()); }

		if (isFirstLaunch(callDate, (Date)map.get(KEY_FIRST_LAUNCH_DATE), map.get(KEY_RECENT_WAKE_COUNT))){ newMap.put(DS_IS_FIRST_LAUNCH, "true"); }
		
		if (_recentUpdateDate != null) { newMap.put(DS_UPDATED_LAUNCH_DATE, getTimestampAsISO(_recentUpdateDate));} 
		if (isFirstUpdateLaunchDate(_currentCallDate) == true) { newMap.put(DS_IS_FIRST_UPDATE_LAUNCH, "true"); }
				
		if (isFirstWakeMonth()) { newMap.put(DS_IS_FIRST_WAKE_MONTH, "true");}
		if (isFirstWakeToday()) { newMap.put(DS_IS_FIRST_WAKE_TODAY, "true"); }
		
		String hod = getHourOfDayLocal(_currentCallDate);
		String dowl = getDayOfWeekLocal(_currentCallDate);
		String dslw = getDaysSinceLastWake(_currentCallDate);
		String dsl = getDaysSinceLaunch(_currentCallDate);
		String dsu = getDaysSinceUpdate(_currentCallDate);
		
		if (hod != null) { newMap.put(DS_HOUR_OF_DAY_LOCAL, hod);}
		if (dowl != null) { newMap.put(DS_DAY_OF_WEEK, dowl);}
		if (dslw != null) { newMap.put(DS_DAYS_SINCE_WAKE, dslw);}
		if (dsl != null) { newMap.put(DS_DAYS_SINCE_LAUNCH, dsl);}
		if (dsu != null) { newMap.put(DS_DAYS_SINCE_UPDATE, dsu);}
		
		return newMap;
	}



	private String lifecycleTypeString(CallTypes type){
		String string = null;
		switch(type){
		case CREATE:
			string = "launch";
			break;
		case RESUME:
			string = "wake";
			break;
		case PAUSE:
			string = "sleep";
			break;
		case DESTROY:
			string = "terminate";
			break;
		case CRASH:
			string = "crash";
			break;
		}
		return string;
	}
	
	
	// LOGGING
	
	private void updateCounts(CallTypes type){		
		
		// Call Processing
		switch (type){
			case CREATE:
				if (_firstLaunchDate == null) {
					_firstLaunchDate = _currentCallDate;
				}
				updateLaunchVersion();
				_recentLaunchCount++;
				_totalLaunchCount++;				
			case RESUME:
				_recentWakeCount++;
				_totalWakeCount++;
				break;
			case PAUSE:
				_recentSleepCount++;
				_totalSleepCount++;
				break;
			case DESTROY:
				_recentTerminateCount++;
				_totalTerminateCount++;
				break;
			default:
				break;			
		}
	}
	
	private void updateLastSimilarCallDate(CallTypes type){
		Date callDate = _currentCallDate;
		switch(type){
		case CRASH:
			_recentCrashDate = callDate;
			break;
		case CREATE:
			_recentLaunchDate = callDate;
		case RESUME:
			_recentWakeDate = callDate;
			break;
		case PAUSE:
			_recentSleepDate = callDate;
			break;
		case DESTROY:
			_recentTerminateDate = callDate;
			break;
		}
	}
	
	
	// COMPONENT PROCESSING
	
	
	private void updateLaunchVersion(){

			try{
				PackageInfo info = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
				String version = info.versionName;
				if (_firstLaunchVersion == null) {
					_firstLaunchVersion = version;
					_recentVersion = null;
					_recentUpdateDate = null;
				}
				else if (_firstLaunchVersion != version &&
						_recentVersion == null){
					_recentVersion = version;
					_recentUpdateDate = _currentCallDate;
				}
			} catch (Exception e) {
				Log.i(TAG, "Unable to determine current app version.");
			}		
	}
	
	private boolean isFirstWakeMonth(){	
		
		Date lastCallDate = lastSimilarCallDate(CallTypes.RESUME);
		if (lastCallDate == null) return true;
		
		 Calendar lcal = Calendar.getInstance();
		 lcal.setTime(lastCallDate);
		 
		 Calendar ccal=Calendar.getInstance();
		 ccal.setTime(_currentCallDate);
		 
		 int cyear = ccal.get(Calendar.YEAR);
		 int lyear = lcal.get(Calendar.YEAR);
		 if (cyear > lyear) return true;
		 else {		 
			 int cmonth = ccal.get(Calendar.MONTH);
			 int lmonth = lcal.get(Calendar.MONTH);
			 if (cmonth > lmonth) return true;
		 }
		return false;
	}
	
	private boolean isFirstWakeToday(){
		
		Date lastCallDate = lastSimilarCallDate(CallTypes.RESUME);
		if (lastCallDate == null) return true;
		
		 Calendar lcal = Calendar.getInstance();
		 lcal.setTime(lastCallDate);
		 
		 Calendar ccal=Calendar.getInstance();
		 ccal.setTime(_currentCallDate);
		 
		 int cyear = ccal.get(Calendar.YEAR);
		 int lyear = lcal.get(Calendar.YEAR);
		 if (cyear > lyear) return true;
		 else {		 
			 int cmonth = ccal.get(Calendar.MONTH);
			 int lmonth = lcal.get(Calendar.MONTH);
			 if (cmonth > lmonth) return true;
			 else {
				 int cday = ccal.get(Calendar.DAY_OF_MONTH);
				 int lday = lcal.get(Calendar.DAY_OF_MONTH);
				 if (cday > lday) return true;
			 }
		 }
		return false;
	}
	
	private boolean isAppUpdated(){
		if (_recentVersion != null &&
			_firstLaunchVersion != null &&
			_recentVersion != _firstLaunchVersion){
			return true;
		} else return false;
	}
	
	private boolean isFirstUpdateLaunchDate(Date date){	
		if (isAppUpdated() == true){
			 Calendar ucal = Calendar.getInstance();
			 ucal.setTimeZone(TimeZone.getDefault());
			 ucal.setTime(_recentUpdateDate);
			 
			 Calendar ccal = Calendar.getInstance();
			 ccal.setTimeZone(TimeZone.getDefault());
			 ccal.setTime(date);
			 
			 long utime= ucal.getTimeInMillis();
			 long ctime = ccal.getTimeInMillis();
			 if (utime == ctime) return true;
		}
		return false;
	}
	
	private boolean isFirstLaunch(Date callDate, Date firstCallDate, Object wakeCount) {
		if (callDate == firstCallDate){
			if ((Integer)wakeCount == 1){
				return true;
			}
		}
		return false;
	}
	
//	private static boolean isApplicationSentToBackground(final Context context) {
//	    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//	    List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
//	    if (!tasks.isEmpty()) {
//	      ComponentName topActivity = tasks.get(0).topActivity;
//	      if (!topActivity.getPackageName().equals(context.getPackageName())) {
//	        return true;
//	      }
//	    }
//	    return false;
//	}
	
	private Date lastSimilarCallDate(CallTypes type){
		
		Date lastDate = new Date();
		switch(type){
		case CRASH:
			lastDate = _recentCrashDate;
			break;
		case CREATE:
			lastDate = _recentLaunchDate;
			break;
		case RESUME:
			lastDate = _recentWakeDate;
			break;
		case PAUSE:
			lastDate = _recentSleepDate;
			break;
		case DESTROY:
			lastDate = _recentTerminateDate;
			break;
		}
		if (lastDate != null) return lastDate;
		return null;
	}
	
	
	private long getSecondsAwake(CallTypes type, Date recentWakeDate, Date currentCallDate){
		
		if(recentWakeDate == null){
			Log.i(TAG, "Wake date argument empty");
			return -1;
		}
		if (currentCallDate == null) {
			Log.i(TAG, "Current date argument empty");
			return -1;
		}
		if (type == CallTypes.PAUSE ||
			type == CallTypes.DESTROY){
			  Calendar sDate = Calendar.getInstance();
			  sDate.setTime(recentWakeDate);
			  sDate.setTimeZone(TimeZone.getDefault());
			  
			  Calendar eDate = Calendar.getInstance();
			  eDate.setTime(currentCallDate);
			  eDate.setTimeZone(TimeZone.getDefault());
			  
			  long seconds = (eDate.getTimeInMillis() - sDate.getTimeInMillis())/1000;
			  return seconds;
		}
		
		return -1;
	}
	
	private String getDaysSinceUpdate(Date date){
		Date launchDate = _recentUpdateDate;
		if (launchDate != null){
		
		  Calendar sDate = Calendar.getInstance();
		  sDate.setTime(launchDate);
		  sDate.setTimeZone(TimeZone.getDefault());
		  
		  Calendar eDate = Calendar.getInstance();
		  eDate.setTime(date);
		  sDate.setTimeZone(TimeZone.getDefault());

		  long daysBetween = 0;
		  while (sDate.before(eDate)) {
		      sDate.add(Calendar.DAY_OF_MONTH, 1);
		      daysBetween++;
		  }
		  if (daysBetween < 1) return null;
		  return String.valueOf(daysBetween);
		} else {
			return null;
		}
	}
	
	private String getDaysSinceLaunch(Date date){
		Date launchDate = _firstLaunchDate;
		if (launchDate == null) launchDate = _currentCallDate;
		
		  Calendar sDate = Calendar.getInstance();
		  sDate.setTime(launchDate);
		  sDate.setTimeZone(TimeZone.getDefault());
		  
		  Calendar eDate = Calendar.getInstance();
		  eDate.setTime(date);
		  sDate.setTimeZone(TimeZone.getDefault());

		  long daysBetween = 0;
		  while (sDate.before(eDate)) {
		      sDate.add(Calendar.DAY_OF_MONTH, 1);
		      daysBetween++;
		  }
		  return String.valueOf(daysBetween);
	}
	
	private String getDaysSinceLastWake(Date date){
		Date lastWake = lastSimilarCallDate(CallTypes.RESUME);
		if (lastWake == null) lastWake = _currentCallDate;
		
		  Calendar sDate = Calendar.getInstance();
		  sDate.setTime(lastWake);
		  sDate.setTimeZone(TimeZone.getDefault());
		  
		  Calendar eDate = Calendar.getInstance();
		  eDate.setTime(date);
		  eDate.setTimeZone(TimeZone.getDefault());

		  long sT = sDate.getTimeInMillis();
		  long eT = eDate.getTimeInMillis();
		  long dT = (eT - sT)/86400000;
		  long daysBetween = dT;
			  
		  return String.valueOf(daysBetween);
	}
	
	private String getDayOfWeekLocal(Date date){
		 Calendar cal = Calendar.getInstance();
		 cal.setTime(date);
		 cal.setTimeZone(TimeZone.getDefault());
		 int i = cal.get(Calendar.DAY_OF_WEEK);
		 if (i >= 0 ) return String.valueOf(i);
		 else return null;
	}
	
	private String getHourOfDayLocal(Date date){
		 Calendar cal = Calendar.getInstance();
		 cal.setTime(date);
		 cal.setTimeZone(TimeZone.getDefault());
		 int hod = cal.get(Calendar.HOUR_OF_DAY);
		 if (hod >= 0 ) return String.valueOf(hod);
		 else return null;
	}
	
	@SuppressLint("SimpleDateFormat")
	private String getTimestampAsMMDDYYY(Date date){
		TimeZone tz = TimeZone.getTimeZone("UTC");
	    DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
	    df.setTimeZone(tz);
	    String string = df.format(date);
	    return string;
	}
	
	@SuppressLint("SimpleDateFormat")
	private String getTimestampAsISO(Date aDate){
		// Carlos Heuberger
		if (aDate == null) return null;
		TimeZone tz = TimeZone.getTimeZone("UTC");
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    df.setTimeZone(tz);
	    String nowAsISO = df.format(aDate);
	    return nowAsISO;
	}
	
	// PERSISTENCE
	private Map<String, Object> loadLifecycleLog(){

		if (_context == null){
			Log.e(TAG, "Could not find current context");
			return null;
		}
		
		File file = new File(_context.getFilesDir(), TEALIUM_LIFECYCLE_LOG);
	
		Map<String, Object> map;
		FileInputStream fis;
        ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			@SuppressWarnings("unchecked")
			Map<String, Object> readObject = (Map<String, Object>) ois.readObject();
			map = readObject;
			Log.i(TAG, "Lifecycle log loaded from memory: " + map);
			return map;
		} catch (FileNotFoundException e) {
			Log.i(TAG, "No prior lifecycle log to load - If lifecycle tracking not implemented, can ignore this message.");
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "Unable to load lifecycle log - Class not found exception.");
		} catch (StreamCorruptedException e) {
			Log.e(TAG, "Unable to load lifecycle log - ObjectInputStream StreamCorruptedException.");
		} catch (IOException e) {
			Log.e(TAG, "Unable to load lifecycle log - ObjectInfputStream IOException.");
		} finally {
			map = null;
			fis = null;
			try{
				if (ois != null) ois.close();}
			catch (IOException e) {
				Log.e(TAG, "Unable to load lifecycle log - ObjectInfputStream IOException.");
			}
		}
	
		return null;
	}
	
	private void saveLifecycleLog(Map<String, Object> lifecycleLog){
				
		if (_context == null) { Log.i(TAG, "Can not save lifecycle log - missing context"); return; }
		File file = new File(_context.getFilesDir(), TEALIUM_LIFECYCLE_LOG);		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Unable to save lifecycle log - FileOutputStream FileNotFoundException.");
		}

		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(fos);
			oos.writeObject(lifecycleLog);
			oos.close();
			Log.i(TAG, "Lifecycle log saved");

		} catch (IOException e) {
			Log.e(TAG, "Unable to save lifecycle log - ObjectOutputStream IOException.");
		}
	}
	
	public void resetLifecycleLog(){
		purgeLifecycleData();
		saveLifecycleLog(lifecycleLog());
	}

	private void setLifecycleDataFrom(Map<String, Object> map){
		if (map == null) return;
		
		// sets lifecycle properties from a map
		_firstLaunchDate = (Date) map.get(KEY_FIRST_LAUNCH_DATE);
		_firstLaunchVersion = (String) map.get(KEY_FIRST_LAUNCH_VERSION);
		_priorCallType = (CallTypes)map.get(KEY_PRIOR_CALL_TYPE);
		_recentUpdateDate = (Date) map.get(KEY_RECENT_UPDATE_DATE);
		_recentWakeDate = (Date) map.get(KEY_RECENT_WAKE_DATE);
		_recentLaunchDate = (Date) map.get(KEY_RECENT_LAUNCH_DATE);
		_recentSleepDate = (Date) map.get(KEY_RECENT_SLEEP_DATE);
		_recentTerminateDate = (Date) map.get(KEY_RECENT_TERMINATE_DATE);
		_recentCrashDate = (Date) map.get(KEY_RECENT_CRASH_DATE);
		_recentVersion = (String) map.get(KEY_RECENT_VERSION);
		_recentWakeCount = (Integer) map.get(KEY_RECENT_WAKE_COUNT);
		_recentLaunchCount = (Integer)map.get(KEY_RECENT_LAUNCH_COUNT);
		_recentSleepCount = (Integer)map.get(KEY_RECENT_SLEEP_COUNT);
		_recentTerminateCount = (Integer)map.get(KEY_RECENT_TERMINATE_COUNT);
		_recentCrashCount = (Integer)map.get(KEY_RECENT_CRASH_COUNT);
		_totalWakeCount = (Integer)map.get(KEY_TOTAL_WAKE_COUNT);
		_totalLaunchCount = (Integer)map.get(KEY_TOTAL_LAUNCH_COUNT);
		_totalSleepCount = (Integer)map.get(KEY_TOTAL_SLEEP_COUNT);
		_totalTerminateCount = (Integer)map.get(KEY_TOTAL_TERMINATE_COUNT);
		_totalCrashCount = (Integer)map.get(KEY_TOTAL_CRASH_COUNT);
		_totalSecondsAwake = (Long)map.get(KEY_TOTAL_SECONDS_AWAKE);
		_totalSecondsAwakeLaunch = (Long)map.get(KEY_TOTAL_SECONDS_AWAKE_LAUNCH);
	}
	
	private Map<String, Object> lifecycleLog(){
		// generates map from lifecycle properties
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (_firstLaunchDate != null) 		map.put(KEY_FIRST_LAUNCH_DATE, _firstLaunchDate);
		if (_firstLaunchVersion != null)	map.put(KEY_FIRST_LAUNCH_VERSION, _firstLaunchVersion);
		if (_priorCallType != null)			map.put(KEY_PRIOR_CALL_TYPE, _priorCallType);
		if (_recentUpdateDate != null) 		map.put(KEY_RECENT_UPDATE_DATE, _recentUpdateDate);	
		if (_recentWakeDate != null)		map.put(KEY_RECENT_WAKE_DATE, _recentWakeDate);
		if (_recentLaunchDate != null)		map.put(KEY_RECENT_LAUNCH_DATE, _recentLaunchDate);
		if (_recentSleepDate != null)		map.put(KEY_RECENT_SLEEP_DATE, _recentSleepDate);
		if (_recentTerminateDate != null)	map.put(KEY_RECENT_TERMINATE_DATE, _recentTerminateDate);
		if (_recentCrashDate != null)		map.put(KEY_RECENT_CRASH_DATE, _recentCrashDate);
		if (_recentVersion != null)			map.put(KEY_RECENT_VERSION, _recentVersion);
		if (_recentWakeCount >= 0)			map.put(KEY_RECENT_WAKE_COUNT, _recentWakeCount);
		if (_recentLaunchCount >= 0)		map.put(KEY_RECENT_LAUNCH_COUNT, _recentLaunchCount);
		if (_recentSleepCount >= 0)			map.put(KEY_RECENT_SLEEP_COUNT, _recentSleepCount);
		if (_recentTerminateCount >= 0)		map.put(KEY_RECENT_TERMINATE_COUNT, _recentTerminateCount);
		if (_recentCrashCount >= 0)			map.put(KEY_RECENT_CRASH_COUNT, _recentCrashCount);
		if (_totalWakeCount >= 0)			map.put(KEY_TOTAL_WAKE_COUNT, _totalWakeCount);
		if (_totalLaunchCount >= 0)			map.put(KEY_TOTAL_LAUNCH_COUNT, _totalLaunchCount);
		if (_totalSleepCount >= 0)			map.put(KEY_TOTAL_SLEEP_COUNT, _totalSleepCount);
		if (_totalTerminateCount >= 0)		map.put(KEY_TOTAL_TERMINATE_COUNT, _totalTerminateCount);
		if (_totalCrashCount >= 0)			map.put(KEY_TOTAL_CRASH_COUNT, _totalCrashCount);
		if (_totalSecondsAwake >= 0)		map.put(KEY_TOTAL_SECONDS_AWAKE, _totalSecondsAwake);
		if (_totalSecondsAwakeLaunch >=0)	map.put(KEY_TOTAL_SECONDS_AWAKE_LAUNCH, _totalSecondsAwakeLaunch);
		
		return map;
	}
	
	private void purgeLifecycleData(){
		_firstLaunchDate = null;
		_firstLaunchVersion = null;
		_priorCallType = null;
		_recentUpdateDate = null;
		_recentWakeDate = null;
		_recentLaunchDate = null;
		_recentSleepDate = null;
		_recentTerminateDate = null;
		_recentCrashDate = null;
		_recentVersion = null;
		_recentWakeCount = 0;
		_recentLaunchCount = 0;
		_recentSleepCount = 0;
		_recentTerminateCount = 0;
		_recentCrashCount = 0;
		_totalWakeCount = 0;
		_totalLaunchCount = 0;
		_totalSleepCount = 0;
		_totalTerminateCount = 0;
		_totalCrashCount = 0;
		_totalSecondsAwake = 0;
		_totalSecondsAwakeLaunch = 0;
		
		Log.i(TAG, "Lifecycle Log reset");
	}
	
}
