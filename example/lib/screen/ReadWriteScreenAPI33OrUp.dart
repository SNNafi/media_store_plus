import 'dart:io' show Directory, File;

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
  bool _isTestDone = false;
  String _fileUri = "";
  final String fileName = 'te  %st.txt';
  String _text = '';

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Read Folder API 33 or Upper Example"),
      ),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: () async {
                  testReadWriteInApi33Plus();
                },
                child: const Text("Test"),
              ),
              if (_isTestDone) ...<Widget>[
                Text(_text),
                const Text.rich(
                  TextSpan(text: 'Check ', children: [
                    TextSpan(
                        text: 'console',
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                        )),
                    TextSpan(text: ' for detailed info')
                  ]),
                )
              ]
            ],
          ),
        ),
      ),
    );
  }

  Future<void> testReadWriteInApi33Plus() async {
    print("testReadWriteInApi33Plus");
    File file = File("/storage/emulated/0/Download/TestD/$fileName");
    print(file.existsSync());
    if ((await file.exists())) {
      print("True ${file.path}");

      _text = await readOrWriteApiLevel33WithPermission(
              fileName: fileName,
              initialRelativePath: "Download/TestD",
              operation: () async {
                print('Operation');
                final text = await file.readAsString();
                print(text);
              }) ??
          '';

      setState(() {
        _isTestDone = true;
      });
    }
  }
}
