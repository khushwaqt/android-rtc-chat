package com.khushwaqt.android_webrtc.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Users(
    @SerializedName("result")
    var result: Boolean? = null,
    @SerializedName("message")
    var message: String? = null,

    @SerializedName("userId")
    var userId: Long,

    var reqTime: Long = 0
) {
    override fun toString(): String {
        return "Users(result=$result,message=$message, userId=$userId)"
    }
}