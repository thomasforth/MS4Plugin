# MS4Plugin
####A third-party Phonegap plugin for iOS and Android that implements the Moodstocks v4 SDK. 

Developed by Thomas Forth at [imactivate](http://www.imactivate.com/). Tested for Android with Phonegap v3.6, on Windows and Mac, in Eclipse. iOS compatibility tested with OS X Yosemite, and Phonegap 4.3.

[Watch this video for Android installation](https://www.youtube.com/watch?v=TgIBX6r1nl4).

[Watch this video for iOS installation](https://www.youtube.com/watch?v=ZuDFnf8S4NY).

Then check for any additional steps that have been added since recording below.

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

##### Offline Bundles
Offline bundles are supported on both iOS and Android. A *.bundle file should be requested directly from Moodstocks. Place it in the Resources folder for iOS projects and in the Assets folder for Android projects. Bundles must match the API key in MS4Plugin.m for iOS projects and in MainActivity.java for Android projects.

##### Landscape orientation
As of 2015-10-06 the plugin works in portrait and landscape mode on both iOS and Android. Make sure that `useDeviceOrientation` is set true. On iOS you will need to enable Landscape views (Landscape Left and/or Landscape Right) in the the project properties. You’ll find this is in the General>Deployment Info>Device Orientation section.

##### iOS 9 compatibility
As of v4.1.7 The Moodstocks SDK does not include bitcode support. The workaround is simple. http://stackoverflow.com/questions/30848208/new-warnings-in-ios9

##### Cordova 5.x compatibility
We know of people successfully using our plugin with every 3.x and 4.x version of Cordova. Cordova 5.x introduces a large number of small bugs. We have fixed most of them, with a few left to squash. Unfortunately these fixes break compatibility with previous version of Cordova and we expect many of them to fixed within Cordova itself in future 5.x versions. For now, we recommend avoiding Cordova 5.x, and we cannot provide support for it.

##### Camera issues
On 2015/04/15 the package id of the default cordova camera plugin changed from org.apache.cordova.camera to cordova-plugin-camera. We have updated our plugin and this should cause no problem.

#### Compatibility with previous version of the Moodstocks phonegap plugin
*At the Phonegap level the API is largely unchanged from the [Moodstocks v3.7 phonegap plugin](https://github.com/Moodstocks/moodstocks-phonegap-plugin) and porting should be straightforward. 
* The `scanSuccess` function now returns a JSON object with three properties, `format`, `value`, and `recognisedFrame`. Currently `recognisedFrame` is unimplemented.
* The `scanOptions` object now requires a `scanType` to be defined. This can be either `auto` or `manual`. Both use cases are included in the demo app.
* Bundle loading is supported on Android, and soon on iOS. A partial implementation is included in the demo app but not activated.
* Camera roll recognition is supported by a completely new function and demonstrated in the demo app.

This release drops support for Android 2.x. Users who require this should use the [Moodstocks v3.7 phonegap plugin](https://github.com/Moodstocks/moodstocks-phonegap-plugin). Android Studio is not supported; Phonegap is the barrier to this.

#### Major architecture changes (Android)
Separate tasks for scanning and the main UI have been replaced with a single task containing multiple fragments -- one for Manual Scanning, one for Automatic Scanning, and one for the phonegap webview. The main task contains the Moodstocks Scanner object and handles synchronisation. The phonegap webview is loaded on application launch and scanner fragments are loaded when required and destroyed when closed.

A new callback system has been implemented which does not rely on the `startActivityForResult` method typical in Phonegap plugins. This is necessary because of the move from Tasks to Fragments.

#### Major architecture changes (iOS)
The old pattern of attaching and detaching the cordovaWebview to the scanningView has been replaced with a much more efficient and reliable pattern. Now, the MainViewController contains the cordovaWebivew and is transparent. It runs on top of a separate ScannerViewController that handles scanning which is loaded and unloaded behind it when needed.

A new callback system has been implemented which uses NSNotificationCenter to communicate between the ScannerViewController and the cordovaWebview within the MainViewController.

#### Unimplemented features
We do not return the frames that were recognised. We have this feature running on both iOS and Android for both Manual and Auto scanner sessions but difficulties implementing the feature on iOS mean we are not making this publicly available. Specifically, returning query frames in iOS from the native code to the CordovaWebView resulted in unacceptable performance. Significant architecture changes will be required to allow the user to specify whether they want queryFrames returned or not so this slowdown could not be avoided in the majority of cases where the user does not want the queryFrame returned. If you need this feature, please get in touch and I will be happy to work with you to make it happen.

#### Copyright and License

Copyright (c) 2015 [Moodstocks SAS](http://www.moodstocks.com/) and [imactivate](http://www.imactivate.com/).
License is MIT and included.