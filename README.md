# MS4Plugin

A third-party Phonegap plugin that implements the Moodstocks v4 SDK for Android and iOS. It is developed by Thomas Forth at [imactivate](http://www.imactivate.com/) and tested for Android with Phonegap v3.1, on Windows and Mac, in Eclipse. Small changes in installation procedure may be required with newer version of Phonegap but we foresee no major compatibility issues. The phonegap camera plugin requires that the Android SDK version used for development is 4.4.x or newer.
iOS compatability has been tested with OS X Yosemite, and Phonegap 4.3 and we see no problems.

Until I have a complete installation guide [watch this video for Android installation](https://www.youtube.com/watch?v=TgIBX6r1nl4).
And [watch this video for iOS installation](https://www.youtube.com/watch?v=TgIBX6r1nl4).

#### Installation (Android)
Installation is via the Phonegap CLI as with the v3.7 plugin but requires **slightly different steps** later in the installation.

1. The native-code portion of the Moodstocks v4.x.x SDK (Eclipse) needs [downloading](https://moodstocks.com/downloads/) and adding to the project.
2. The MainActivity.java class needs moving to the `your.package.name` package you chose when creating your project using the Phonegap CLI.
3. Replace the `_yourKey_` and `_yourSecret_` placedholders in MainActivity.java with your Moodstocks key and secret.
4. All references to `com.imactivate.MS4TOM` need changing to `your.package.name`. This includes in the `AndroidManifest.xml` and in each Java file including `import com.imactivate.MS4TOM.R`.
5. In `AndroidManifest.xml` the `android:name="<name>"` property of the main activity to `android:name="MainActivity"`.
6. (Depending on the phonegap version you use some or all of these changes to `AndroidManifest.xml` may not be required). The `android:debuggable="true"` property needs removing. The `android:minSdkVersion="10"` property needs changing to `android:minSdkVersion="11"`. Optionally, replace `android:theme="@android:style/Theme.Black.NoTitleBar"` with `android:theme="@android:style/Theme.Holo.NoActionBar"`.
7. The camera plugin now requires an SDK with API>18. If you are using an older SDK version right-click on the project title in Eclipse and select Properties -> Android. Then change the Project Build Target to a Platform version of at least 4.4.2 (API Level 19). You can safely delete the `import org.apache.cordova.file.FileUtils` line in `CameraLauncher.java` if you still have issues.

##### Installing the Demo (Android)
Your project should now compile and install. Additionally you may want to install the Phonegap v4 Demo App.

In `assets/www/` replace `index.html` with the `index.html` in the [Moodstocks v4 Phonegap Demo](https://github.com/thomasforth/MS4Plugin/) and add the content of the Demo's `img` folder to `assets/www/img`.

#### Installation (iOS)
Installation on iOS is easy. See the video.

1. Create a new phonegap iOS project.
2. Add the Moodstocks SDK Framework.
3. In `MainViewController.m`, find the `webViewDidfinishLoad` method and add `self.webView.backgroundColor = [UIColor clearColor];` and `self.webView.opaque = NO;` to the method body. This makes the MainViewController transparent so they ScannerViewController is visible through it.
4. In `MS4Plugin.m` change the values of `MS_API_KEY` and `MS_API_SECRET`.
5. (optional) In `Staging/config.xml` change the `DisallowOverscroll` property to `true`.

##### Installing the Demo (iOS)
Your project should now compile and install. Additionally you may want to install the Phonegap v4 Demo App.

In `Staging/www/` delete `index.html` and replace it with the `index.html` from the [Moodstocks v4 Phonegap Demo](https://github.com/thomasforth/MS4Plugin/). Add the content of the Demo's `img` folder to `Staging/www/img`.

#### Compatibility
At the Phonegap level the API is largely unchanged from the [Moodstocks v3.7 phonegap plugin](https://github.com/Moodstocks/moodstocks-phonegap-plugin) and porting should be straightforward. 

The `scanOptions` object now requires a `scanType` to be defined. This can be either `auto` or `manual`. Both use cases are included in the demo app. Bundle loading is supported but not required. A partial implementation is included in the demo app but not activated. Camera roll recognition is supported by a completely new function and demonstrated in the demo app.

The `scanSuccess` function now returns a JSON object with three properties, `format`, `value`, and `recognisedFrame`. Currently `recognisedFrame` is unimplemented.

This release drops support for Android 2.x. Users who require this should use the [Moodstocks v3.7 phonegap plugin](https://github.com/Moodstocks/moodstocks-phonegap-plugin). Developed with Phonegap 3.1, in Eclipse, on Windows 8.1. Android Studio is not supported; Phonegap is the barrier to this.

##### Camera issues
On 2015/04/15 the package id of the default cordova camera plugin changed from org.apache.cordova.camera to cordova-plugin-camera. We have updated our plugin and this should cause no problem but is worth noting.

#### Major architecture changes (Android)
Separate tasks for scanning and the main UI have been replaced with a single task containing multiple fragments -- one for Manual Scanning, one for Automatic Scanning, and one for the phonegap webview. The main task contains the Moodstocks Scanner object and handles synchronisation. The phonegap webview is loaded on application launch and scanner fragments are loaded when required and destroyed when closed.

A new callback system has been implemented which does not rely on the `startActivityForResult` method typical in Phonegap plugins. This is necessary because of the move from Tasks to Fragments.

#### Major architecture changes (iOS)
The old pattern of attaching and detaching the cordovaWebview to the scanningView has been replaced with a much more efficient and reliable pattern. Now, the MainViewController contains the cordovaWebivew and is transparent. It runs on top of a separate ScannerViewController that handles scanning which is loaded and unloaded behind it when needed.

A new callback system has been implemented which uses NSNotificationCenter to communicate between the ScannerViewController and the cordovaWebview within the MainViewController.

#### Unimplemented features
We have not yet implemented Bundles for iOS. This is coming soon and we foresee no difficulties. If you need this *now*, please get in touch.

We do not return the frames that were recognised. We have this feature running on both iOS and Android for both Manual and Auto scanner sessions but difficulties implementing the feature on iOS mean we are not making this publicly available. Specifically, returning query frames in iOS greatly slowed recognition resulting in unacceptable performance. Significant architecture changes will be required to allow the user to specify whether they want the queryFrames returned or not so this slowdown could not be avoided in the majority of cases where the user does not want the queryFrame returned. If you need this feature, please get in touch and I will be happy to work with you to make it happen.

#### Copyright

Copyright (c) 2015 [Moodstocks SAS](http://www.moodstocks.com/) and [imactivate](http://www.imactivate.com/)