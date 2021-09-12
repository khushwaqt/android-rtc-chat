package com.khushwaqt.android_webrtc.models

import androidx.annotation.Keep
import com.khushwaqt.android_webrtc.apprtc.AppSdpObserver
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory

@Keep
class RtcClientModel(
    var userId: Long,
    var peerConnection: PeerConnection?,
    var peerConnectionFactory: PeerConnectionFactory,
    var sdpObserver: AppSdpObserver,
    var isMuted: Boolean = false
)