package com.khushwaqt.android_webrtc.apprtc

import com.khushwaqt.android_webrtc.models.Users
import com.khushwaqt.android_webrtc.views.MainActivity
import org.webrtc.PeerConnection
import timber.log.Timber
import java.sql.Time
import java.util.*

object RtcUtilities {

    fun toggleMicMe(isMicEnabled: Boolean) {
        RtcManager.rctClientList.forEach { userItem ->
            userItem.peerConnection?.senders?.forEach { rtpSender ->
                rtpSender.track()?.setEnabled(isMicEnabled)
            }
        }
    }

    fun muteUser() {
        val item = RtcManager.rctClientList.firstOrNull()
        item?.peerConnection?.receivers?.forEach { rtpSender ->
            rtpSender.track()?.setEnabled(item.isMuted)
            val index = RtcManager.rctClientList.indexOf(item)
            item.isMuted = !item.isMuted
            RtcManager.rctClientList[index] = item
        }
    }

    fun connectionEstablished() {
        RtcManager.rctClientList.forEach { userItem ->
            if (userItem.peerConnection?.iceConnectionState() == PeerConnection.IceConnectionState.CONNECTED) {
                Timber.d("Connection established...")
                MainActivity.connectionStatus.postValue("Connected...")
            }
            if (userItem.peerConnection?.iceConnectionState() == PeerConnection.IceConnectionState.DISCONNECTED) {
                Timber.d("Connection disconnected...")
                MainActivity.connectionStatus.postValue("Disconnected...")
            }
            if (userItem.peerConnection?.iceConnectionState() == PeerConnection.IceConnectionState.CHECKING) {
                Timber.d("Connection checking...")
                MainActivity.connectionStatus.postValue("Checking...")
            }
            if (userItem.peerConnection?.iceConnectionState() == PeerConnection.IceConnectionState.COMPLETED) {
                Timber.d("Connection Completed...")
                MainActivity.connectionStatus.postValue("Completed...")
            }
            if (userItem.peerConnection?.iceConnectionState() == PeerConnection.IceConnectionState.CLOSED) {
                Timber.d("Connection Closed...")
                MainActivity.connectionStatus.postValue("Closed...")
            }
        }
    }
}