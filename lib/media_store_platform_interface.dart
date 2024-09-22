import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'media_store_method_channel.dart';
import 'media_store_plus.dart';

abstract class MediaStorePlatform extends PlatformInterface {
  MediaStorePlatform() : super(token: _token);

  static final Object _token = Object();

  static MediaStorePlatform _instance = MethodChannelMediaStore();

  static MediaStorePlatform get instance => _instance;

  static set instance(MediaStorePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<int> getPlatformSDKInt() {
    throw UnimplementedError('getPlatformSDKInt() has not been implemented.');
  }

  Future<SaveInfo?> saveFile({
    required String tempFilePath,
    required String fileName,
    required DirType dirType,
    required DirName dirName,
    required String relativePath,
  }) {
    throw UnimplementedError('saveFile() has not been implemented.');
  }

  Future<bool> deleteFile({
    required String fileName,
    required DirType dirType,
    required DirName dirName,
    required String relativePath,
  }) {
    throw UnimplementedError('deleteFile() has not been implemented.');
  }

  Future<Uri?> getFileUri({
    required String fileName,
    required DirType dirType,
    required DirName dirName,
    required String relativePath,
  }) {
    throw UnimplementedError('getFileUri() has not been implemented.');
  }

  Future<Uri?> getUriFromFilePath({required String path}) {
    throw UnimplementedError('getUriFromFilePath() has not been implemented.');
  }

  Future<DocumentTree?> requestForAccess(
      {required String? initialRelativePath}) {
    throw UnimplementedError('requestForAccess() has not been implemented.');
  }

  Future<bool> editFile({required String uriString, required tempFilePath}) {
    throw UnimplementedError('editFile() has not been implemented.');
  }

  Future<bool> deleteFileUsingUri({required String uriString}) {
    throw UnimplementedError('deleteFileUsingUri() has not been implemented.');
  }

  Future<bool> isFileDeletable({required String uriString}) {
    throw UnimplementedError('isFileDeletable() has not been implemented.');
  }

  Future<bool> isFileWritable({required String uriString}) {
    throw UnimplementedError('isFileWritable() has not been implemented.');
  }

  Future<bool> readFileUsingUri(
      {required String uriString, required tempFilePath}) {
    throw UnimplementedError('readFileUsingUri() has not been implemented.');
  }

  Future<bool> readFile({
    required String tempFilePath,
    required String fileName,
    required DirType dirType,
    required DirName dirName,
    required String relativePath,
  }) {
    throw UnimplementedError('readFile() has not been implemented.');
  }

  Future<bool> isFileUriExist({required String uriString}) {
    throw UnimplementedError('isFileUriExist() has not been implemented.');
  }

  Future<DocumentTree?> getDocumentTree({required String uriString}) {
    throw UnimplementedError('getDocumentTree() has not been implemented.');
  }

  Future<String?> getFilePathFromUri({required String uriString}) {
    throw UnimplementedError('getFilePathFromUri() has not been implemented.');
  }
}
