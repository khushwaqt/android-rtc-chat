package com.khushwaqt.android_webrtc.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Response(
    @SerializedName("result")
    var result: Boolean? = null,
    @SerializedName("message")
    var message: String? = null,
    @SerializedName("room")
    var roomId: String? = null,
    @SerializedName("imagePath")
    var imagePath: String? = null
)