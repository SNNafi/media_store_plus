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
    private lateinit var volume: String


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
                    call.argument("volume")!!
            )
        } else if (call.method == "deleteFile") {
            deleteFile(
                    call.argument("fileName")!!,
                    call.argument("appFolder")!!,
                    call.argument("dirType")!!,
                    call.argument("dirName")!!,
                    call.argument("volume")!!
            )
        } else if (call.method == "getFileUri") {
            val uri: Uri? = getUriFromDisplayName(
                    call.argument("fileName")!!,
                    call.argument("appFolder")!!,
                    call.argument("dirType")!!,
                    call.argument("dirName")!!,
                    call.argument("volume")!!
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
                    call.argument("dirName")!!,
                    call.argument("volume")!!
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
        } else if (call.method == "getFilePathFromUri") {
            filePathFromUri(Uri.parse(call.argument("uriString")!!))
        } else if (call.method == "getAvailableStorageDirectoriesNames") {
            result.success(getAvailableStorageDirectoryNames())
        } else if (call.method == "getAvailableStorageDirectoryPaths") {
            result.success(getAvailableStorageDirectoryPaths())
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
            volume: String
    ) {
        try {
            this.fileName = name
            this.tempFilePath = path
            this.appFolder = appFolder
            this.dirType = dirType
            this.dirName = dirName
            this.volume = volume
            val saveInfo: SaveInfo? = createOrUpdateFile(path, name, appFolder, dirType, dirName, volume)
            File(tempFilePath).delete()

            if (saveInfo != null) {
                result.success(saveInfo.json)
            } else {
                result.success(null)
            }

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
            Log.e("saveFile", e.message, e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun deleteFile(
            name: String,
            appFolder: String,
            dirType: Int,
            dirName: String,
            volume: String
    ) {
        try {
            this.fileName = name
            this.tempFilePath = ""
            this.appFolder = appFolder
            this.dirType = dirType
            this.dirName = dirName
            this.volume = volume
            val status: Boolean = deleteFileUsingDisplayName(
                    name,
                    appFolder,
                    dirType,
                    dirName,
                    volume
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
            Log.e("deleteFile", e.message, e)
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createOrUpdateFile(
            path: String,
            name: String,
            appFolder: String,
            dirType: Int,
            dirName: String,
            volume: String
    ): SaveInfo? {
        // { photo, music, video, download }
        Log.d("DirName", dirName)

        val relativePath: String = if (appFolder.trim().isEmpty()) {
            dirName;
        } else {
            dirName + File.separator + appFolder;
        }

        val collection: Uri = when (dirType) {
            0 -> {
                MediaStore.Images.Media.getContentUri(volume)
            }

            1 -> {
                MediaStore.Audio.Media.getContentUri(volume)
            }

            2 -> {
                MediaStore.Video.Media.getContentUri(volume)
            }

            else -> {
                MediaStore.Downloads.getContentUri(volume)
            }
        }

        val isReplaced = deleteFileUsingDisplayName(name, appFolder, dirType, dirName, volume)
        Log.d("saveFile<isReplaced>", isReplaced.toString())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, name)
                // put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                put(
                        MediaStore.Audio.Media.RELATIVE_PATH, relativePath
                )
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

            val displayName = getDisplayNameFromUri(uri).toString()

            Log.d("saveFile", name)
            Log.d("saveFile", uri.toString())
            Log.d("saveFile<displayName>", displayName)

            val saveStatus: SaveStatus = when (isReplaced) {
                false -> if (name.trim() == displayName.trim()) SaveStatus.CREATED else SaveStatus.DUPLICATED
                else -> SaveStatus.REPLACED
            }

            return SaveInfo(displayName, uri.toString(), saveStatus)
        }
        return null
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    @kotlin.jvm.Throws
    private fun deleteFileUsingDisplayName(
            displayName: String,
            appFolder: String,
            dirType: Int,
            dirName: String,
            volume: String
    ): Boolean {
        val relativePath: String = if (appFolder.trim().isEmpty()) {
            dirName + File.separator;
        } else {
            dirName + File.separator + appFolder + File.separator;
        }
        val uri: Uri? = getUriFromDisplayName(displayName, appFolder, dirType, dirName, volume)
        Log.d("DisplayName $displayName", uri.toString())
        if (uri != null) {
            val resolver: ContentResolver = activity!!.applicationContext.contentResolver
            val selectionArgs =
                    arrayOf(displayName, relativePath, volume)

            resolver.delete(
                    uri,
                    MediaStore.Audio.Media.DISPLAY_NAME + " =?  AND " + MediaStore.Audio.Media.RELATIVE_PATH + " =?  AND " + MediaStore.Audio.Media.VOLUME_NAME + " =? ",
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
            volume: String
    ): Uri? {
        val uri: Uri = when (dirType) {
            0 -> {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            1 -> {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            2 -> {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            else -> {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            }
        }

        val relativePath: String = if (appFolder.trim().isEmpty()) {
            dirName + File.separator;
        } else {
            dirName + File.separator + appFolder + File.separator;
        }

        val projection: Array<String> = arrayOf(MediaStore.MediaColumns._ID)
        val selectionArgs =
                arrayOf(displayName, relativePath, volume)
        val cursor: Cursor = activity!!.applicationContext.contentResolver.query(
                uri,
                projection,
                MediaStore.Audio.Media.DISPLAY_NAME + " =?  AND " + MediaStore.Audio.Media.RELATIVE_PATH + " =?  AND " + MediaStore.Audio.Media.VOLUME_NAME + " =? ",
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

        } catch (e: Exception) {
            Log.e("uriFromFilePath", e.message, e)
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
                    activity!!.applicationContext.contentResolver
            contentResolver.openFileDescriptor(fileUri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { os ->
                    File(path).inputStream().use { it.copyTo(os) }
                }
            }
            File(path).delete()
            result.success(true)
        } catch (e: Exception) {
            Log.e("editFile", e.message, e)
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
        val contentResolver: ContentResolver = activity!!.applicationContext.contentResolver
        try {
            contentResolver.delete(fileUri, null, null)
            result.success(true)
        } catch (e: Exception) {
            Log.e("deleteFileUsingUri", e.message, e)
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
        this.uriString = uriString
        this.tempFilePath = path
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
            Log.e("readFileUsingUri", e.message, e)
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
            dirName: String,
            volume: String
    ) {

        this.fileName = name
        this.tempFilePath = path
        this.appFolder = appFolder
        this.dirType = dirType
        this.dirName = dirName
        this.volume = volume

        Log.d("DirName", dirName)

        try {
            val uri: Uri? = getUriFromDisplayName(name, appFolder, dirType, dirName, volume)
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
            Log.e("readFile", e.message, e)
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
            Log.e("getFolderChildren", e.message, e)
            result.success("")
        }
    }

    private fun filePathFromUri(uri: Uri) {
        try {
            activity?.let {
                val projection: Array<String> = arrayOf(MediaStore.MediaColumns.DATA)
                val cursor: Cursor? = it.applicationContext.contentResolver.query(uri, projection, null, null, null)
                cursor?.let { c ->
                    if (c.moveToFirst()) {
                        val columnIndex = c.getColumnIndexOrThrow(projection[0]);
                        val path = c.getString(columnIndex)
                        Log.d("filePathFromUri[$uri]", path)
                        result.success(path)
                    }
                }
                cursor?.close()
            }

        } catch (e: Exception) {
            Log.e("filePathFromUri", e.message, e)
        }

    }

    private fun getDisplayNameFromUri(uri: Uri): String? {
        try {
            activity?.let {
                val projection: Array<String> = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
                val cursor: Cursor? = it.applicationContext.contentResolver.query(uri, projection, null, null, null)
                cursor?.let { c ->
                    if (c.moveToFirst()) {
                        val columnIndex = c.getColumnIndexOrThrow(projection[0]);
                        val name = c.getString(columnIndex)
                        Log.d("getDisplayNameFromUri[$uri]", name)
                        c.close()
                        return name
                    }
                }

            }
        } catch (e: Exception) {
            Log.e("getDisplayNameFromUri", e.message, e)
        }
        return null
    }

    // External Storage i.e SD cards
    private fun getAvailableStorageDirectoryNames(): List<String> {
        return activity?.let {
            if (Build.VERSION.SDK_INT > 29) MediaStore.getExternalVolumeNames(it.applicationContext).toList() else StorageUtil.getStorageDirectories(it.applicationContext).toList()
        } ?: listOf()
    }

    private fun getAvailableStorageDirectoryPaths(): List<String> {
        return activity?.let {
            StorageUtil.getStorageDirectories(it.applicationContext).toList()
        } ?: listOf()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        when (requestCode) {
            990 -> {
                if (resultCode == Activity.RESULT_OK) {
                    saveFile(
                            tempFilePath,
                            fileName,
                            appFolder,
                            dirType,
                            dirName,
                            volume
                    )
                } else {
                    result.success(null)
                }
                return true
            }

            991 -> {
                if (resultCode == Activity.RESULT_OK) {
                    deleteFile(
                            fileName,
                            appFolder,
                            dirType,
                            dirName,
                            volume
                    )
                } else {
                    result.success(false)
                }
                return true
            }

            992 -> {
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
            }

            993 -> {
                if (resultCode == Activity.RESULT_OK) {
                    editFile(this.uriString, this.tempFilePath)
                } else {
                    result.success(false)
                }
                return true
            }

            994 -> {
                if (resultCode == Activity.RESULT_OK) {
                    deleteFileUsingUri(this.uriString)
                } else {
                    result.success(false)
                }
                return true
            }

            995 -> {
                if (resultCode == Activity.RESULT_OK) {
                    readFileUsingUri(uriString, tempFilePath)
                } else {
                    result.success(false)
                }
                return true
            }

            996 -> {
                if (resultCode == Activity.RESULT_OK) {
                    readFile(
                            tempFilePath,
                            fileName,
                            appFolder,
                            dirType,
                            dirName,
                            volume
                    )
                } else {
                    result.success(false)
                }
                return true
            }

            else -> return false
        }
    }
}
