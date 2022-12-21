abstract class MediaStorePluginException implements Exception {
  const MediaStorePluginException([this.message]);

  final String? message;

  @override
  String toString() {
    String result = runtimeType.toString();
    if (message is String) return '$result: $message';
    return result;
  }
}

/// [AppFolderNotSetException] will be thrown if [MediaStore.appFolder] is not set before calling any method.
class AppFolderNotSetException extends MediaStorePluginException {
  const AppFolderNotSetException([String? message]) : super(message);
}

/// [MisMatchDirectoryTypeAndNameException] will be thrown if [DirName] not matches with [DirType].
class MisMatchDirectoryTypeAndNameException extends MediaStorePluginException {
  const MisMatchDirectoryTypeAndNameException([String? message])
      : super(message);
}
