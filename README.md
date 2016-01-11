Tealium Android Library - 4.1.4c with install referring
=====================================

Add the ```tealium-4.1.4c-referrer.aar``` dependency (instead of the 4.1.4c jar) and integrate the Tealium Android Library as usual. When available, the ```install_referrer``` data source will become available in the data layer. 

**Test using adb:**

```
adb shell

am broadcast -a com.android.vending.INSTALL_REFERRER -n com.tealium.example/com.tealium.install_referrer.InstallReferrerReceiver --es referrer "referrer=foo_store"
```