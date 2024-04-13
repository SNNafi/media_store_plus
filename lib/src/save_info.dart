/// The save status of the file
enum SaveStatus {
  /// We are not tracking save status explicitly below API level 30.
  createdOrReplaced,

  /// File is created
  created,

  /// File is replaced by previous file
  replaced,

  /// File is duplicated. If there's no write permission to replace the file,
  /// then another file will be created by adding suffix at the last of the file name.
  /// Like al_aqsa_mosque.jpeg to al_aqsa_mosque (1).jpeg
  duplicated,
}

SaveStatus _getSaveStatus(int v) {
  switch (v) {
    case 0:
      return SaveStatus.created;
    case 1:
      return SaveStatus.replaced;
    case 2:
      return SaveStatus.duplicated;
    default:
      return SaveStatus.createdOrReplaced;
  }
}

/// Save info while calling [saveFile] method. It contains saved file name, uri and the saved status, whether the file is created, replaced or duplicated
class SaveInfo {
  /// Name of the file after saving. It can be a duplicated file name
  final String name;

  /// [Uri] of the file
  final Uri uri;

  /// Save status
  final SaveStatus saveStatus;

  /// If `false`, that's mean file can't be created or replaced with the actual name.
  /// A duplicated file is created
  /// Check [name] for the file name
  bool get isSuccessful => saveStatus != SaveStatus.duplicated;

  /// 'true' if it is duplicated file
  bool get isDuplicated => saveStatus == SaveStatus.duplicated;

  const SaveInfo({required this.name, required this.uri, required this.saveStatus});

  factory SaveInfo.fromJson(Map<String, dynamic> json) {
    return SaveInfo(
        name: json['name'],
        uri: Uri.parse(json['uri']),
        saveStatus: _getSaveStatus(int.parse(json['save_status'])));
  }

  @override
  String toString() {
    return '$name $uri $saveStatus';
  }
}
