import 'dart:io';

import 'package:media_store_plus/src/dir_type.dart';
import 'package:media_store_plus/src/document_tree.dart';
import 'package:media_store_plus/src/exceptions.dart';
import 'package:media_store_plus/src/extensions.dart';

import 'media_store_platform_interface.dart';

export 'package:media_store_plus/src/dir_type.dart';
export 'package:media_store_plus/src/document_tree.dart';
export 'package:media_store_plus/src/extensions.dart';

/// To use Android `MediaStore` API in Flutter. It supports both read & write operation in every android version.
/// It also requests for appropriate permission, if needed.
class MediaStore {
  /// Set app directory like Music/[MediaStore.appFolder], Download/[MediaStore.appFolder], DCIM/[MediaStore.appFolder]
  static String appFolder = "";
  int _sdkInt = 0;

  MediaStore() {
    _getSDKInt();
  }

  void _getSDKInt() async {
    _sdkInt = await MediaStorePlatform.instance.getPlatformSDKInt();
  }

  /// Get running platform sdk int
  Future<int> getPlatformSDKInt() {
    if (_sdkInt != 0) {
      return Future.value(_sdkInt);
    }
    return MediaStorePlatform.instance.getPlatformSDKInt();
  }

  /// It will create new file or update existing file. Return `true` upon saving or updating.
  /// __It will request for user permission if app hasn't permission to save or edit file in that location.__
  /// To use this method, first save your file in a temporary location like app data folder then provide this path.
  /// This method then copy file contents from this path and save it in the particularly location using [MediaStore].
  /// Then it will delete the temporary file.
  /// __It will use [MediaStore] from API level 30 & use direct [File] below 30__
  ///
  /// Return [Uri] as [String] of the saved file if successful
  ///
  /// To save in /storage/emulated/0/Podcasts/[relativePath],
  /// [dirType] = [DirType.audio] &
  /// [dirName] = [DirName.podcasts]
  ///
  /// If you want to save to the root of a directory, like, /storage/emulated/0/Podcasts,
  /// Set [relativePath] = [FilePath.root]
  /// Or in any other directory, set [relativePath] as you like,
  /// If [relativePath] is `null` it will use [MediaStore.appFolder]. Whether you set [relativePath] or not, if [MediaStore.appFolder] is `null` it will always throw [AppFolderNotSetException]
  ///
  /// throws [AppFolderNotSetException] if [MediaStore.appFolder] is not set.
  /// throws [MisMatchDirectoryTypeAndNameException] if [DirName] not matches with [DirType].
  Future<String?> saveFile({
    required String tempFilePath,
    required DirType dirType,
    required DirName dirName,
    String? relativePath,
  }) async {
    if (appFolder.isEmpty) {
      throw const AppFolderNotSetException(
          "Set the folder location first using MediaStore.appFolder");
    }

    checkDirTypeAndName(dirType: dirType, dirName: dirName);

    if (_sdkInt >= 29) {
      String fileName = Uri.parse(tempFilePath).pathSegments.last.trim();
      return await MediaStorePlatform.instance.saveFile(
        tempFilePath: tempFilePath.sanitize,
        fileName: fileName,
        dirType: dirType,
        dirName: dirName,
        relativePath: relativePath.orAppFolder,
      );
    } else {
      Directory directory = Directory(dirType.fullPath(
          relativePath: relativePath.orAppFolder, dirName: dirName));

      await Directory(directory.path).create(recursive: true);

      String fileName = Uri.parse(tempFilePath).pathSegments.last.trim();
      File tempFile = File(tempFilePath.sanitize);
      File file = await tempFile.copy("${directory.path}/$fileName");
      return file.uri.toString();
    }
  }

  /// It will delete existing file if exist. Return `true` if deleted or return false
  /// __It will request for user permission if app hasn't permission to delete that file__.
  /// To use this method, first save your file in a temporary location like app data folder then provide this path.
  /// This method then copy file contents from this path and save it in the particularly location using [MediaStore].
  /// Then it will delete the temporary file.
  /// __It will use [MediaStore] from API level 30 & use direct [File] below 30__
  ///
  /// throws [AppFolderNotSetException] if [MediaStore.appFolder] is not set.
  /// throws [MisMatchDirectoryTypeAndNameException] if [DirName] not matches with [DirType].
  Future<bool> deleteFile({
    required String fileName,
    required DirType dirType,
    required DirName dirName,
    String? relativePath,
  }) async {
    if (appFolder.isEmpty) {
      throw const AppFolderNotSetException(
          "Set the folder location first using MediaStore.appFolder");
    }

    checkDirTypeAndName(dirType: dirType, dirName: dirName);

    if (_sdkInt >= 29) {
      return await MediaStorePlatform.instance.deleteFile(
        fileName: fileName,
        dirType: dirType,
        dirName: dirName,
        relativePath: relativePath.orAppFolder,
      );
    } else {
      Directory directory = Directory(dirType.fullPath(
          relativePath: relativePath.orAppFolder, dirName: dirName));
      File file = File("${directory.path}/$fileName");
      if ((await file.exists())) {
        await file.delete();
      }
      return !(await file.exists());
    }
  }

  /// It will return file [Uri] if file exist, otherwise `null`
  ///
  /// To get [Uri] for /storage/emulated/0/Podcasts/[relativePath]/DailyQuran.mp3,
  /// [fileName] = DailyQuran.mp3
  /// [dirType] = [DirType.audio]
  /// [dirName] = [DirName.podcasts]
  ///
  /// If you want to get it from the root of a directory, like, /storage/emulated/0/Podcasts,
  /// Set [relativePath] = [FilePath.root]
  /// Or in any other directory, set [relativePath] as you like,
  /// If [relativePath] is `null` it will use [MediaStore.appFolder]. Whether you set [relativePath] or not, if [MediaStore.appFolder] is `null` it will always throw [AppFolderNotSetException]
  ///
  ///
  /// throws [AppFolderNotSetException] if [MediaStore.appFolder] is not set.
  /// throws [MisMatchDirectoryTypeAndNameException] if [DirName] not matches with [DirType].
  Future<Uri?> getFileUri({
    required String fileName,
    required DirType dirType,
    required DirName dirName,
    String? relativePath,
  }) async {
    if (appFolder.isEmpty) {
      throw const AppFolderNotSetException(
          "Set the folder location first using MediaStore.appFolder");
    }

    checkDirTypeAndName(dirType: dirType, dirName: dirName);

    if (_sdkInt >= 29) {
      return await MediaStorePlatform.instance.getFileUri(
        fileName: fileName,
        dirType: dirType,
        dirName: dirName,
        relativePath: relativePath.orAppFolder,
      );
    } else {
      Directory directory = Directory(dirType.fullPath(
          relativePath: relativePath.orAppFolder, dirName: dirName));
      File file = File("${directory.path}/$fileName");
      return await MediaStorePlatform.instance
          .getUriFromFilePath(path: file.path);
    }
  }

  /// It will return `true` if file exist, otherwise `null`
  ///
  /// To check for /storage/emulated/0/Download/[MediaStore.appFolder]/DailyQuran.mp3,
  /// [fileName] = DailyQuran.mp3
  /// [dirType] = [DirType.download]
  /// [dirName] = [DirName.download]
  ///
  /// If you want to check it in the root of a directory, like, /storage/emulated/0/Podcasts,
  /// Set [relativePath] = [FilePath.root]
  /// Or in any other directory, set [relativePath] as you like,
  /// If [relativePath] is `null` it will use [MediaStore.appFolder]. Whether you set [relativePath] or not, if [MediaStore.appFolder] is `null` it will always throw [AppFolderNotSetException]
  ///
  ///
  /// throws [AppFolderNotSetException] if [MediaStore.appFolder] is not set.
  /// throws [MisMatchDirectoryTypeAndNameException] if [DirName] not matches with [DirType].
  Future<bool> isFileExist({
    required String fileName,
    required DirType dirType,
    required DirName dirName,
    String? relativePath,
  }) async {
    if (appFolder.isEmpty) {
      throw const AppFolderNotSetException(
          "Set the folder location first using MediaStore.appFolder");
    }

    checkDirTypeAndName(dirType: dirType, dirName: dirName);

    if (_sdkInt >= 29) {
      final uri = await MediaStorePlatform.instance.getFileUri(
        fileName: fileName,
        dirType: dirType,
        dirName: dirName,
        relativePath: relativePath.orAppFolder,
      );
      return uri != null;
    } else {
      Directory directory = Directory(dirType.fullPath(
          relativePath: relativePath.orAppFolder, dirName: dirName));
      File file = File("${directory.path}/$fileName");
      final uri =
          await MediaStorePlatform.instance.getUriFromFilePath(path: file.path);
      return uri != null;
    }
  }

  /// This method is not meant for using outside the plugin.
  Future<Uri?> getUriFromFilePath({required String path}) {
    return MediaStorePlatform.instance.getUriFromFilePath(path: path);
  }

  /// From API level 30, app needs to ask for read or write permission for specific directory.
  /// Use this method to get read and write access by file picker.
  /// It will return [DocumentTree] if permission is granted
  ///
  /// To open file picker in a exact location, use [initialRelativePath]. It is `optional`
  /// For open in Music/Quran, [initialRelativePath] = "Music/Quran"
  ///
  /// Read more about it from here: https://developer.android.com/training/data-storage/shared/documents-files#grant-access-directory
  Future<DocumentTree?> requestForAccess(
      {required String? initialRelativePath}) {
    return MediaStorePlatform.instance
        .requestForAccess(initialRelativePath: initialRelativePath);
  }

  /// It will edit the file using [Uri] from the given [uriString] if exist. Return `true` upon editing.
  /// __It will request for user permission if app hasn't permission to edit the file.__
  /// To use this method, first save the updated file in a temporary location, like app data folder then provide this path.
  /// This method then copy file contents from this path and edit it in the particularly location using [MediaStore].
  /// Then it will delete the temporary file.
  Future<bool> editFile({required String uriString, required String tempFilePath}) {
    return MediaStorePlatform.instance
        .editFile(uriString: uriString, tempFilePath: tempFilePath.sanitize);
  }

  /// It will delete existing file using [Uri] from the given [uriString] if exist. Return `true` if deleted or return false
  /// __It will request for user permission if app hasn't permission to delete that file__.
  Future<bool> deleteFileUsingUri({required String uriString}) {
    return MediaStorePlatform.instance.deleteFileUsingUri(uriString: uriString);
  }

  /// Return `true` if the file from the given [uriString] is deletable
  Future<bool> isFileDeletable({required String uriString}) {
    return MediaStorePlatform.instance.isFileDeletable(uriString: uriString);
  }

  /// Return `true` if the file from the given [uriString] is writable
  Future<bool> isFileWritable({required String uriString}) {
    return MediaStorePlatform.instance.isFileWritable(uriString: uriString);
  }

  /// It will read the file using [Uri] from the given [uriString] if exist. Return `true` upon reading.
  /// __It will request for user permission if app hasn't permission to read the file.__
  /// To use this method, first create a new file in a temporary location, like app data folder then provide this path.
  /// This method then copy file contents to this temporary path to read directy by [File].
  Future<bool> readFileUsingUri(
      {required String uriString, required String tempFilePath}) {
    return MediaStorePlatform.instance
        .readFileUsingUri(uriString: uriString, tempFilePath: tempFilePath.sanitize);
  }

  /// It will read the file if exists. Return `true` upon reading.
  /// __It will request for user permission if app hasn't permission to read the file.__
  /// To use this method, first create a new file in a temporary location, like app data folder then provide this path.
  /// This method then copy file contents to this temporary path to read directy by [File].
  /// __It will use [MediaStore] from API level 30 & use direct [File] below 30__
  ///
  /// To read /storage/emulated/0/Podcasts/[MediaStore.appFolder]/Podcasts/DailyQuran.mp3,
  /// [fileName] = DailyQuran.mp3
  /// [dirType] = [DirType.audio] &
  /// [dirName] = [DirName.podcasts]
  ///
  /// If you want to read it from the root of a directory, like, /storage/emulated/0/Podcasts,
  /// Set [relativePath] = [FilePath.root]
  /// Or in any other directory, set [relativePath] as you like,
  /// If [relativePath] is `null` it will use [MediaStore.appFolder]. Whether you set [relativePath] or not, if [MediaStore.appFolder] is `null` it will always throw [AppFolderNotSetException]
  ///
  ///
  /// throws [AppFolderNotSetException] if [MediaStore.appFolder] is not set.
  /// throws [MisMatchDirectoryTypeAndNameException] if [DirName] not matches with [DirType].
  Future<bool> readFile({
    required String fileName,
    required String tempFilePath,
    required DirType dirType,
    required DirName dirName,
    String? relativePath,
  }) async {
    if (appFolder.isEmpty) {
      throw const AppFolderNotSetException(
          "Set the folder location first using MediaStore.appFolder");
    }

    checkDirTypeAndName(dirType: dirType, dirName: dirName);

    if (_sdkInt >= 29) {
      return await MediaStorePlatform.instance.readFile(
        tempFilePath: tempFilePath.sanitize,
        fileName: fileName,
        dirType: dirType,
        dirName: dirName,
        relativePath: relativePath.orAppFolder,
      );
    } else {
      Directory directory = Directory(dirType.fullPath(
          relativePath: relativePath.orAppFolder, dirName: dirName));
      File file = File("${directory.path}/$fileName");
      File tempFile = await file.copy(tempFilePath.sanitize);
      return await tempFile.exists();
    }
  }

  /// Return `true` if the file from the given [uriString] exists
  Future<bool> isFileUriExist({required String uriString}) {
    return MediaStorePlatform.instance.isFileUriExist(uriString: uriString);
  }

  /// Return [DocumentTree] if the given folder uri exist and have permission to read files from that location
  /// To grant read or write in a particular folder use [requestForAccess]
  Future<DocumentTree?> getDocumentTree({required String uriString}) {
    return MediaStorePlatform.instance.getDocumentTree(uriString: uriString);
  }
}
