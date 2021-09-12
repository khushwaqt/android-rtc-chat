package com.khushwaqt.android_webrtc.views

import androidx.lifecycle.ViewModel
import com.khushwaqt.android_webrtc.connections.SocketLiveData
import com.khushwaqt.android_webrtc.connections.SocketRepository
import com.khushwaqt.android_webrtc.models.SocketEventModel

class MainActivityViewModel : ViewModel() {
    private val socketRepository = SocketRepository()
    var socketLiveData: SocketLiveData? = socketRepository.getSocketLiveData()

    fun setEvent(eventModel: SocketEventModel) {
        socketRepository.getSocketLiveData()?.sendEvent(eventModel)
    }
}