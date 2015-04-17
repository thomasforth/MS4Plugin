# MS4Plugin

A third-party Phonegap plugin that implements the Moodstocks v4 SDK for Android. It is developed by Thomas Forth at [imactivate](http://www.imactivate.com/) and tested on Phonegap v3.1, on Windows and Mac, in Eclipse. Small changes in installation procedure may be required with newer version of Phonegap but we foresee no major compatibility issues.

Until I have a complete installation guide [watch this video](https://www.youtube.com/watch?v=TgIBX6r1nl4).

#### Installation
Installation is via the Phonegap CLI as with the v3.7 plugin but requires **slightly different steps** later in the installation.

1. The native-code portion of the Moodstocks v4.x SDK (Eclipse) needs [downloading](https://moodstocks.com/downloads/) and adding to the project.
2. The MainActivity.java class needs moving to the `your.package.name` package you chose when creating your project using the Phonegap CLI.
3. Replace the `_yourKey_` and `_yourSecret_` placedholders in MainActivity.java with your Moodstocks key and secret.
4. All references to `com.imactivate.MS4TOM` need changing to `your.package.name`. This includes in the `AndroidManifest.xml` and in each Java file including `import com.imactivate.MS4TOM.R`.
5. In `AndroidManifest.xml` the `android:name="<name>"` property of the main activity to `android:name="MainActivity"`.
6. (Depending on the phonegap version you use some or all of these changes to `AndroidManifest.xml` may not be required). The `android:debuggable="true"` property needs removing. The `android:minSdkVersion="10"` property needs changing to `android:minSdkVersion="11"`. Optionally, replace `android:theme="@android:style/Theme.Black.NoTitleBar"` with `android:theme="@android:style/Theme.Holo.NoActionBar"`.
7. As of 2015/04/15 you may encounter a problem with the camera plugin dependency. This is detailed below.

##### Installing the Demo
Your project should now compile and install. Additionally you may want to install the Phonegap v4 Demo App.

In `assets/www/` replace `index.html` with the `index.html` in the [Moodstocks v4 Phonegap Demo](https://github.com/thomasforth/MS4Plugin/) and add the content of the Demo's `img` folder to `assets/www/img`.

#### Compatibility
At the Phonegap level the API is largely unchanged from the [Moodstocks v3.7 phonegap plugin](https://github.com/Moodstocks/moodstocks-phonegap-plugin) and porting should be straightforward. 

The `scanOptions` object now requires a `scanType` to be defined. This can be either `auto` or `manual`. Both use cases are included in the demo app. Bundle loading is supported but not required. A partial implementation is included in the demo app but not activated. Camera roll recognition is supported by a completely new function and demonstrated in the demo app.

This release drops support for Android 2.x. Users who require this should use the [Moodstocks v3.7 phonegap plugin](https://github.com/Moodstocks/moodstocks-phonegap-plugin). Developed with Phonegap 3.1, in Eclipse, on Windows 8.1. Android Studio is not supported; Phonegap is the barrier to this.

#### Camera issues
On 2015/04/15 the package id of the default cordova camera plugin changed from org.apache.cordova.camera to cordova-plugin-camera. We have updated our plugin. If you encounter problems you may be able to fix them by doing the following.

Right click on the project title in Eclipse and select Properties -> Android. Then change the Project Build Target to Platform 4.4.2 (API Level 19). This will enable newer features.

Delete the following line in the CameraLauncher.java file:
import org.apache.cordova.file.FileUtils;
This is not needed for functionality.


#### Major architecture changes
Separate tasks for scanning and the main UI have been replaced with a single task containing multiple fragments -- one for Manual Scanning, one for Automatic Scanning, and one for the phonegap webview. The main task contains the Moodstocks Scanner object and handles synchronisation. The phonegap webview is loaded on application launch and scanner fragments are loaded when required and destroyed when closed.

A new callback system has been implemented which does not rely on the `startActivityForResult` method typical in Phonegap plugins. This is necessary because of the move from Tasks to Fragments.

#### iOS Support
For now the v4 plugin works on Android. If you’re a confident iOS developer and you’d like to team up on an iOS version of the plugin let's talk!

#### Copyright

Copyright (c) 2015 [Moodstocks SAS](http://www.moodstocks.com/) and [imactivate](http://www.imactivate.com/)