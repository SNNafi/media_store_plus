import 'package:media_store_plus/src/exceptions.dart';

///
class FilePath {
  FilePath._();

  static String root = "";
}

/// Directory Type
enum DirType { photo, audio, video, download }

/// Directory names for directory type.
/// Read more from here: https://developer.android.com/training/data-storage/shared/media#media_store
enum DirName {
  dcim,
  pictures,
  movies,
  alarms,
  audiobooks,
  music,
  notifications,
  podcasts,
  ringtones,
  download
}

extension DirTypeVale on DirType {
  DirName get defaults {
    switch (this) {
      case DirType.photo:
        return DirName.pictures;
      case DirType.audio:
        return DirName.music;
      case DirType.video:
        return DirName.movies;
      case DirType.download:
        return DirName.download;
    }
  }

  /// It will provide the direct file path for [DirType] & [DirName] using [relativePath]
  /// Example: For [DirType.audio] && [DirName.podcasts], it will return "/storage/emulated/0/Podcasts/[relativePath]"
  /// For setting, [relativePath] = [FilePath.root], it will return "/storage/emulated/0/Podcasts"
  String fullPath({required String relativePath, required DirName dirName}) {
    checkDirTypeAndName(dirType: this, dirName: dirName);
    final segment = relativePath.trim();
    if (segment == FilePath.root) {
      return "/storage/emulated/0/${dirName.folder}";
    }
    return "/storage/emulated/0/${dirName.folder}/$segment";
  }
}

extension DirFolderName on DirName {
  String get folder {
    switch (this) {
      case DirName.dcim:
        return "DCIM";
      case DirName.pictures:
        return "Pictures";
      case DirName.movies:
        return "Movies";
      case DirName.alarms:
        return "Alarms";
      case DirName.audiobooks:
        return "Audiobooks";
      case DirName.music:
        return "Music";
      case DirName.notifications:
        return "Notifications";
      case DirName.podcasts:
        return "Podcasts";
      case DirName.ringtones:
        return "Ringtones";
      case DirName.download:
        return "Download";
    }
  }
}

/// It will check if the [DirName] is right for the [DirType]
/// Read more from here: https://developer.android.com/training/data-storage/shared/media#media_store
void checkDirTypeAndName({required DirType dirType, required DirName dirName}) {
  switch (dirType) {
    case DirType.photo:
      if (dirName != DirName.dcim && dirName != DirName.pictures) {
        throw MisMatchDirectoryTypeAndNameException("""
        
For DirType.photo,
DirName must be DirName.dcim or DirName.pictures.
Found: DirName.${dirName.name}""");
      }
      break;
    case DirType.audio:
      if (dirName != DirName.alarms &&
          dirName != DirName.audiobooks &&
          dirName != DirName.music &&
          dirName != DirName.notifications &&
          dirName != DirName.podcasts &&
          dirName != DirName.ringtones) {
        throw MisMatchDirectoryTypeAndNameException("""
        
For DirType.audio,
DirName must be DirName.alarms or DirName.audiobooks or DirName.music or DirName.notifications or DirName.podcasts or DirName.ringtones.
Found: DirName.${dirName.name}""");
      }
      break;
    case DirType.video:
      if (dirName != DirName.dcim &&
          dirName != DirName.pictures &&
          dirName != DirName.movies) {
        throw MisMatchDirectoryTypeAndNameException("""

For DirType.video,
DirName must be DirName.dcim or DirName.pictures or DirName.movies.
Found: DirName.${dirName.name}""");
      }
      break;
    case DirType.download:
      if (dirName != DirName.download) {
        throw MisMatchDirectoryTypeAndNameException("""
        
For DirType.download,
DirName must be DirName.download.
Found: DirName.${dirName.name}""");
      }
      break;
  }
}
