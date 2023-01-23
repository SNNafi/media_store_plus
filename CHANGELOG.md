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

Remove munual `MIME_TYPE` detection. Let the android decides this.

## 0.0.5

Fix bug in save files if directory not exist, under API level 29.