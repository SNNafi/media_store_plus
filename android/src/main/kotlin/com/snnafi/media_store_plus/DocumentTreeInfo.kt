package com.snnafi.media_store_plus

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName


data class DocumentTreeInfo(
    @SerializedName("uri_string")
    val uri: String,
    @SerializedName("children")
    val children: List<DocumentInfo>
) {
    val json: String
        get() = Gson().toJson(this);
}

data class DocumentInfo(
    @SerializedName("name")
    val name: String?,
    @SerializedName("uri_string")
    val uri: String,
    @SerializedName("is_virtual")
    val isVirtual: Boolean,
    @SerializedName("is_directory")
    val isDirectory: Boolean,
    @SerializedName("file_type")
    val fileType: String?,
    @SerializedName("last_modified")
    val lastModified: Long,
    @SerializedName("file_length")
    val fileLength: Long,
    @SerializedName("is_writable")
    val isWritable: Boolean?,
    @SerializedName("is_deletable")
    val isDeletable: Boolean?,

    )