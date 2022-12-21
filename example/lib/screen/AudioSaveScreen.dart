import 'dart:io';

import 'package:audioplayers/audioplayers.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:media_store_plus/media_store_plus.dart';
import 'package:path_provider/path_provider.dart';

import '../main.dart';
import '../util.dart';

class AudioSaveScreen extends StatefulWidget {
  final DirType dirType;

  const AudioSaveScreen({Key? key, required this.dirType}) : super(key: key);

  @override
  State<AudioSaveScreen> createState() => _AudioSaveScreenState();
}

class _AudioSaveScreenState extends State<AudioSaveScreen> {
  bool _isSavingTaskOngoing = false;
  bool _audioAvailable = false;
  String _fileUri = "";

  AudioPlayer? player;

  @override
  void initState() {
    super.initState();
    checkIfExist();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(
            "${widget.dirType == DirType.audio ? "Audio" : "Download"} Folder Example"),
      ),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Text("Audio can be played here after saving"),
              ElevatedButton(
                onPressed: () async {
                  final Uri? uri = await mediaStorePlugin.getFileUri(
                      fileName: "ayat_ul_kursi.mp3",
                      dirType: widget.dirType,
                      dirName: widget.dirType.defaults);
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
                        File(directory.path + "/" + "ayat_ul_kursi.mp3");
                    await (await rootBundle.load("assets/ayat_ul_kursi.mp3"))
                        .writeToFile(tempFile);
                    final bool status = await mediaStorePlugin.saveFile(
                        tempFilePath: tempFile.path,
                        dirType: widget.dirType,
                        dirName: widget.dirType.defaults);
                    setState(() {
                      _isSavingTaskOngoing = false;
                      _audioAvailable = status;
                    });
                  },
                  child: Text("Save Audio"),
                ),
              if (_isSavingTaskOngoing)
                Padding(
                  padding: const EdgeInsets.all(20),
                  child: CircularProgressIndicator(),
                ),
              if (_audioAvailable)
                ElevatedButton(
                    onPressed: () async {
                      player?.stop();
                      player?.dispose();
                      setState(() {
                        _audioAvailable = false;
                      });

                      final bool status = await mediaStorePlugin.deleteFile(
                          fileName: "ayat_ul_kursi.mp3",
                          dirType: widget.dirType,
                          dirName: widget.dirType.defaults);
                      print("Delete Status: $status");

                      if (status) {
                        ScaffoldMessenger.of(context)
                            .showSnackBar(const SnackBar(
                          content: Text('File Deleted!'),
                        ));
                      }
                    },
                    child: Text("Delete")),
              if (_audioAvailable)
                Column(
                  mainAxisAlignment: MainAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    ElevatedButton(
                        onPressed: () async {
                          if (player == null) {
                            player = AudioPlayer();
                            await player?.play(DeviceFileSource(getFile(
                                    fileName: "ayat_ul_kursi.mp3",
                                    dirType: widget.dirType,
                                    dirName: widget.dirType.defaults)
                                .path));
                          }
                        },
                        child: Text("Play")),
                    ElevatedButton(
                        onPressed: () async {
                          player?.pause();
                        },
                        child: Text("Pause")),
                  ],
                )
            ],
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    player?.stop();
    player?.release();
    super.dispose();
  }

  Future<void> checkIfExist() async {
    print("checkIfExist");

    // Using direct path
    // File file = getFile(
    //     fileName: "ayat_ul_kursi.mp3",
    //     dirType: widget.dirType,
    //     dirName: widget.dirType.defaults);
    //
    // if ((await file.exists())) {
    //   setState(() {
    //     _audioAvailable = true;
    //   });
    // }

    // Using uri

    final Uri? uri = await mediaStorePlugin.getFileUri(
        fileName: "ayat_ul_kursi.mp3",
        dirType: widget.dirType,
        dirName: widget.dirType.defaults);

    if (uri != null) {
      File tempFile = File((await getApplicationSupportDirectory()).path + "/" + "ayat_ul_kursi.mp3");
      // read using uri
      bool status = await mediaStorePlugin.readFile(
          fileName: "ayat_ul_kursi.mp3",
          tempFilePath: tempFile.path,
          dirType: widget.dirType,
          dirName: widget.dirType.defaults);
      
      if (status) {
        player = AudioPlayer();
        player?.play(BytesSource(await tempFile.readAsBytes()));
        player?.pause();
        setState(() {
          _audioAvailable = true;
        });
      }
      
    }
  }
}
