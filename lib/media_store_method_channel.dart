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
  Future<SaveInfo?> saveFile({
    required String tempFilePath,
    required String fileName,
    required DirType dirType,
    required DirName dirName,
    required StorageVolume volume,
    required String relativePath,
  }) async {
    final string = await methodChannel.invokeMethod<String?>('saveFile', {
      "tempFilePath": tempFilePath,
      "fileName": fileName,
      "dirType": dirType.index,
      "dirName": dirName.folder,
      "appFolder": relativePath,
      "volume": volume.name,
    });

    final jsonString = (string ?? '');
    final json = jsonDecode(jsonString);
    try {
      return SaveInfo.fromJson(json);
    } catch (e) {
      return null;
    }
  }

  @override
  Future<bool> deleteFile({
    required String fileName,
    required DirType dirType,
    required DirName dirName,
    required StorageVolume volume,
    required String relativePath,
  }) async {
    final status = await methodChannel.invokeMethod<bool>('deleteFile', {
      "fileName": fileName,
      "dirType": dirType.index,
      "dirName": dirName.folder,
      "appFolder": relativePath,
      "volume": volume.name,
    });
    return status ?? false;
  }

  @override
  Future<Uri?> getFileUri({
    required String fileName,
    required DirType dirType,
    required DirName dirName,
    required StorageVolume volume,
    required String relativePath,
  }) async {
    final uriString = await methodChannel.invokeMethod<String?>('getFileUri', {
      "fileName": fileName,
      "dirType": dirType.index,
      "dirName": dirName.folder,
      "appFolder": relativePath,
      "volume": volume.name,
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
      return DocumentTree.fromJson(jsonString);
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
    required StorageVolume volume,
    required String relativePath,
  }) async {
    final status = await methodChannel.invokeMethod<bool>('readFile', {
      "tempFilePath": tempFilePath,
      "fileName": fileName,
      "dirType": dirType.index,
      "dirName": dirName.folder,
      "appFolder": relativePath,
      "volume": volume.name,
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
      return DocumentTree.fromJson(jsonString);
    } else {
      return null;
    }
  }

  @override
  Future<String?> getFilePathFromUri({required String uriString}) async {
    final filePath =
        await methodChannel.invokeMethod<String?>('getFilePathFromUri', {
      "uriString": uriString,
    });
    if (filePath != null) {
      return filePath;
    }
    return null;
  }

  @override
  Future<List<List<String>>> getAvailableStorageDirectories() async {
    final names = await methodChannel
        .invokeListMethod<String>('getAvailableStorageDirectoriesNames');
    final paths = await methodChannel
        .invokeListMethod<String>('getAvailableStorageDirectoryPaths');
    return [names ?? [], paths ?? []];
  }

  @override
  Future<List<String>> getAvailableStorageDirectoryPaths() async {
    final list = await methodChannel
        .invokeListMethod<String>('getAvailableStorageDirectoryPaths');
    return list ?? <String>[];
  }
}
