package com.khushwaqt.android_webrtc.models

import androidx.annotation.Keep

@Keep
class SocketEventModel(
    var socketEvent: String,
    var payload0: String? = null,
    var payload1: String? = null,
    var payload2: String? = null,
    var payload3: String? = null
)