package com.snnafi.media_store_plus

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

enum class SaveStatus {
    @SerializedName("0")
    CREATED,

    @SerializedName("1")
    REPLACED,

    @SerializedName("2")
    DUPLICATED
}

data class SaveInfo(
        @SerializedName("name")
        val name: String,
        @SerializedName("uri")
        val uri: String,
        @SerializedName("save_status")
        val status: SaveStatus
) {
    val isSucessful: Boolean
        get() = status != SaveStatus.DUPLICATED

    val json: String
        get() = Gson().toJson(this)
}