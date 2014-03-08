## Tealium Android Library Version 2.1

The sets of files herein contain all the working files, and documentation available for implementing the Tealium Android Library in your Android projects.

Detailed instructions can be found within the included JavaDocs at android-library/doc/index.html, which is viewable with any modern web browser.

MINIMUM SDK: 8 (AVD tested)
MINIMUM SDK: 9 (live device tested)
TARGET SDK: 19

[Implementation guide](https://community.tealiumiq.com/posts/747550)
[API Reference](https://community.tealiumiq.com/posts/771744)

###### dalvik LOGCAT Messages Caused by Library

E/dalvikvm(****): Could not find class 'android.app.Application$ActivityLifecycleCallbacks', referenced from method com.tealium.library.Tealium.shutdown.  
W/dalvikvm(****): VFY: unable to resolve check-cast 10 (Landroid/app/Application$ActivityLifecycleCallbacks;) in Lcom/tealium/library/Tealium;  
W/dalvikvm(****): Link of class 'Lcom/tealium/library/c$1;' failed  
E/dalvikvm(****): Could not find class 'com.tealium.library.c$1', referenced from method com.tealium.library.c.<init>  
 - Disregard, there is versioning code preventing run time use of API 14+ interfaces in <14 environments.

#### Support
mobile_support@tealium.com
