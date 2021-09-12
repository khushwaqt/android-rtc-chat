package com.khushwaqt.android_webrtc.connections

class SocketRepository {

    private var socketLiveData: SocketLiveData? = null

    init {
        socketLiveData = SocketLiveData.get()
    }

    fun getSocketLiveData(): SocketLiveData? {
        return socketLiveData
    }
}