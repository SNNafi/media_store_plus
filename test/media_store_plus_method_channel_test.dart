import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:media_store_plus/media_store_plus_method_channel.dart';

void main() {
  MethodChannelMediaStorePlus platform = MethodChannelMediaStorePlus();
  const MethodChannel channel = MethodChannel('media_store_plus');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
