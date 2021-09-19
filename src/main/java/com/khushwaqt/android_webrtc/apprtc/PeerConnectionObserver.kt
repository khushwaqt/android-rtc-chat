package com.khushwaqt.android_webrtc.apprtc

import com.khushwaqt.android_webrtc.BaseClass
import com.khushwaqt.android_webrtc.connections.SocketRepository
import com.khushwaqt.android_webrtc.models.SocketEventModel
import org.webrtc.*
import timber.log.Timber


class PeerConnectionObserver : PeerConnection.Observer {
    var remoteView: SurfaceViewRenderer? = null
    var userId: String? = null
    var socketId: String? = null
    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
        Timber.tag("VoiceChat").d("On Signalling Change")
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
        Timber.tag("VoiceChat").d("On Ice Connection Change")
        RtcUtilities.connectionEstablished()
    }

    override fun onIceConnectionReceivingChange(b: Boolean) {
        Timber.tag("VoiceChat").d("On Ice Connection Receiving change")
    }

    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
        Timber.tag("VoiceChat").d("On Ice Gathering Change")
    }

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        Timber.tag("VoiceChat").d("Emitting IceCandidate to $userId")
        SocketRepository().getSocketLiveData()
            ?.sendEvent(
                SocketEventModel(
                    "ice",
                    BaseClass.gSon.toJson(iceCandidate),
                    BaseClass.userId.toString()
                )
            )
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        Timber.tag("VoiceChat").d("On IceCandidate Removed ")
    }

    override fun onAddStream(mediaStream: MediaStream) {
        Timber.tag("VoiceChat").d("On Add Stream")
        //            toggleMic(micStatus)
        mediaStream.videoTracks?.get(0)?.addSink(remoteView)
    }

    override fun onRemoveStream(mediaStream: MediaStream) {
        Timber.tag("VoiceChat").d("On Remove Stream")
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        Timber.tag("VoiceChat").d("On Data Channel")
    }

    override fun onRenegotiationNeeded() {
        Timber.tag("VoiceChat").d("On Renegotiation Needed")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        Timber.tag("VoiceChat").d("On Add Track")
    }
}
