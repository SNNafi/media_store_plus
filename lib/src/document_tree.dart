import 'dart:convert';

import 'package:media_store_plus/media_store_plus.dart';

/// From API level 30, app needs to ask for read or write permission for specefic directory.
/// Use [MediaStore.requestForAccess] method to get read and write access by file picker.
/// It will return [DocumentTree] if permission is granted
class DocumentTree {
  String uriString;
  List<Document> children;

  DocumentTree({required this.uriString, required this.children});

  factory DocumentTree.fromJson(dynamic data) {
    final json = jsonDecode(data);
    var children = (json["children"] as List<dynamic>)
        .map((e) => Document.fromJson(e))
        .toList();
    return DocumentTree(uriString: json["uri_string"], children: children);
  }

  /// Requested directory's uri
  Uri get uri => Uri.parse(uriString);

  /// Requested directory's children's uri. Can be empty if no file exists.
  List<Uri> get childrenUriList =>
      children.map((e) => Uri.parse(e.uriString)).toList();
}

/// File info from a directory
class Document {
  String uriString;
  String? name;
  bool isVirtual;
  bool isDirectory;
  String? fileType;
  int lastModified;
  int fileLength;
  bool? isWritable;
  bool? isDeletable;

  Document(
    this.uriString,
    this.name,
    this.isVirtual,
    this.isDirectory,
    this.fileType,
    this.lastModified,
    this.fileLength,
    this.isWritable,
    this.isDeletable,
  );

  factory Document.fromJson(Map<String, dynamic> data) => Document(
        data["uri_string"],
        data["name"],
        data["is_virtual"],
        data["is_directory"],
        data["file_type"],
        data["last_modified"],
        data["file_length"],
        data["is_writable"],
        data["is_deletable"],
      );

  /// File uri
  Uri get uri => Uri.parse(uriString);

  @override
  String toString() {
    return "$name - $fileType - isDirectory:$isDirectory - isVirtual:$isVirtual - lastModified:$lastModified - fileLength:$fileLength - writable:$isWritable - deletable:$isDeletable";
  }
}
