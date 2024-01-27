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

    private var activity: Activity? = null
    private lateinit var channel: MethodChannel
    private lateinit var result: io.flutter.plugin.common.MethodChannel.Result

    private lateinit var uriString: String
    private lateinit var fileName: String
    private lateinit var tempFilePath: String
    private var dirType: Int = 0
    private lateinit var dirName: String
    private lateinit var appFolder: String
    private var externalVolumeName: String? = null


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "media_store_plus")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        this.result = result
        if (call.method == "getPlatformSDKInt") {
            result.success(Build.VERSION.SDK_INT)
        } else if (call.method == "saveFile") {
            saveFile(
                    Uri.parse(call.argument("tempFilePath")!!).path!!,
                    call.argument("fileName")!!,
                    call.argument("appFolder")!!,
                    call.argument("dirType")!!,
                    call.argument("dirName")!!,
                    call.argument("externalVolumeName"),
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
        this.activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        this.activity = binding.activity as FlutterActivity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFile(
            path: String,
            name: String,
            appFolder: String,
            dirType: Int,
            dirName: String,
            externalVolumeName: String?
    ) {
        try {
            this.fileName = name
            this.tempFilePath = path
            this.appFolder = appFolder
            this.dirType = dirType
            this.dirName = dirName
            this.externalVolumeName = externalVolumeName
            createOrUpdateFile(path, name, appFolder, dirType, dirName, externalVolumeName)
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

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun deleteFile(
            name: String,
            appFolder: String,
            dirType: Int,
            dirName: String
    ) {
        try {
            this.fileName = name
            this.tempFilePath = ""
            this.appFolder = appFolder
            this.dirType = dirType
            this.dirName = dirName
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


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createOrUpdateFile(
            path: String,
            name: String,
            appFolder: String,
            dirType: Int,
            dirName: String,
            externalVolumeName: String?
    ) {
        // { photo, music, video, download }
        Log.d("DirName", dirName)

        val relativePath: String;
        if (appFolder.trim().isEmpty()) {
            relativePath = dirName;
        } else {
            relativePath = dirName + File.separator + appFolder;
        }

        val collection: Uri
        if (dirType == 0) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else if (dirType == 1) {
            collection = if (externalVolumeName != null) {
                MediaStore.Audio.Media.getContentUri(MediaStore.getExternalVolumeNames(activity!!.applicationContext).find { it.lowercase()  == externalVolumeName.lowercase()   })
            } else {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }
        } else if (dirType == 2) {
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }
        deleteFileUsingDisplayName(name, appFolder, dirType, dirName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, name)
                put(MediaStore.Audio.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }


            val resolver = activity!!.applicationContext.contentResolver
            val uri = resolver.insert(collection, values)!!

            resolver.openOutputStream(uri).use { os ->
                File(path).inputStream().use { it.copyTo(os!!) }
            }

            values.clear()
            values.put(MediaStore.Audio.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)

            Log.d("saveFile", name)
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    @kotlin.jvm.Throws
    private fun deleteFileUsingDisplayName(
            displayName: String,
            appFolder: String,
            dirType: Int,
            dirName: String
    ): Boolean {
        val relativePath: String;
        if (appFolder.trim().isEmpty()) {
            relativePath = dirName + File.separator;
        } else {
            relativePath = dirName + File.separator + appFolder + File.separator;
        }
        val uri: Uri? = getUriFromDisplayName(displayName, appFolder, dirType, dirName)
        Log.d("DisplayName $displayName", uri.toString())
        if (uri != null) {
            val resolver: ContentResolver = activity!!.applicationContext.getContentResolver()
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
            dirName: String
    ): Uri? {
        val uri: Uri
        if (dirType == 0) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else if (dirType == 1) {
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        } else if (dirType == 2) {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        }

        val relativePath: String;
        if (appFolder.trim().isEmpty()) {
            relativePath = dirName + File.separator;
        } else {
            relativePath = dirName + File.separator + appFolder + File.separator;
        }

        val projection: Array<String>
        projection = arrayOf(MediaStore.MediaColumns._ID)
        val selectionArgs =
                arrayOf(displayName, relativePath)
        val cursor: Cursor = activity!!.applicationContext.getContentResolver().query(
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
            Uri.parse(uri.toString() + "/" + fileId)
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
        this.uriString = uriString
        this.tempFilePath = path
        val fileUri = Uri.parse(uriString)
        try {
            val contentResolver: ContentResolver =
                    activity!!.applicationContext.getContentResolver()
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
        this.uriString = uriString
        val fileUri = Uri.parse(uriString)
        val contentResolver: ContentResolver = activity!!.applicationContext.getContentResolver()
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

        val contentResolver: ContentResolver = activity!!.applicationContext.getContentResolver()
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

        val contentResolver: ContentResolver = activity!!.applicationContext.getContentResolver()
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

        val contentResolver: ContentResolver = activity!!.applicationContext.getContentResolver()
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
        this.uriString = uriString
        this.tempFilePath = path
        val fileUri = Uri.parse(uriString)
        try {
            val contentResolver: ContentResolver =
                    activity!!.applicationContext.getContentResolver()
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

        this.fileName = name
        this.tempFilePath = path
        this.appFolder = appFolder
        this.dirType = dirType
        this.dirName = dirName

        Log.d("DirName", dirName)

        try {
            val uri: Uri? = getUriFromDisplayName(name, appFolder, dirType, dirName)
            if (uri != null) {
                val contentResolver: ContentResolver =
                        activity!!.applicationContext.getContentResolver()
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
        return DocumentsContract.isDocumentUri(activity!!.applicationContext, fileUri);
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
                        externalVolumeName,
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
                    Log.d("requestForAccess: G", directoryUri.toString())

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
                editFile(this.uriString, this.tempFilePath)
            } else {
                result.success(false)
            }
            return true
        } else if (requestCode == 994) {
            if (resultCode == Activity.RESULT_OK) {
                deleteFileUsingUri(this.uriString)
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
