import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:media_store_plus/media_store_plus.dart';

import 'media_store_platform_interface.dart';

/// An implementation of [MediaStorePlatform] that uses method channels.
class MethodChannelMediaStore extends MediaStorePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('media_store_plus');

  @override
  Future<int> getPlatformSDKInt() async {
    final version = await methodChannel.invokeMethod<int>('getPlatformSDKInt');
    return version!;
  }

  @override
  Future<bool> saveFile({
    required String tempFilePath,
    required String fileName,
    required DirType dirType,
    required DirName dirName,
    required String relativePath,
  }) async {
    final status = await methodChannel.invokeMethod<bool>('saveFile', {
      "tempFilePath": tempFilePath,
      "fileName": fileName,
      "dirType": dirType.index,
      "dirName": dirName.folder,
      "appFolder": relativePath,
    });
    return status ?? false;
  }

  @override
  Future<bool> deleteFile({
    required String fileName,
    required DirType dirType,
    required DirName dirName,
    required String relativePath,
  }) async {
    final status = await methodChannel.invokeMethod<bool>('deleteFile', {
      "fileName": fileName,
      "dirType": dirType.index,
      "dirName": dirName.folder,
      "appFolder": relativePath,
    });
    return status ?? false;
  }

  @override
  Future<Uri?> getFileUri({
    required String fileName,
    required DirType dirType,
    required DirName dirName,
    required String relativePath,
  }) async {
    final uriString = await methodChannel.invokeMethod<String?>('getFileUri', {
      "fileName": fileName,
      "dirType": dirType.index,
      "dirName": dirName.folder,
      "appFolder": relativePath,
    });
    if (uriString != null) {
      return Uri.parse(uriString);
    }
    return null;
  }

  @override
  Future<Uri?> getUriFromFilePath({required String path}) async {
    final uriString =
        await methodChannel.invokeMethod<String?>('getUriFromFilePath', {
      "filePath": path,
    });
    if (uriString != null) {
      return Uri.parse(uriString);
    }
    return null;
  }

  @override
  Future<DocumentTree?> requestForAccess(
      {required String? initialRelativePath}) async {
    final string =
        await methodChannel.invokeMethod<String>('requestForAccess', {
      "initialRelativePath": initialRelativePath,
    });
    var jsonString = (string ?? "");
    if (jsonString.isNotEmpty) {
      final json = jsonDecode(jsonString);
      return DocumentTree.fromJson(json);
    } else {
      return null;
    }
  }

  @override
  Future<bool> editFile(
      {required String uriString, required tempFilePath}) async {
    final status = await methodChannel.invokeMethod<bool>('editFile', {
      "contentUri": uriString,
      "tempFilePath": tempFilePath,
    });
    return status ?? false;
  }

  @override
  Future<bool> deleteFileUsingUri({required String uriString}) async {
    final status = await methodChannel
        .invokeMethod<bool>('deleteFileUsingUri', {"contentUri": uriString});
    return status ?? false;
  }

  @override
  Future<bool> isFileDeletable({required String uriString}) async {
    final status = await methodChannel
        .invokeMethod<bool>('isFileDeletable', {"contentUri": uriString});
    return status ?? false;
  }

  @override
  Future<bool> isFileWritable({required String uriString}) async {
    final status = await methodChannel
        .invokeMethod<bool>('isFileWritable', {"contentUri": uriString});
    return status ?? false;
  }

  @override
  Future<bool> readFileUsingUri(
      {required String uriString, required tempFilePath}) async {
    final status = await methodChannel.invokeMethod<bool>('readFileUsingUri', {
      "contentUri": uriString,
      "tempFilePath": tempFilePath,
    });
    return status ?? false;
  }

  @override
  Future<bool> readFile({
    required String tempFilePath,
    required String fileName,
    required DirType dirType,
    required DirName dirName,
    required String relativePath,
  }) async {
    final status = await methodChannel.invokeMethod<bool>('readFile', {
      "tempFilePath": tempFilePath,
      "fileName": fileName,
      "dirType": dirType.index,
      "dirName": dirName.folder,
      "appFolder": relativePath,
    });
    return status ?? false;
  }

  @override
  Future<bool> isFileUriExist({required String uriString}) async {
    final status = await methodChannel.invokeMethod<bool>('isFileUriExist', {
      "contentUri": uriString,
    });
    return status ?? false;
  }

  @override
  Future<DocumentTree?> getDocumentTree({required String uriString}) async {
    final string = await methodChannel.invokeMethod<String>('getDocumentTree', {
      "contentUri": uriString,
    });
    var jsonString = (string ?? "");
    if (jsonString.isNotEmpty) {
      final json = jsonDecode(jsonString);
      return DocumentTree.fromJson(json);
    } else {
      return null;
    }
  }
}
