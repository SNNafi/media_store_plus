# media_store_plus

To use Android `MediaStore` API in Flutter.
It supports both read & write operations in every Android version through Flutter.
It also requests appropriate permissions, if needed.

# Motivation

From API level __30__, we have to use __Scoped Storage__ to write files. Though, we can read all files by direct path until API level __32__, from API level __33__ we need to use __Scoped Storage__ for reading also.

We can write in DCIM, Pictures, Movies, Alarms, Audiobooks, Music, Podcasts, Ringtones, and Download folders by `MediaStore` without any kind of Storage permission. You can also read without any permission from these folders as long as the files are created by the app. But if we uninstall the app, and install it again, then, it will lose the read/write access for the files previously created by the app.

Again, we can read all files by requesting `android.permission.READ_EXTERNAL_STORAGE` permission until API level 32.From API level 33, it has no usage. Android introduces three specific permissions i.e. `android.permission.READ_MEDIA_IMAGES`, `android.permission.READ_MEDIA_AUDIO` & `android.permission.READ_MEDIA_VIDEO` to read audio, video & images.

But, we can't read other folders from API level 33 without requesting explicit permission for those folders.

__Sum up, from Flutter there is no way to write in any folder with/without storage permission other than the app data folder!__

Because, we can't use the `MediaStore` API from Flutter, that is required for writing, other than the app data folder. Again we can't request to read/write any specific folder using a file picker from Flutter.

So, the only solution is to use the platform channel for this. This issue led me to create this __media_store_plus__ plugin.

# Usage

You can read, write, edit, and delete in the DCIM, Pictures, Movies, Alarms, Audiobooks, Music, Podcasts, Ringtones, Download with this plugin.
You can also request to read or write any specific folder other than those mentioned above, by file picker with this plugin.
Again when you try to read, edit, or delete a file that is not created by your app, it will automatically request permission from the user for that task.

__You can read the full documentation from [here](https://pub.dev/documentation/media_store_plus/latest/).__ You can also check the example app for implementation. Reading the plugin's source code also helps you in that case.

# Getting Started

First, add `media_store_plus` as a dependency in your pubspec.yaml file.

```yaml
dependencies:
  media_store_plus: ^0.1.3
```

Don't forget to `flutter pub get`.

# Android Side

Edit the `AndroidManifest.xml` like this.

```xml
    <!-- required from API level 33 -->
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" /> <!-- To read images created by other apps -->
    <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" /> <!-- To read audios created by other apps -->
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" /> <!-- To read vidoes created by other apps -->

    <uses-permission
        android:name="android.permission.READ_EXTERNAL_STORAGE" <!-- To read all files until API level 32 -->
        android:maxSdkVersion="32" />

    <uses-permission
        android:name="android.permission.WRITE_EXTERNAL_STORAGE" <!-- To write all files until API level 29. We will MediaStore from API level 30 -->
        android:maxSdkVersion="29" />

    <application
        ---------------------------
        android:requestLegacyExternalStorage="true"> 
        <!-- Need for API level 29. Scoped Storage has some issue in Android 10. So, google recommanded to add this. -->
        <!-- Read more from here: https://developer.android.com/training/data-storage/shared/media#access-other-apps-files-->
    </application>
```

__You need to modify the `proguard-rules.pro` file as per [this](https://github.com/google/gson/blob/main/examples/android-proguard-example/proguard.cfg) link. Because this plugin uses GSON internally.__

# Contribution

You can create issue(s) for any missing feature(s) that is relevant to this plugin. You can also help by pointing out any bugs. Pull requests are also welcomed

# Status

This is an active project as `MediaStore` is the future of accessing files in Android. There's a lot of room to improve this plugin.


# Support the package (optional)

If you find this package useful, you can support it by giving it a star.

# Credits

This package is developed by [Shahriar Nasim Nafi](https://github.com/SNNafi)
