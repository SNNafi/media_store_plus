enum StorageVolumeType {
  /// Internal storage. It is called by Android __external_primary__, located at `/storage/emulated/0/`
  primary,

  /// SD card or other external storage
  external,
}

///
class StorageVolume {
  /// The name define by [MediaStore], doesn't applicable under API level 30
  final String name;

  /// The actual path like `/storage/6193-E5BF/`.
  final String path;

  const StorageVolume({required this.name, required this.path});

  static const StorageVolume primary =
      StorageVolume(name: 'external_primary', path: '/storage/emulated/0/');

  /// Storage volume type
  StorageVolumeType get type => path == '/storage/emulated/0/'
      ? StorageVolumeType.primary
      : StorageVolumeType.external;

  @override
  String toString() {
    return '$type: $name -> $path';
  }

  @override
  bool operator ==(covariant StorageVolume other) {
    return name == other.name;
  }

  @override
  int get hashCode => Object.hashAll([name]);
}
