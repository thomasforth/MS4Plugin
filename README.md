# MS4Plugin

A third-party Phonegap plugin that implements the Moodstocks v4 SDK for Android. It is developed by Thomas Forth at [imactivate](http://www.imactivate.com/).

#### Installation
Installation is via the Phonegap CLI **but requires additional steps**.

1. The native-code portion of the Moodstocks v4.x SDK (Eclipse) needs [downloading](https://moodstocks.com/downloads/) and adding to the project.
2. The MainActivity.java class needs moving to the `your.package.name` package you chose when creating your project using the Phonegap CLI.
3. All references to `com.imactivate.MS4TOM` need changing to `your.package.name`. This includes in the `AndroidManifest.xml` and in each Java file including `import com.imactivate.MS4TOM.R`.

A walk-through and examples are provided at [imactivate.com/moodstocksv4](http://www.imactivate.com/moodstocksv4).

#### Compatibility
At the Phonegap level the API is largely unchanged from the [Moodstocks v3.7 phonegap plugin](https://github.com/Moodstocks/moodstocks-phonegap-plugin) and porting should be straightforward. This release drops support for Android 2.x. Users who require this should use the [Moodstocks v3.7 phonegap plugin](https://github.com/Moodstocks/moodstocks-phonegap-plugin). Developed with Phonegap 3.1, in Eclipse, on Windows 8.1. Android Studio is not supported; Phonegap is the barrier to this.

#### Major architecture changes
Separate tasks for scanning and the main UI have been replaced with a single task containing multiple fragments -- one for Manual Scanning, one for Automatic Scanning, and one for the Phonegap webview. The main task contains the Moodstocks Scanner object and handles synchronisation. The phongap webview is loaded on application launch and scanner fragments are loaded when required and destroyed when closed.

#### iOS Support
For now the v4 plugin works on Android. If you’re a confident iOS developer and you’d like to team up on an iOS version of the plugin let's talk!

#### Copyright

Copyright (c) 2015 [Moodstocks SAS](http://www.moodstocks.com/) and [imactivate](http://www.imactivate.com/)