package com.snnafi.media_store_plus

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


fun String.capitalized(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale.getDefault())
        else it.toString()
    }
}

/** MediaStorePlusPlugin */
class MediaStorePlusPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    PluginRegistry.ActivityResultListener {
    companion object {
        private var activity: Activity? = null
        private lateinit var channel: MethodChannel
        private lateinit var result: Result

        private lateinit var uriString: String
        private lateinit var fileName: String
        private lateinit var tempFilePath: String
        private var dirType: Int = 0
        private lateinit var dirName: String
        private lateinit var appFolder: String
        private var externalVolumeName: String? = null
        private var id3v2Tags: Map<String, String>? = null
        const val TAG = "MediaStorage"
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "media_store_plus")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        Log.d(TAG, "call.method: ${call.method}")
        if (call.method == "getPlatformSDKInt") {
            result.success(Build.VERSION.SDK_INT)
        } else if (call.method == "saveFile") {
            saveFile(
                Uri.parse(call.argument("tempFilePath")!!).path!!,
                call.argument("fileName")!!,
                call.argument("appFolder")!!,
                call.argument("dirType")!!,
                call.argument("dirName")!!,
                call.argument("externalVolumeName")!!,
                call.argument("id3v2Tags"),
            )
        } else if (call.method == "deleteFile") {
            deleteFile(
                call.argument("fileName")!!,
                call.argument("appFolder")!!,
                call.argument("dirType")!!,
                call.argument("dirName")!!
            )
        } else if (call.method == "getFileUri") {
            val uri: Uri? = getUriFromDisplayName(
                call.argument("fileName")!!,
                call.argument("appFolder")!!,
                call.argument("dirType")!!,
                call.argument("dirName")!!
            )
            if (uri != null) {
                result.success(uri.toString().trim())
            } else {
                result.success(null)
            }
        } else if (call.method == "getUriFromFilePath") {
            uriFromFilePath(Uri.parse(call.argument("filePath")!!).path!!)
        } else if (call.method == "requestForAccess") {
            requestForAccess(Uri.parse(call.argument("initialRelativePath")!!).path!!)
        } else if (call.method == "editFile") {
            editFile(
                call.argument("contentUri")!!,
                Uri.parse(call.argument("tempFilePath")!!).path!!,
            )
        } else if (call.method == "deleteFileUsingUri") {
            deleteFileUsingUri(
                call.argument("contentUri")!!,
            )
        } else if (call.method == "isFileDeletable") {
            result.success(
                isDeletable(
                    call.argument("contentUri")!!,
                )
            )
        } else if (call.method == "isFileWritable") {
            result.success(
                isWritable(
                    call.argument("contentUri")!!,
                )
            )
        } else if (call.method == "readFile") {
            readFile(
                Uri.parse(call.argument("tempFilePath")!!).path!!,
                call.argument("fileName")!!,
                call.argument("appFolder")!!,
                call.argument("dirType")!!,
                call.argument("dirName")!!
            )
        } else if (call.method == "readFileUsingUri") {
            readFileUsingUri(
                call.argument("contentUri")!!,
                Uri.parse(call.argument("tempFilePath")!!).path!!,
            )
        } else if (call.method == "isFileUriExist") {
            result.success(
                isFileUriExist(
                    call.argument("contentUri")!!,
                )
            )
        } else if (call.method == "getDocumentTree") {
            getFolderChildren(
                call.argument("contentUri")!!,
            )
        } else {
            result.notImplemented()
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity as FlutterActivity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    private fun saveFile(
        path: String,
        name: String,
        appFolder: String,
        dirType: Int,
        dirName: String,
        externalVolumeName: String,
        id3v2Tags: Map<String, String>?,
    ) {
        try {
            createOrUpdateFile(path, name, appFolder, dirType, dirName, externalVolumeName,id3v2Tags)
            File(tempFilePath).delete()
            result.success(true)

        } catch (e: Exception) {
            if (e is RecoverableSecurityException) {
                val recoverableSecurityException = e as? RecoverableSecurityException
                recoverableSecurityException?.let {
                    val intentSender =
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    intentSender.let {
                        activity!!.startIntentSenderForResult(
                            intentSender, 990, null, 0, 0, 0, null
                        )
                    }
                }
            }
            Log.e("Exception", e.message, e)
        }
    }

    private fun deleteFile(
        name: String,
        appFolder: String,
        dirType: Int,
        dirName: String
    ) {
        try {
            fileName = name
            tempFilePath = ""
            val status: Boolean = deleteFileUsingDisplayName(
                name,
                appFolder,
                dirType,
                dirName
            )
            result.success(status)
        } catch (e: Exception) {
            if (e is RecoverableSecurityException) {
                val recoverableSecurityException = e as? RecoverableSecurityException
                recoverableSecurityException?.let {
                    val intentSender =
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    intentSender.let {
                        activity!!.startIntentSenderForResult(
                            intentSender, 991, null, 0, 0, 0, null
                        )
                    }
                }
            }
            Log.e("Exception", e.message, e)
        }
    }

    private fun defineVolume(externalVolumeName:String?): String {
        return if (externalVolumeName != null) {
            MediaStore.getExternalVolumeNames(activity!!.applicationContext)
                .find { it.lowercase() == externalVolumeName.lowercase() }
                ?: MediaStore.VOLUME_EXTERNAL_PRIMARY
        } else {
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        }
    }

    private fun fileExists(uri: Uri?): Boolean {
        val isExist = try {
            val i = uri?.let { activity!!.applicationContext.contentResolver.openInputStream(it) }
            i != null
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
        return isExist
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveId3(file: String, id3v2Tags: Map<String, String>?) {

        if (id3v2Tags != null) {
            try {
                val mp3File = Mp3File(file)

                val id3v24Tag = ID3v24Tag()
                id3v24Tag.title = id3v2Tags["title"]
                id3v24Tag.comment = id3v2Tags["comment"]
                id3v24Tag.album = id3v2Tags["album"]
                id3v24Tag.artist = id3v2Tags["artist"]
                id3v24Tag.url = java.lang.String.format(
                    "https://www.suamusica.com.br/perfil/%s?playlistId=%s&albumId=%s&musicId=%s",
                    id3v2Tags["artistId"],
                    id3v2Tags["playlistId"],
                    id3v2Tags["albumId"],
                    id3v2Tags["musicId"]
                )
                mp3File.id3v2Tag = id3v24Tag
                val newFilename = "$file.tmp"
                mp3File.save(newFilename)

                val from = File(newFilename)
                from.renameTo(File(file))

                Log.i(TAG, "Successfully set ID3v2 tags")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set ID3v2 tags", e)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getUriFromDirType(dirType: Int,externalVolumeName: String?): Uri {
        return when (dirType) {
            0 -> MediaStore.Images.Media.getContentUri(defineVolume(externalVolumeName))
            1 -> MediaStore.Audio.Media.getContentUri(defineVolume(externalVolumeName))
            2 -> MediaStore.Video.Media.getContentUri(defineVolume(externalVolumeName))
            else -> MediaStore.Downloads.getContentUri(defineVolume(externalVolumeName))
        }

    }

    private fun createOrUpdateFile(
        path: String,
        name: String,
        appFolder: String,
        dirType: Int,
        dirName: String,
        externalVolumeName : String?,
        id3v2Tags: Map<String, String>?
    ) {
        saveId3(path, id3v2Tags)
        // { photo, music, video, download }
        Log.d(TAG, "DirName $dirName")

         val relativePath: String = if (appFolder.trim().isEmpty()) {
            dirName
        } else {
            dirName + File.separator + appFolder
        }

        deleteFileUsingDisplayName(name, appFolder, dirType, dirName,externalVolumeName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, name)
                put(MediaStore.Audio.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }


            val resolver = activity!!.applicationContext.contentResolver
            val uri = resolver.insert(getUriFromDirType(dirType,externalVolumeName), values)!!

            resolver.openOutputStream(uri).use { os ->
                File(path).inputStream().use { it.copyTo(os!!) }
            }

            values.clear()
            values.put(MediaStore.Audio.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)

            Log.d(TAG, "saveFile $name")

        }
    }


    @kotlin.jvm.Throws
    private fun deleteFileUsingDisplayName(
        displayName: String,
        appFolder: String,
        dirType: Int,
        dirName: String,
        externalVolumeName: String?,
    ): Boolean {
        val relativePath: String = if (appFolder.trim().isEmpty()) {
            dirName
        } else {
            dirName + File.separator + appFolder
        }
        val uri: Uri? = getUriFromDisplayName(displayName, appFolder, dirType, dirName)
        Log.d(TAG, "DisplayName $displayName $uri")
        if (uri != null) {
            val resolver: ContentResolver = activity!!.applicationContext.contentResolver
            val selectionArgs =
                arrayOf(displayName, relativePath)
            resolver.delete(
                uri,
                MediaStore.Audio.Media.DISPLAY_NAME + " =?  AND " + MediaStore.Audio.Media.RELATIVE_PATH + " =? ",
                selectionArgs
            )
            Log.d("deleteFile", displayName)
            return true
        }
        return false
    }


    @kotlin.jvm.Throws
    private fun getUriFromDisplayName(
        displayName: String,
        appFolder: String,
        dirType: Int,
        dirName: String,
        externalVolumeName:String?,
    ): Uri? {

        val uri = getUriFromDirType(dirType,externalVolumeName)

        val relativePath: String = if (appFolder.trim().isEmpty()) {
            dirName + File.separator
        } else {
            dirName + File.separator + appFolder + File.separator
        }

        val projection: Array<String> = arrayOf(MediaStore.MediaColumns._ID)
        val selectionArgs =
            arrayOf(displayName, relativePath)
        val cursor: Cursor = activity!!.applicationContext.contentResolver.query(
            uri,
            projection,
            MediaStore.Audio.Media.DISPLAY_NAME + " =?  AND " + MediaStore.Audio.Media.RELATIVE_PATH + " =? ",
            selectionArgs,
            null
        )!!
        cursor.moveToFirst()
        return if (cursor.count > 0) {
            val columnIndex: Int = cursor.getColumnIndex(projection[0])
            val fileId: Long = cursor.getLong(columnIndex)
            cursor.close()
            Uri.parse("$uri/$fileId")
        } else {
            null
        }

    }

    private fun uriFromFilePath(path: String): String? {
        try {
            MediaScannerConnection.scanFile(
                activity!!.applicationContext,
                arrayOf(File(path).absolutePath),
                null
            ) { _, uri ->
                Log.d("uriFromFilePath", uri?.toString().toString())
                result.success(uri?.toString()?.trim())
            }

        } catch (_: Exception) {
        }
        return null
    }

    // Music/AppFolder
    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestForAccess(initialFolderRelativePath: String?) {

        val startDir: String? = initialFolderRelativePath?.split("/")?.joinToString("%2F")
        startDir?.let {
            Log.d("Start Dir", it)
        }


        // Choose a directory using the system's file picker.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            startDir?.let {
                // Optionally, specify a URI for the directory that should be opened in
                // the system file picker when it loads.
                var uriroot =
                    getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")    // get system root uri
                var scheme = uriroot.toString()
                Log.d("Debug", "INITIAL_URI scheme: $scheme")
                scheme = scheme.replace("/root/", "/document/")
                scheme += "%3A$startDir"
                uriroot = Uri.parse(scheme)
                // give changed uri to Intent
                Log.d("requestForAccess", "uri: $uriroot")
                putExtra(
                    DocumentsContract.EXTRA_INITIAL_URI,
                    uriroot
                )
            }
        }

        activity!!.startActivityForResult(intent, 992)
    }

    private fun editFile(uriString: String, path: String) {
        tempFilePath = path
        val fileUri = Uri.parse(uriString)
        try {
            val contentResolver: ContentResolver =
                activity!!.applicationContext.contentResolver
            contentResolver.openFileDescriptor(fileUri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { os ->
                    File(path).inputStream().use { it.copyTo(os) }
                }
            }
            File(path).delete()
            result.success(true)
        } catch (e: Exception) {
            if (e is RecoverableSecurityException) {
                val recoverableSecurityException = e as? RecoverableSecurityException
                recoverableSecurityException?.let {
                    val intentSender =
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    intentSender.let {
                        activity!!.startIntentSenderForResult(
                            intentSender, 993, null, 0, 0, 0, null
                        )
                    }
                }
            }
        }
    }

    private fun deleteFileUsingUri(uriString: String) {
        val fileUri = Uri.parse(uriString)
        val contentResolver: ContentResolver = activity!!.applicationContext.contentResolver
        try {
            DocumentsContract.deleteDocument(contentResolver, fileUri)
            result.success(true)
        } catch (e: Exception) {
            if (e is RecoverableSecurityException) {
                val recoverableSecurityException = e as? RecoverableSecurityException
                recoverableSecurityException?.let {
                    val intentSender =
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    intentSender.let {
                        activity!!.startIntentSenderForResult(
                            intentSender, 994, null, 0, 0, 0, null
                        )
                    }
                }
            }
        }
    }

    private fun isDeletable(uriString: String): Boolean {
        val uri = Uri.parse(uriString)
        if (!DocumentsContract.isDocumentUri(activity!!.applicationContext, uri)) {
            return false
        }

        val contentResolver: ContentResolver = activity!!.applicationContext.contentResolver
        val cursor: Cursor? = contentResolver.query(
            uri,
            arrayOf(DocumentsContract.Document.COLUMN_FLAGS),
            null,
            null,
            null
        )

        val flags: Int = cursor?.use {
            if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                0
            }
        } ?: 0

        return flags and DocumentsContract.Document.FLAG_SUPPORTS_DELETE != 0
    }

    private fun isWritable(uriString: String): Boolean {
        val uri = Uri.parse(uriString)
        if (!DocumentsContract.isDocumentUri(activity!!.applicationContext, uri)) {
            return false
        }

        val contentResolver: ContentResolver = activity!!.applicationContext.contentResolver
        val cursor: Cursor? = contentResolver.query(
            uri,
            arrayOf(DocumentsContract.Document.COLUMN_FLAGS),
            null,
            null,
            null
        )

        val flags: Int = cursor?.use {
            if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                0
            }
        } ?: 0

        return flags and DocumentsContract.Document.FLAG_SUPPORTS_WRITE != 0
    }

    private fun documentId(uriString: String): Long? {
        val uri = Uri.parse(uriString)
        if (!DocumentsContract.isDocumentUri(activity!!.applicationContext, uri)) {
            return null
        }

        val contentResolver: ContentResolver = activity!!.applicationContext.contentResolver
        val cursor: Cursor = contentResolver.query(
            uri,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
            null,
            null,
            null
        )!!

        cursor.moveToFirst()
        return if (cursor.count > 0) {
            val columnIndex: Int = cursor.getColumnIndex(cursor.columnNames[0])
            val fileId: Long = cursor.getLong(columnIndex)
            cursor.close()
            fileId
        } else {
            null
        }
    }

    private fun readFileUsingUri(uriString: String, path: String) {
        tempFilePath = path
        val fileUri = Uri.parse(uriString)
        try {
            val contentResolver: ContentResolver =
                activity!!.applicationContext.contentResolver
            contentResolver.openInputStream(fileUri)?.use { inputStream ->
                File(path).outputStream().use {
                    inputStream.copyTo(it)
                }
            }
            result.success(true)
        } catch (e: Exception) {
            if (e is RecoverableSecurityException) {
                val recoverableSecurityException = e as? RecoverableSecurityException
                recoverableSecurityException?.let {
                    val intentSender =
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    intentSender.let {
                        activity!!.startIntentSenderForResult(
                            intentSender, 995, null, 0, 0, 0, null
                        )
                    }
                }
            }
        }
    }

    private fun readFile(
        path: String,
        name: String,
        appFolder: String,
        dirType: Int,
        dirName: String
    ) {

        fileName = name
        tempFilePath = path

        Log.d("DirName", dirName)

        try {
            val uri: Uri? = getUriFromDisplayName(name, appFolder, dirType, dirName)
            if (uri != null) {
                val contentResolver: ContentResolver =
                    activity!!.applicationContext.contentResolver
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    File(path).outputStream().use {
                        inputStream.copyTo(it)
                    }
                }
                result.success(true)
            } else {
                result.success(false)
            }
        } catch (e: Exception) {
            if (e is RecoverableSecurityException) {
                val recoverableSecurityException = e as? RecoverableSecurityException
                recoverableSecurityException?.let {
                    val intentSender =
                        recoverableSecurityException.userAction.actionIntent.intentSender
                    intentSender.let {
                        activity!!.startIntentSenderForResult(
                            intentSender, 996, null, 0, 0, 0, null
                        )
                    }
                }
            }
        }
    }

    private fun isFileUriExist(uriString: String): Boolean {
        val fileUri = Uri.parse(uriString)
        return DocumentsContract.isDocumentUri(activity!!.applicationContext, fileUri)
    }

    private fun getFolderChildren(uriString: String) {
        try {
            val directoryUri = Uri.parse(uriString)
            val documentsTree =
                DocumentFile.fromTreeUri(activity!!.applicationContext, directoryUri)
            val children: MutableList<DocumentInfo> = mutableListOf()
            documentsTree?.let {
                val childDocuments = documentsTree.listFiles()
                for (childDocument in childDocuments) {
                    Log.d("File: ", "${childDocument.name}, ${childDocument.uri}")
                    children.add(
                        DocumentInfo(
                            childDocument.name,
                            childDocument.uri.toString().trim(),
                            childDocument.isVirtual,
                            childDocument.isDirectory,
                            childDocument.type,
                            childDocument.lastModified(),
                            childDocument.length(),
                            isWritable(childDocument.uri.toString()),
                            isDeletable(childDocument.uri.toString()),
                        )
                    )
                }
            }
            val documentTreeInfo = DocumentTreeInfo(directoryUri.toString().trim(), children)
            result.success(documentTreeInfo.json)
        } catch (e: Exception) {
            result.success("")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == 990) {
            if (resultCode == Activity.RESULT_OK) {
                saveFile(
                    tempFilePath,
                    fileName,
                    appFolder,
                    dirType,
                    dirName,
                    id3v2Tags,
                )
            } else {
                result.success(false)
            }
            return true
        } else if (requestCode == 991) {
            if (resultCode == Activity.RESULT_OK) {
                deleteFile(
                    fileName,
                    appFolder,
                    dirType,
                    dirName
                )
            } else {
                result.success(false)
            }
            return true
        } else if (requestCode == 992) {
            // https://developer.android.com/training/data-storage/shared/documents-files#persist-permissions
            if (resultCode == Activity.RESULT_OK) {
                var documentTreeInfo: DocumentTreeInfo? = null
                val uriList: MutableList<String> = mutableListOf()
                data?.data?.also { directoryUri ->
                    Log.d(TAG, "requestForAccess: G: $directoryUri")

                    uriList.add(directoryUri.toString().trim())


                    val contentResolver = activity!!.applicationContext.contentResolver
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(directoryUri, takeFlags)

                    val documentsTree =
                        DocumentFile.fromTreeUri(activity!!.applicationContext, directoryUri)

                    val children: MutableList<DocumentInfo> = mutableListOf()
                    documentsTree?.let {
                        val childDocuments = documentsTree.listFiles()
                        for (childDocument in childDocuments) {
                            Log.d("File: ", "${childDocument.name}, ${childDocument.uri}")
                            children.add(
                                DocumentInfo(
                                    childDocument.name,
                                    childDocument.uri.toString().trim(),
                                    childDocument.isVirtual,
                                    childDocument.isDirectory,
                                    childDocument.type,
                                    childDocument.lastModified(),
                                    childDocument.length(),
                                    null,
                                    null,
                                )
                            )
                            uriList.add(childDocument.uri.toString().trim())
                        }
                    }
                    documentTreeInfo = DocumentTreeInfo(directoryUri.toString().trim(), children)

                }
                val string = documentTreeInfo?.json ?: ""
                Log.d("requestForAccess: G", string)
                result.success(string)
            } else {
                result.success("")
            }
            return true
        } else if (requestCode == 993) {
            if (resultCode == Activity.RESULT_OK) {
                editFile(uriString, tempFilePath)
            } else {
                result.success(false)
            }
            return true
        } else if (requestCode == 994) {
            if (resultCode == Activity.RESULT_OK) {
                deleteFileUsingUri(uriString)
            } else {
                result.success(false)
            }
            return true
        } else if (requestCode == 995) {
            if (resultCode == Activity.RESULT_OK) {
                readFileUsingUri(uriString, tempFilePath)
            } else {
                result.success(false)
            }
            return true
        } else if (requestCode == 996) {
            if (resultCode == Activity.RESULT_OK) {
                readFile(
                    tempFilePath,
                    fileName,
                    appFolder,
                    dirType,
                    dirName
                )
            } else {
                result.success(false)
            }
            return true
        }
        return false
    }
}
