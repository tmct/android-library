Tealium Android Library - 3 &amp; 3c
=====================================

**********************
![](../../wiki/images/warning_30.png) Upgrading from an earlier version? See the [Upgrade Notice](#upgrade-notice)
**********************

### Brief ###

These frameworks provide the means to tag native Android applications for the purposes of leveraging [Tealium's tag management platform (Tealium IQ)](http://tealium.com/products/enterprise-tag-management/). 

Tealium's [mobile solution](http://tealium.com/products/enterprise-tag-management/mobile/) permits an app to add, remove or edit analytic services remotely, in real-time, without requiring a code rebuild or new release to take effect.

###Table of Contents###

- [Requirements](#requirements)
- [Quick Start](#quick-start)
    - [1. Clone/Copy Library](#1-clonecopy-library)
    - [2. Add to Project](#2-add-to-project)
    - [3. Run](#3-run)
    - [4. Use Proxy to Verify (optional)](#4-use-proxy-to-verify-optional)
- [What Next](#what-next)
- [Contact Us](#contact-us)
- [Switching Between Full and Compact](#switching-between-full-and-compact)

###Requirements###

- [Android ADT Bundle with Eclipse](http://developer.android.com/sdk/index.html)
- Minimum target Android Version:
 - 8 (AVD tested)
 - 9 (Live device tested)
 - 14 (Recommended)

### Quick Start ###
This guide presumes you have already created an [Android app using Eclipse](https://developer.android.com/training/basics/firstapp/index.html?hl=it). Follow the below steps to add Tealium's Compact library (3c) to it.  Discussion on which version is ultimately best for you can be found in the [What Next](#what-next) section.

####1. Clone/Copy Library####
onto your dev machine by clicking on the *Clone to Desktop* or *Download ZIP* buttons on the main repo page.

![](../../wiki/images/android_githubclone.png)

####2. Add To Project 

2a. Create a "libs" directory in your project root, if not already present. 

2b. From the *android-library/TealiumCompact* folder, drag & drop the [tealium.3c.jar](TealiumCompact/tealium.3c.jar) file into your Eclipse project's Package Explorer window.

![](../../wiki/images/android_addtoproject.png)

2c. Click "Ok" in the resulting File Operation dialog box.

![](../../wiki/images/android_copylinkbox.png)

2d. [Add the following Permissions](http://developer.android.com/guide/topics/manifest/uses-permission-element.html) to your project:

- android.permission.INTERNET
- android.permission.ACCESS_NETWORK_STATE

Your project's AndroidManifest.xml's Permission's tab should now look similar to:
![](../../wiki/images/android_permissions.png)

2e. Import the library into your project's primary application class:

```java
package com.example.myapp;

import android.app.Application;
import com.tealium.library.Tealium;
```

2f. Init the library in the same application class:

```java
public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
        // Must initialize after the super.onCreate() call.
        
        Tealium.initialize(Tealium.Config.create(this, "tealiummobile", "demo", "dev")
			.setLibraryLogLevel(Tealium.LogLevel.DEBUG));
		
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

2g. Ensure the **AndroidManifest.xml** has been updated to use this Application subclass:
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

2h. [*Tealium.onResume(Activity)*](../../wiki/API-Tealium#void-onresumeactivity-activity) and [*TealiumOnPause(Activity)*](../../wiki/API-Tealium#void-onpauseactivity-activity) methods will need to be added to each of your activity classes if you minimum SDK &lt; 14 (ICE CREAM SANDWICH).

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


####3. Run
Your app is now ready to compile and run.  In the console output you should see a variation of:

![](../../wiki/images/android_console.png)

Congratulations! You have successfully implemented the Tealium Compact library into your project.

This output:

```
04-17 11:51:20.525: D/Tealium(2599): view : {
04-17 11:51:20.525: D/Tealium(2599):  "object_class": "MainActivity"
04-17 11:51:20.525: D/Tealium(2599):  "screen_title": "Tealium Example"
04-17 11:51:20.525: D/Tealium(2599):  "tealium_id": "SEYco"
04-17 11:51:20.525: D/Tealium(2599): }
```

shows an abbreviation of all of the data gathered, use ```Tealium.Config.setLibraryLogLevel(LogLevel.VERBOSE)``` to see all datasources available for mapping in Tealium's IQ Dashboard. The Library only actually sends those data sources and values that are mapped.

####4. Use Proxy to verify (optional)

Full Library can be tested using [AudienceStream](http://tealium.com/products/data-distribution/audiencestream/) Trace, if it's available for your account. Contact your account manager for more information.

You can use an HTTP proxy to confirm successful retrieval of configuration data from our multi-CDN and to confirm successful delivery of a tracking call. Several popular third-party options are:

- [Charles Proxy](http://www.charlesproxy.com)
- [Wireshark](http://www.wireshark.org)
- [HTTP Scoop](http://www.tuffcode.com)

Tealium's multi-CDN configuration address is *http://tags.tiqcdn.com*.  You may have to use the
[Tealium.Config.setHTTPSEnabled(boolean)](../../wiki/API-Tealium.Config#tealiumconfig-sethttpsenabledboolean-isenabled) method and set to *false* when you initialize the library to permit proxying.

If you have access to the Tealium Community site, detailed instructions on how to setup Charles Proxy on an iDevice can be found at: https://community.tealiumiq.com/posts/624994

Alternatively, you can use an analytic service with real-time reporting to confirm delivery of dispatches.  This verification method requires both an active analytics account (i.e. [Google Analytics](http://www.google.com/analytics/)) and an active [Tealium IQ](http://tealium.com) account to enable mapping.  If you have both of these, consult the Tealium community post at: https://community.tealiumiq.com/posts/568700

###Switching Between Full and Compact

Swapping the [tealium.3.jar](TealiumFull/tealium.3.jar) with [tealium.3c.jar](TealiumCompact/tealium.3c.jar) (or vice versa) is simple; just replace the undesired library in the *libs/* directory with the desired library. Since the Full and Compact libraries have identical APIs; the swap will produce no errors.

### What Next###
Now that you've successfully integrated the library, you should now determine if the [Compact or Full Library versions](../../wiki/compact-vs-full) best fit your needs. Below are the key differences:


|     |Compact  |  Full
-------------------------------------|:-------------------------------:|:----:
jar size                                                            |84 KB | 139 KB
Initialization time                                                 |~ 10 ms | ~ 10 ms
Memory Usage                                                        |~ 4 KB |~ 4 KB
[Non-UI AutoTracking](../../wiki/Advanced-Guide#non-ui-autotracking)                |Yes |  Yes
[UI Autotracking](../../wiki/Advanced-Guide#ui-autotracking)                        |No  |  Yes
[Mobile Companion](../../wiki/advanced-guide#mobile-companion)  |No  |  Yes
[Mobile AudienceStream Trace](../../wiki/Advanced-Guide#audiencestream-trace)  |No  |  Yes

Continue with the Compact version, add any needed [additional tracking calls](../../wiki/advanced-guide#universal-track-call) for events or view appearances.

Still can't decide? Browse through our [wiki pages](../../wiki/home) for more info, or check out our [TealiumIQ Community](https://community.tealiumiq.com/series/3333)

### Contact Us ###
Questions or comments?

- Post code questions in the [issues page.](../../issues)
- Email us at [mobile_support@tealium.com](mailto:mobile_support@tealium.com)
- Contact your Tealium account manager

### Upgrade Notice

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


