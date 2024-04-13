## 0.0.1

Initial release

## 0.0.2

Add `Document` class to get info about a file (name, content uri, type, modification time, size,)
Add `getDocumentTree()` method to list folder files by folder's content uri

## 0.0.3

Introduce 'relativePath'. Check the documentation for more info </br>
Fix bug when file name contains space. </br>
Add supports for `application/octet-stream` mime type file </br>

## 0.0.4

Remove manual `MIME_TYPE` detection. Let the android decides this.

## 0.0.5

Fix bug in save files if directory not exist, under API level 29.

## 0.0.6

Fix bug in native side due to gson in release mode.

## 0.0.7

Replace `FlutterActivity` with `Activity` to support all subclasses of `Activity`, thus resolving the casting errors.

## 0.0.8

`saveFile` now returns the file `Uri` if the operation is successful

Fix bugs while using special characters in file names

## 0.0.9

Fix bugs below API Level `29` introduced by fixing special characters issue

## 0.1.0

Add `getFilePathFromUri()` to get the file path from a `Uri`

Update the `deleteFile()` implementation

## 0.1.1

Add `SaveInfo` class to get info about a file after saving.

`saveFile` now returns `SaveInfo`