import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:media_store_plus/media_store_plus.dart';
import 'package:path_provider/path_provider.dart';

import '../main.dart';
import '../util.dart';

class ReadWriteScreenAPI33OrUp extends StatefulWidget {
  const ReadWriteScreenAPI33OrUp({Key? key}) : super(key: key);

  @override
  State<ReadWriteScreenAPI33OrUp> createState() =>
      _ReadWriteScreenAPI33OrUpState();
}

class _ReadWriteScreenAPI33OrUpState extends State<ReadWriteScreenAPI33OrUp> {
  bool _isSavingTaskOngoing = false;
  bool _imageAvailable = false;
  String _fileUri = "";

  @override
  void initState() {
    super.initState();
    checkIfExist();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Read Folder API 33 or Upper Example"),
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
                      fileName: "al_aqsa_mosque.jpeg",
                      dirType: DirType.photo,
                      dirName: DirType.photo.defaults);
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
                    File tempFile =
                        File(directory.path + "/" + "al_aqsa_mosque.jpeg");
                    await (await rootBundle.load("assets/al_aqsa_mosque.jpeg"))
                        .writeToFile(tempFile);
                    final bool status = await mediaStorePlugin.saveFile(
                        tempFilePath: tempFile.path,
                        dirType: DirType.photo,
                        dirName: DirType.photo.defaults);
                    setState(() {
                      _isSavingTaskOngoing = false;
                      _imageAvailable = status;
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
                          fileName: "al_aqsa_mosque.jpeg",
                          dirType: DirType.photo,
                          dirName: DirType.photo.defaults);
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
                  File(
                      "/storage/emulated/0/Download/TestD/al_aqsa_mosque.jpeg"),
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
    File file = File("/storage/emulated/0/Download/TestD/test.txt");

    if ((await file.exists())) {
      print("TRUe ${file.path}");

      await readOrWriteApiLevel33WithPermission(initialRelativePath: "Download/TestD", operation: () async {
       final text = await file.readAsString();
       print(text);
      });

      setState(() {
        // _imageAvailable = true;
      });
    }
  }
}
