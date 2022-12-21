import 'package:flutter_test/flutter_test.dart';
import 'package:media_store_plus/media_store_plus.dart';
import 'package:media_store_plus/media_store_plus_platform_interface.dart';
import 'package:media_store_plus/media_store_plus_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockMediaStorePlusPlatform
    with MockPlatformInterfaceMixin
    implements MediaStorePlusPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final MediaStorePlusPlatform initialPlatform = MediaStorePlusPlatform.instance;

  test('$MethodChannelMediaStorePlus is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelMediaStorePlus>());
  });

  test('getPlatformVersion', () async {
    MediaStorePlus mediaStorePlusPlugin = MediaStorePlus();
    MockMediaStorePlusPlatform fakePlatform = MockMediaStorePlusPlatform();
    MediaStorePlusPlatform.instance = fakePlatform;

    expect(await mediaStorePlusPlugin.getPlatformVersion(), '42');
  });
}
