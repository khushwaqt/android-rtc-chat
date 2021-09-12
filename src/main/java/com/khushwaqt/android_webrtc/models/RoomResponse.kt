package com.khushwaqt.android_webrtc.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class RoomResponse(
    @SerializedName("result")
    var result: Boolean? = null,
    @SerializedName("message")
    var message: String? = null,
    @SerializedName("roomData")
    var roomData: RoomData? = null
) {
    @Keep
    data class RoomData(
        @SerializedName("_id")
        var id: String? = null,
        @SerializedName("roomName")
        var roomName: String? = null,
        @SerializedName("count")
        var count: Int? = null,
        @SerializedName("users")
        var users: List<User?>? = null,
        @SerializedName("createdAt")
        var createdAt: String? = null,
        @SerializedName("__v")
        var v: Int? = null
    ) {
        @Keep
        data class User(
            @SerializedName("created")
            var created: String? = null,
            @SerializedName("_id")
            var id: String? = null,
            @SerializedName("userId")
            var userId: String? = null,
            @SerializedName("userNick")
            var userNick: String? = null,
            @SerializedName("socketId")
            var socketId: String? = null,
            @SerializedName("reqTime")
            var reqTime: Long = 0,
            @SerializedName("imagePath")
            var imagePath: String? = null
        )
    }
}