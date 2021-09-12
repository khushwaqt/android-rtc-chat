package com.khushwaqt.android_webrtc.apprtc

import com.khushwaqt.android_webrtc.BaseClass
import com.khushwaqt.android_webrtc.connections.SocketRepository
import com.khushwaqt.android_webrtc.models.SocketEventModel
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import timber.log.Timber


class AppSdpObserver : SdpObserver {
    var userId: String? = null
    var socketId: String? = null
    override fun onCreateSuccess(sessionDescription: SessionDescription) {
        Timber.tag("VoiceChat").d("SDP Observer On create success")

        val user = RtcManager.rctClientList.filterIndexed { _, user ->
            user.userId == userId?.toLong()
        }.firstOrNull()
        Timber.tag("VoiceChat").d("Setting local description.")
        user?.peerConnection?.setLocalDescription(user.sdpObserver, sessionDescription)
        Timber.tag("VoiceChat").d("Emitting session description to $userId")
        SocketRepository().getSocketLiveData()?.sendEvent(
            SocketEventModel(
                "offer",
                BaseClass.gSon.toJson(sessionDescription),
                BaseClass.userId.toString()
            )
        )
    }

    override fun onSetSuccess() {
        Timber.tag("VoiceChat").d("On set success")
    }

    override fun onCreateFailure(s: String) {
        Timber.tag("VoiceChat").d("On Create failure. Reason $s")
    }

    override fun onSetFailure(s: String) {
        Timber.tag("VoiceChat").d("On set failure. Reason $s")
    }
}