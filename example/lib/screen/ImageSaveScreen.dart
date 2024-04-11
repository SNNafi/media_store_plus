import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:media_store_plus/media_store_plus.dart';
import 'package:path_provider/path_provider.dart';

import '../main.dart';
import '../util.dart';

class ImageSaveScreen extends StatefulWidget {
  const ImageSaveScreen({Key? key}) : super(key: key);

  @override
  State<ImageSaveScreen> createState() => _ImageSaveScreenState();
}

class _ImageSaveScreenState extends State<ImageSaveScreen> {
  bool _isSavingTaskOngoing = false;
  bool _imageAvailable = false;
  String _fileUri = "";

  String fileName = "al aqsa_mosque.jpeg";

  /// set `null`to save in relativePath [MediaStore.appFolder]
  String? relativePath = "AnotherFolder";

  @override
  void initState() {
    super.initState();
    checkIfExist();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Image Folder Example"),
      ),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Text("Image will be shown here after saving"),
              ElevatedButton(
                onPressed: () async {
                  final Uri? uri = await mediaStorePlugin.getFileUri(
                    fileName: fileName,
                    dirType: DirType.photo,
                    dirName: DirType.photo.defaults,
                    relativePath: relativePath,
                  );
                  if (uri != null) {
                    setState(() {
                      _fileUri = uri.path;
                    });
                  }
                },
                child: Text("Get File Uri"),
              ),
              if (_fileUri.isNotEmpty)
                Text.rich(
                  TextSpan(text: 'File Uri: ', children: [
                    TextSpan(
                        text: _fileUri,
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                        )),
                  ]),
                ),
              if (!_isSavingTaskOngoing)
                ElevatedButton(
                  onPressed: () async {
                    setState(() {
                      _isSavingTaskOngoing = true;
                    });

                    Directory directory =
                        await getApplicationSupportDirectory();
                    File tempFile = File("${directory.path}/$fileName");
                    await (await rootBundle.load("assets/al_aqsa_mosque.jpeg"))
                        .writeToFile(tempFile);
                    final path = await mediaStorePlugin.saveFile(
                      tempFilePath: tempFile.path,
                      dirType: DirType.photo,
                      dirName: DirType.photo.defaults,
                      relativePath: relativePath,
                    );
                    print(path);
                    setState(() {
                      _isSavingTaskOngoing = false;
                      _imageAvailable = path != null;
                    });
                  },
                  child: Text("Save Image"),
                ),
              if (_isSavingTaskOngoing)
                Padding(
                  padding: const EdgeInsets.all(20),
                  child: CircularProgressIndicator(),
                ),
              if (_imageAvailable)
                ElevatedButton(
                    onPressed: () async {
                      setState(() {
                        _imageAvailable = false;
                      });

                      final bool status = await mediaStorePlugin.deleteFile(
                        fileName: fileName,
                        dirType: DirType.photo,
                        dirName: DirType.photo.defaults,
                        relativePath: relativePath,
                      );
                      print("Delete Status: $status");

                      if (status) {
                        ScaffoldMessenger.of(context)
                            .showSnackBar(const SnackBar(
                          content: Text('File Deleted!'),
                        ));
                      }
                    },
                    child: Text("Delete")),
              if (_imageAvailable)
                Image.file(
                  getFile(
                    fileName: fileName,
                    dirType: DirType.photo,
                    dirName: DirType.photo.defaults,
                    relativePath: relativePath,
                  ),
                  height: 400,
                  width: 300,
                  fit: BoxFit.fitWidth,
                )
            ],
          ),
        ),
      ),
    );
  }

  Future<void> checkIfExist() async {
    print("checkIfExist");
    File file = getFile(
      fileName: fileName,
      dirType: DirType.photo,
      dirName: DirType.photo.defaults,
      relativePath: relativePath,
    );

    if ((await file.exists())) {
      setState(() {
        _imageAvailable = true;
      });
    }
  }
}
