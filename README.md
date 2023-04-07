# media_store_plus

To use Android `MediaStore` API in Flutter.
It supports both read & write operation in every android version through flutter.
It also requests for appropriate permissions, if needed.

# Motivation

From API level __30__, we have to use __Scoped Storage__ to write file. Though, we can read all files by direct path until API level __32__, from API level __33__ we need to use __Scoped Storage__ for reading also.

We can write in DCIM, Pictures, Movies, Alarms, Audiobooks, Music, Podcasts, Ringtones, Download folders by `MediaStore` without any kinds of Storage permission. You can also read without any permission from these folders as long as the files are created by the app. But if we uninstall the app, and install it again, then, it will lose the read/write access for the files previously created by the app.

Again, we can read all files by requesting `android.permission.READ_EXTERNAL_STORAGE` permission until API level 32.From API level 33, it has no usage. Android introduces three specific permission i.e. `android.permission.READ_MEDIA_IMAGES`, `android.permission.READ_MEDIA_AUDIO` & `android.permission.READ_MEDIA_VIDEO` to read audio, video & images.

But, we can't read other folders from API level 33 without requesting explicit permission for those folders.

__Sum up, From flutter there is no way write in any folder with/without storage permission other than app data folder!__

Because, we can't use `MediaStore` API from flutter, that is required for writing, other than app data folder. Again we can't request to read/write any specfic folder using file picker from flutter.

So, only solution to use platform channel for this. Actually, this issue lead me to create this __media_store_plus__ plugin.

# Usage

You can read, write, edit, delete in the DCIM, Pictures, Movies, Alarms, Audiobooks, Music, Podcasts, Ringtones, Download with this plugin.
You can also request to read or write any specific folder other than these mentioned above, by file picker with this plugin.
Again when you will try to read, edit or delete a file that is not created by your app, it will request permission from user for that task automatically.

__You can read the full documentation from [here](https://pub.dev/documentation/media_store_plus/latest/).__ You can also check the example app for implementation. Reading the plugin's source code also help you in that case.

# Getting Started

First, add `media_store_plus` as a dependency in your pubspec.yaml file.

```yaml
dependencies:
  media_store_plus: ^0.0.7
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

# Contribution

You can create issue(s) for any missing feature(s) that is relevant to this plugin.You can also help by pointing out any bugs. Pull requests are also welcomed

# Status

This is an active project as `MediaStore` is the future of accessing files in android. There's a lot of rooms to improve this plugin.


# Support the package (optional)

If you find this package useful, you can support it by giving it a star.

# Credits

This package is developed by [Shahriar Nasim Nafi](https://github.com/SNNafi)
