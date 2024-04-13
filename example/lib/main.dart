import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:media_store_plus/media_store_plus.dart';
import 'package:media_store_plus_example/screen/AudioSaveScreen.dart';
import 'package:media_store_plus_example/screen/ImageSaveScreen.dart';
import 'package:media_store_plus_example/screen/ReadWriteScreenAPI33OrUp.dart';
import 'package:media_store_plus_example/screen/VideoSaveScreen.dart';
import 'package:permission_handler/permission_handler.dart';

final mediaStorePlugin = MediaStore();

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  // From API 33, we request photos, audio, videos permission to read these files. This the new way
  // From API 29, we request storage permission only to read access all files
  // API lower than 30, we request storage permission to read & write access access all files

  // For writing purpose, we are using [MediaStore] plugin. It will use MediaStore or java File based on API level.
  // It will use MediaStore for writing files from API level 30 or use java File lower than 30
  List<Permission> permissions = [
    Permission.storage,
  ];

  if ((await mediaStorePlugin.getPlatformSDKInt()) > 29) {
    permissions.add(Permission.photos);
    permissions.add(Permission.audio);
    permissions.add(Permission.videos);
  }

  await permissions.request();
  // we are not checking the status as it is an example app. You should (must) check it in a production app

  // You have set this otherwise it throws AppFolderNotSetException
  MediaStore.appFolder = "MediaStorePlugin";


  runApp(
    MaterialApp(
      theme: ThemeData(primarySwatch: Colors.teal),
      home: const MyApp(),
    ),
  );
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool _isLoading = false;
  int _platformSDKVersion = 0;
  StorageVolume _selectedVolume = StorageVolume.primary;
  List<StorageVolume> _availableVolumes = [StorageVolume.primary];

  @override
  void initState() {
    super.initState();
    _getValues();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> _getValues() async {
    setState(() {
      _isLoading = true;
    });

    int platformSDKVersion;
    List<StorageVolume> availableVolumes = [StorageVolume.primary];

    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformSDKVersion = await mediaStorePlugin.getPlatformSDKInt();
      availableVolumes =
          await mediaStorePlugin.getAvailableStorageDirectories();
    } on PlatformException {
      platformSDKVersion = -1;
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformSDKVersion = platformSDKVersion;
      _availableVolumes = availableVolumes;
    });

    setState(() {
      _isLoading = false;
    });

  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('media_store_plus_example'),
      ),
      body: _isLoading
          ? const Center(
              child: CircularProgressIndicator(),
            )
          : Center(
              child: Column(
                mainAxisSize: MainAxisSize.max,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text.rich(
                    TextSpan(text: 'Running on: ', children: [
                      TextSpan(
                          text: _platformSDKVersion.toString(),
                          style: const TextStyle(
                            fontWeight: FontWeight.bold,
                          )),
                    ]),
                  ),
                  const SizedBox(
                    height: 10,
                  ),
                  const Text(
                    'Save file in...',
                    style: TextStyle(fontWeight: FontWeight.bold, fontSize: 20),
                  ),
                  const SizedBox(
                    height: 20,
                  ),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Text(
                        'Select Volume:',
                        style: TextStyle(
                            fontWeight: FontWeight.w500, fontSize: 16),
                      ),
                      const SizedBox(
                        width: 20,
                      ),
                      DropdownButton(
                        value: _selectedVolume,
                        icon: const Icon(Icons.keyboard_arrow_down),
                        items: _availableVolumes.map((StorageVolume item) {
                          return DropdownMenuItem(
                            value: item,
                            child: Text(item.name),
                          );
                        }).toList(),
                        onChanged: (StorageVolume? newValue) {
                          setState(() {
                            _selectedVolume = newValue!;
                          });
                        },
                      ),
                    ],
                  ),
                  const SizedBox(
                    height: 10,
                  ),
                  ElevatedButton(
                    onPressed: () {
                      Navigator.of(context)
                          .push(MaterialPageRoute(builder: (context) {
                        return ImageSaveScreen(
                          volume: _selectedVolume,
                        );
                      }));
                    },
                    child: const Text("Image Folder"),
                  ),
                  ElevatedButton(
                    onPressed: () {
                      Navigator.of(context)
                          .push(MaterialPageRoute(builder: (context) {
                        return AudioSaveScreen(
                          dirType: DirType.audio,
                          volume: _selectedVolume,
                        );
                      }));
                    },
                    child: const Text("Audio Folder"),
                  ),
                  ElevatedButton(
                    onPressed: () {
                      Navigator.of(context)
                          .push(MaterialPageRoute(builder: (context) {
                        return VideoSaveScreen(
                          volume: _selectedVolume,
                        );
                      }));
                    },
                    child: const Text("Video Folder"),
                  ),
                  ElevatedButton(
                    onPressed: () {
                      Navigator.of(context)
                          .push(MaterialPageRoute(builder: (context) {
                        return AudioSaveScreen(
                          dirType: DirType.download,
                          volume: _selectedVolume,
                        );
                      }));
                    },
                    child: const Text("Download Folder"),
                  ),
                  ElevatedButton(
                    onPressed: () {
                      Navigator.of(context)
                          .push(MaterialPageRoute(builder: (context) {
                        return const ReadWriteScreenAPI33OrUp();
                      }));
                    },
                    child: const Text("Read/Write API 33 or Upper Folder"),
                  ),
                ],
              ),
            ),
    );
  }
}
