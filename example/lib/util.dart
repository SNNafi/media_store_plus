import 'dart:io';

import 'package:flutter/services.dart';
import 'package:media_store_plus/media_store_plus.dart';
import 'package:path_provider/path_provider.dart';

import 'main.dart';

String getPath({
  String? relativePath,
  required String fileName,
  required DirType dirType,
  required DirName dirName,
}) {
  return dirType.fullPath(
          relativePath: relativePath.orAppFolder, dirName: dirName) +
      "/" +
      fileName;
}

File getFile({
  String? relativePath,
  required String fileName,
  required DirType dirType,
  required DirName dirName,
}) {
  return File(
    dirType.fullPath(relativePath: relativePath.orAppFolder, dirName: dirName) +
        "/" +
        fileName,
  );
}

extension ByteDataToFile on ByteData {
  Future<void> writeToFile(File file) async {
    final buffer = this.buffer;
    await file.writeAsBytes(
        buffer.asUint8List(this.offsetInBytes, this.lengthInBytes));
  }
}

// It will try to read or write first in the given folder.
// if it is not accessible, it will request for permission for that folder then try again to read or write
Future<bool> readOrWriteApiLevel33WithPermission(
    {required String initialRelativePath,
    required Function() operation}) async {
  try {
    await operation();
    return true;
  } on FileSystemException catch (e) {
    print(e);

    // To test this place a .txt file in that folder
    // request for read/write access for a folder in API level 33
    // Use this also get write access for a folder from API level 30
    final documentTree = await mediaStorePlugin.requestForAccess(
        initialRelativePath: initialRelativePath);
    if (documentTree != null && documentTree.childrenUriList.isNotEmpty) {
      final uriString = documentTree.children
          .where((element) => element.name!.contains(".txt"))
          .first
          .uriString;
      print("Folder Uri ${documentTree.uri.toString()}");
      print("File Uri ${uriString}");
      documentTree.children.forEach((doc) {
        print(doc);
      });

      // check if file exists by uri
      await mediaStorePlugin
          .isFileUriExist(uriString: uriString)
          .then((value) => value == true ? print("Exist") : print(""));

      // check if file is writable by uri
      await mediaStorePlugin
          .isFileWritable(uriString: uriString)
          .then((value) => value == true ? print("Writable") : print(""));

      // check if file is deletable by uri
      await mediaStorePlugin
          .isFileDeletable(uriString: uriString)
          .then((value) => value == true ? print("Deletable") : print(""));

      // read file by uri
      File tempFile = File(
          (await getApplicationSupportDirectory()).path + "/" + "text.txt");
      bool status = await mediaStorePlugin.readFileUsingUri(
          uriString: uriString, tempFilePath: tempFile.path);
      if (status) {
        print((await tempFile.readAsString()));
      }

      // edit file by uri
      await tempFile
          .writeAsString("EDITED: You are reading from API level 33.");
      await mediaStorePlugin.editFile(
          uriString: uriString, tempFilePath: tempFile.path);

      status = await mediaStorePlugin.readFileUsingUri(
          uriString: uriString, tempFilePath: tempFile.path);
      if (status) {
        print((await tempFile.readAsString()));
      }

      // delete file by uri
      // await mediaStorePlugin.deleteFileUsingUri(uriString: uriString);

      print("read folder info by folder uri");
      DocumentTree? docTree = await mediaStorePlugin.getDocumentTree(
          uriString: documentTree.uriString);
      if (docTree != null && docTree.childrenUriList.isNotEmpty) {
        final uriString = docTree.children
            .where((element) => element.name!.contains(".txt"))
            .first
            .uriString;
        print("2 Folder Uri ${docTree.uri.toString()}");
        print("2 File Uri ${uriString}");
        docTree.children.forEach((doc) {
          print("2 ${doc}");
        });

        // check if file exists by uri
        await mediaStorePlugin
            .isFileUriExist(uriString: uriString)
            .then((value) => value == true ? print("Exist") : print(""));

        // check if file is writable by uri
        await mediaStorePlugin
            .isFileWritable(uriString: uriString)
            .then((value) => value == true ? print("Writable") : print(""));

        // check if file is deletable by uri
        await mediaStorePlugin
            .isFileDeletable(uriString: uriString)
            .then((value) => value == true ? print("Deletable") : print(""));

        // read file by uri
        File tempFile = File(
            (await getApplicationSupportDirectory()).path + "/" + "text.txt");
        bool status = await mediaStorePlugin.readFileUsingUri(
            uriString: uriString, tempFilePath: tempFile.path);
        if (status) {
          print((await tempFile.readAsString()));
        }

        // edit file by uri
        await tempFile
            .writeAsString("EDITED: You are reading from API level 33.");
        await mediaStorePlugin.editFile(
            uriString: uriString, tempFilePath: tempFile.path);

        status = await mediaStorePlugin.readFileUsingUri(
            uriString: uriString, tempFilePath: tempFile.path);
        if (status) {
          print((await tempFile.readAsString()));
        }

        // delete file by uri
        // await mediaStorePlugin.deleteFileUsingUri(uriString: uriString);
      }
    }

    return true;
  } catch (e) {
    print(e);
    return false;
  }
  return false;
}
