import 'package:media_store_plus/media_store_plus.dart';

/// From API level 30, app needs to ask for read or write permission for specefic directory.
/// Use [MediaStore.requestForAccess] method to get read and write access by file picker.
/// It will return [DocumentTree] if permission is granted
class DocumentTree {
  /// Requested directory's uri
  Uri uri;

  /// Requested directory's children's uri. Can be empty if no file exists.
  List<Uri> childrenUriList;

  DocumentTree({required this.uri, required this.childrenUriList});
}
