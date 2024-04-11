import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:media_store_plus/media_store_plus.dart';
import 'package:path_provider/path_provider.dart';
import 'package:video_player/video_player.dart';

import '../main.dart';
import '../util.dart';

class VideoSaveScreen extends StatefulWidget {
  const VideoSaveScreen({Key? key}) : super(key: key);

  @override
  State<VideoSaveScreen> createState() => _VideoSaveScreenState();
}

class _VideoSaveScreenState extends State<VideoSaveScreen> {
  bool _isSavingTaskOngoing = false;
  bool _videoAvailable = false;
  String _fileUri = "";

  late VideoPlayerController? _controller;
  bool startedPlaying = false;

  @override
  void initState() {
    super.initState();
    checkIfExist();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Video Folder Example"),
      ),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              const Text("Video can be played here after saving"),
              ElevatedButton(
                onPressed: () async {
                  final Uri? uri = await mediaStorePlugin.getFileUri(
                      fileName: "kaba_video.mp4",
                      dirType: DirType.video,
                      dirName: DirType.video.defaults);
                  if (uri != null) {
                    setState(() {
                      _fileUri = uri.path;
                    });
                  }
                },
                child: const Text("Get File Uri"),
              ),
              if (_fileUri.isNotEmpty)
                Text.rich(
                  TextSpan(text: 'File Uri: ', children: [
                    TextSpan(
                        text: _fileUri,
                        style: const TextStyle(
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
                        File("${directory.path}/kaba_video.mp4");
                    await (await rootBundle.load("assets/kaba_video.mp4"))
                        .writeToFile(tempFile);
                    final path = await mediaStorePlugin.saveFile(
                        tempFilePath: tempFile.path,
                        dirType: DirType.video,
                        dirName: DirType.video.defaults);
                    print(path);
                    setState(() {
                      _isSavingTaskOngoing = false;
                      _videoAvailable = path != null;
                    });

                    if (path != null) {
                      _controller = VideoPlayerController.file(getFile(
                          fileName: "kaba_video.mp4",
                          dirType: DirType.video,
                          dirName: DirType.video.defaults));
                      _controller?.initialize();
                    }
                  },
                  child: const Text("Save Video"),
                ),
              if (_isSavingTaskOngoing)
                const Padding(
                  padding: EdgeInsets.all(20),
                  child: CircularProgressIndicator(),
                ),
              if (_videoAvailable)
                ElevatedButton(
                    onPressed: () async {
                      _controller?.pause();
                      _controller?.dispose();
                      setState(() {
                        _videoAvailable = false;
                      });

                      final bool status = await mediaStorePlugin.deleteFile(
                          fileName: "kaba_video.mp4",
                          dirType: DirType.video,
                          dirName: DirType.video.defaults);
                      print("Delete Status: $status");

                      if (status) {
                        ScaffoldMessenger.of(context)
                            .showSnackBar(const SnackBar(
                          content: Text('File Deleted!'),
                        ));
                      }
                    },
                    child: const Text("Delete")),
              if (_videoAvailable)
                Container(
                  width: 300,
                  height: 400,
                  child: AspectRatio(
                    aspectRatio: _controller!.value.aspectRatio,
                    child: Stack(
                      alignment: Alignment.bottomCenter,
                      children: <Widget>[
                        VideoPlayer(_controller!),
                        _ControlsOverlay(
                          controller: _controller!,
                        ),
                        VideoProgressIndicator(_controller!,
                            allowScrubbing: true),
                      ],
                    ),
                  ),
                )
            ],
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  Future<void> checkIfExist() async {
    print("checkIfExist");

    // Using path
    // File file = getFile(
    //     fileName: "kaba_video.mp4",
    //     dirType: DirType.video,
    //     dirName: DirType.video.defaults);

    // if ((await file.exists())) {
    //   _controller = VideoPlayerController.file(getFile(
    //       fileName: "kaba_video.mp4",
    //       dirType: DirType.video,
    //       dirName: DirType.video.defaults));
    //   _controller?.initialize();
    //   setState(() {
    //     _videoAvailable = true;
    //   });
    // }

    // Using uri

    final Uri? uri = await mediaStorePlugin.getFileUri(
        fileName: "kaba_video.mp4",
        dirType: DirType.video,
        dirName: DirType.video.defaults);

    if (uri != null) {
      _controller = VideoPlayerController.contentUri(uri);
      _controller?.initialize();
      setState(() {
        _videoAvailable = true;
      });
    }
  }
}

class _ControlsOverlay extends StatelessWidget {
  const _ControlsOverlay({Key? key, required this.controller})
      : super(key: key);

  static const List<Duration> _exampleCaptionOffsets = <Duration>[
    Duration(seconds: -10),
    Duration(seconds: -3),
    Duration(seconds: -1, milliseconds: -500),
    Duration(milliseconds: -250),
    Duration.zero,
    Duration(milliseconds: 250),
    Duration(seconds: 1, milliseconds: 500),
    Duration(seconds: 3),
    Duration(seconds: 10),
  ];
  static const List<double> _examplePlaybackRates = <double>[
    0.25,
    0.5,
    1.0,
    1.5,
    2.0,
    3.0,
    5.0,
    10.0,
  ];

  final VideoPlayerController controller;

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: <Widget>[
        AnimatedSwitcher(
          duration: const Duration(milliseconds: 50),
          reverseDuration: const Duration(milliseconds: 200),
          child: controller.value.isPlaying
              ? const SizedBox.shrink()
              : Container(
                  color: Colors.black26,
                  child: const Center(
                    child: Icon(
                      Icons.play_arrow,
                      color: Colors.white,
                      size: 100.0,
                      semanticLabel: 'Play',
                    ),
                  ),
                ),
        ),
        GestureDetector(
          onTap: () {
            controller.value.isPlaying ? controller.pause() : controller.play();
          },
        ),
        Align(
          alignment: Alignment.topLeft,
          child: PopupMenuButton<Duration>(
            initialValue: controller.value.captionOffset,
            tooltip: 'Caption Offset',
            onSelected: (Duration delay) {
              controller.setCaptionOffset(delay);
            },
            itemBuilder: (BuildContext context) {
              return <PopupMenuItem<Duration>>[
                for (final Duration offsetDuration in _exampleCaptionOffsets)
                  PopupMenuItem<Duration>(
                    value: offsetDuration,
                    child: Text('${offsetDuration.inMilliseconds}ms'),
                  )
              ];
            },
            child: Padding(
              padding: const EdgeInsets.symmetric(
                // Using less vertical padding as the text is also longer
                // horizontally, so it feels like it would need more spacing
                // horizontally (matching the aspect ratio of the video).
                vertical: 12,
                horizontal: 16,
              ),
              child: Text('${controller.value.captionOffset.inMilliseconds}ms'),
            ),
          ),
        ),
        Align(
          alignment: Alignment.topRight,
          child: PopupMenuButton<double>(
            initialValue: controller.value.playbackSpeed,
            tooltip: 'Playback speed',
            onSelected: (double speed) {
              controller.setPlaybackSpeed(speed);
            },
            itemBuilder: (BuildContext context) {
              return <PopupMenuItem<double>>[
                for (final double speed in _examplePlaybackRates)
                  PopupMenuItem<double>(
                    value: speed,
                    child: Text('${speed}x'),
                  )
              ];
            },
            child: Padding(
              padding: const EdgeInsets.symmetric(
                // Using less vertical padding as the text is also longer
                // horizontally, so it feels like it would need more spacing
                // horizontally (matching the aspect ratio of the video).
                vertical: 12,
                horizontal: 16,
              ),
              child: Text('${controller.value.playbackSpeed}x'),
            ),
          ),
        ),
      ],
    );
  }
}
