Tealium Android Library - 4.1.4 &amp; 4.1.4c
=====================================

**********************
<img align="left" src="https://res.cloudinary.com/dfpz40r7j/image/upload/v1479312678/warning-icon-60_yd8bwd.png"> This library has been replaced by the [tealium-android](https://github.com/tealium/tealium-android) repository. This version can still be used with Tealium services but no further development or bug fixes planned.
**********************

### Brief ###

The frameworks included allow the native tagging of a mobile application once and then configuration of third-party analytic services remotely through [Tealium IQ](http://tealium.com/products/enterprise-tag-management/); all without needing to recode and redeploy an app for every update to these services.

First time implementations should read the [How Tealium Works](../../wiki/how-tealium-works) wiki page for a brief overview of how Tealium's SDK differs from conventional analytic SDKs. For any additional information, consult the [wiki home page](../../wiki/home).

The remainder of this document provides quick install instructions.

###Table of Contents###

- [Requirements](#requirements)
- [Android Studio Quick Start](#android-studio-quick-start)
    - [1. Add Tealium Library](#add-tealium-library)
    - [2. Init and Track](#init-and-track)
- [Eclipse Quick Start](#eclipse-quick-start)
    - [1. Clone/Copy Library](#1-clonecopy-library)
    - [2. Add to Project](#2-add-to-project)
- [Run App](#run)
- [Dispatch Verification](#dispatch-verification)
- [What Next](#what-next)
	- [ProGuard](#proguard)
- [Contact Us](#contact-us)
- [Switching Between Full and Compact](#switching-between-full-and-compact)

###Requirements###
- [Android Studio](https://developer.android.com/sdk/index.html) 

OR
- [Android ADT Bundle with Eclipse](http://developer.android.com/tools/help/adt.html)
	- NOTE: Written from the Eclipse perspective but compatible with any Android development environment.
- Minimum target Android Version: 9 / GINGERBREAD


### Android Studio Quick Start ###

####AS1. Add Tealium Library ####

AS1a. Add a *libs* folder to your Android Studio project's *Application* folder.

AS1b. Copy the desired Tealium library jar file into the new *libs* folder

![](../../wiki/images/AS_FileStructure_Tealium.png)

AS1c. Add the jar files path to your Build.Gradle's *dependencies*

![](../../wiki/images/AS_BuildGradle_Tealium.png)

AS1d. Update Manifest.xml with the following use-permissions:
- android.permission.ACCESS_NETWORK_STATE
- android.permission.INTERNET

![](../../wiki/images/AS_Manifest_Tealium.png)


####AS2. Init and Track ####

AS2a. Add the following import statements to your Application or Activity Class:
```java
package com.example.myapp;

import android.app.Application;
import com.tealium.library.Tealium;
import com.tealium.library.Tealium.Config;
import com.tealium.library.Tealium.LogLevel;
```

AS2b. Init the library in the same application class:

```java
public class MyApplication extends Application {

@Override
public void onCreate() {
	super.onCreate();
	// Must initialize after the super.onCreate() call.

	Tealium.initialize(Config.create(this, "tealiummobile", "demo", "dev")
		.setLibraryLogLevel(LogLevel.DEBUG));

	// (!) Don't forget to replace "tealiummobile", "demo" and "dev" with your own account-profile-target settings before creating your production build. 
}
}
```

### Eclipse Quick Start ###
This guide presumes you have already created an Android app using Eclipse. Follow the below steps to add Tealium's [Full Library](../../wiki/compact-vs-full) to it.  Discussion on which version is ultimately best for you can be found in the [What Next](#what-next) section.

####E1. Clone/Copy Library####
onto your dev machine by clicking on the *Clone to Desktop* or *Download ZIP* buttons on the main repo page.

![](../../wiki/images/android_githubclone.png)

####E2. Add To Project 

E2a. Create a "libs" directory in your project root, if not already present. 

E2b. From the *android-library/TealiumFull* folder, drag & drop the ```tealium.x.jar``` file into your Eclipse project's Package Explorer window.

![](../../wiki/images/android_addtoproject.png)

E2c. Click "Ok" in the resulting File Operation dialog box.

![](../../wiki/images/android_copylinkbox.png)

E2d. [Add the following Permissions](http://developer.android.com/guide/topics/manifest/uses-permission-element.html) to your project:

- android.permission.INTERNET
- android.permission.ACCESS_NETWORK_STATE

Your project's AndroidManifest.xml's Permission's tab should now look similar to:
![](../../wiki/images/android_permissions.png)

E2e. Import the library into your project's primary application class:

```java
package com.example.myapp;

import android.app.Application;
import com.tealium.library.Tealium;
import com.tealium.library.Tealium.Config;
import com.tealium.library.Tealium.LogLevel;
```

E2f. Init the library in the same application class:

```java
public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
        // Must initialize after the super.onCreate() call.
        
        Tealium.initialize(Config.create(this, "tealiummobile", "demo", "dev")
			.setLibraryLogLevel(LogLevel.DEBUG));
		
        // (!) Don't forget to replace "tealiummobile", "demo" and "dev" with your own account-profile-target settings before creating your production build. 
	}
}
```

Example of the required import and init statements: 

**MyApplication.java**

```java
package com.example.myapp;

import android.app.Application;
import com.tealium.library.Tealium;

// Subclass  android.app.Application so that way Tealium 
//  will already be initialized for any Activity. 
public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
        // It is necessary to initialize after the super.onCreate() call.
        
        Tealium.initialize(Tealium.Config.create(this, "tealiummobile", "demo", "dev")
			.setLibraryLogLevel(Tealium.LogLevel.DEBUG));

        // (!) Don't forget to replace "tealiummobile", "demo" and "dev" with your own account-profile-target settings before creating your production build. 

	}
}
```

E2g. Ensure the **AndroidManifest.xml** has been updated to use this Application subclass:
```xml
<!-- <manifest ... -->
	<application
        	android:allowBackup="true"
        	android:icon="@drawable/ic_launcher"
        	android:label="@string/app_name"
        	android:theme="@style/AppTheme" 
        	android:name="com.example.myapp.MyApplication">
        	<!-- 
        		If "android:name" is not defined, Android will use the base 
        		Application, and Tealium.initialize(...) will not be called.  
        	-->
        	<!-- ... -->
    	</application>
<!-- ... </manifest> -->
```

E2h. [*Tealium.onResume(Activity)*](../../wiki/API-Tealium#void-onresumeactivity-activity) and [*Tealium.onPause(Activity)*](../../wiki/API-Tealium#void-onpauseactivity-activity) methods will need to be added to each of your activity classes if you minimum SDK &lt; 14 (ICE CREAM SANDWICH).

Example:

**MainActivity.java**

```java
package com.example.myapp;

import com.tealium.library.Tealium;
import android.os.Bundle;
import android.app.Activity;

public class MainActivity extends Activity implements View.OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        
        Tealium.onResume(this); 
		
		// COMPACT LIBRARY ONLY:
		Tealium.track(this, null, null);
		// The full library will pick up this view event automatically.
	}		

	@Override
	protected void onPause() {
		super.onPause();
        Tealium.onPause(this);
	}
}
```


###Run
Your app is now ready to compile and run.  In the console output you should see a variation of:

![](../../wiki/images/android_console.png)

Congratulations! You have successfully implemented the Tealium Full library into your project.

This output:

```
04-17 11:51:20.525: D/Tealium(2599): view : {
04-17 11:51:20.525: D/Tealium(2599):  "object_class": "MainActivity"
04-17 11:51:20.525: D/Tealium(2599):  "screen_title": "Tealium Example"
04-17 11:51:20.525: D/Tealium(2599):  "tealium_id": "SEYco"
04-17 11:51:20.525: D/Tealium(2599): }
```

shows an abbreviation of all of the data gathered, use ```Tealium.Config.setLibraryLogLevel(LogLevel.VERBOSE)``` to see all datasources available for mapping in Tealium's IQ Dashboard. The Library only actually sends those data sources and values that are mapped.

####Dispatch Verification

The two recommended methods for dispatch verification are:

- AudienceStream Live Events
- Vendor Dashboard

AudienceStream live events provides real time visualization of dispatched data if the Tealium DataCloud Tag has been added the same TIQ account-profile used to init the library:

![](../../wiki/images/EventStore.png)

An analytic vendor with real time processing, such as [Google Analytics](http://www.google.com/analytics/)), can also be used to verify dispatches if the data sources have been properly mapped to the target vendors' variables. 

Note: vendors without real-time processing may take up to several hours to update their reporting.

###Switching Between Full and Compact

Swapping the ```tealium.x.jar``` with ```tealium.xc.jar``` (or vice versa) is simple; just replace the undesired library in the *libs/* directory with the desired library. Since the Full and Compact libraries have identical APIs; the swap will produce no errors.

### What Next###
Now that you've successfully integrated the library, you should now determine if the [Compact or Full Library versions](../../wiki/compact-vs-full) best fit your needs. Below are the key differences:


|     |Compact  |  Full
-------------------------------------|:-------------------------------:|:----:
jar size                                                            |102 KB | 177 KB
Initialization time                                                 |~ 0.01 sec | ~ 0.01 sec
Memory Usage                                                        |~ 604 KB |~ 741 KB
[Non-UI AutoTracking](../../wiki/Advanced-Guide#non-ui-autotracking)                |Yes |  Yes
[UI Autotracking](../../wiki/Advanced-Guide#ui-autotracking)                        |No  |  Yes
[Mobile Companion](../../wiki/advanced-guide#mobile-companion)  |No  |  Yes
[Mobile AudienceStream Trace](../../wiki/Advanced-Guide#audiencestream-trace)  |No  |  Yes

If continuing with the Compact version, add any needed [additional tracking calls](../../wiki/advanced-guide#universal-track-call) for events or view appearances.

Still can't decide? Browse through our [wiki pages](../../wiki/home) for more info, or check out our [TealiumIQ Community](https://community.tealiumiq.com/series/3333)

#### ProGuard

If you choose to [ProGuard](http://developer.android.com/tools/help/proguard.html) an app bundled with the Tealium Library; please be sure to start with the default configuration located at ```${sdk.dir}/tools/proguard/proguard-android.txt```. The following rules will also need to be added to the default:

```
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
	public *;
}

-keep class com.tealium.library.* {
	public <init>(...);
	<methods>;
}
```

### Contact Us ###
Questions or comments?

- Post code questions in the [issues page.](../../issues)
- Contact your Tealium account manager

### Upgrade Notice

#### New Features
* **Version 4.1.4**
 * Added setAcceptThirdPartyCookie API to Tealium.Config
 * Removed Automatic lowercasing of data source Keys  
* **Version 4.1.3**
 * Added setAcceptCookie API to Tealium.Config
* **Version 4.1.2**
 * Corrected a bug where passing a non-anticipated object in the Tealium.track(...) method failed to populate various data source keys
* **Version 4.1.1**
 * Corrected ```platform_version``` and ```os_version``` data sources
 * Bug fixes  
* **Version 4.1**
 * Added Support for [TagBridge](../../wiki/Features#tag-bridge-api)
 * Added Android Studio compatible Sample Apps.
* **Version 4.0**
 * Added Support for Mobile Publish Settings
 * Removed Methods/Fields deprecated in **Version 3**.
* **Version 3.1:**
 * Added UI-Autoracking [Activity exlusion API](../../wiki/API-Tealium.Config#tealiumconfig-setexcludedactivityclassessetclass-extends-activity-excludedactivityclasses)
* **Version 3.0:**

If upgrading from a Library version earlier than 3.0 note that:

* void trackCustomEvent(String eventName, Map&lt;String, String&gt; variables)
* void trackItemClicked(String itemName)
* void trackItemClicked(String itemName, Map&lt;String, String&gt; variables)
* void trackScreenViewed(String viewName)
* void trackScreenViewed(String viewName, Map&lt;String, String&gt; variables)

are no longer available. Please also note that 

```java
boolean onResume(Activity)
```

is now

```java
void onResume(Activity)
```

and

```java
boolean onPause()
```

is now

```java
void onPause(Activity)
```


--------------------------------------------

Copyright (C) 2012-2015, Tealium Inc.


