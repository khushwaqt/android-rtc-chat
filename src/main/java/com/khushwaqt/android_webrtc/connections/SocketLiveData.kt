package com.khushwaqt.android_webrtc.connections

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.khushwaqt.android_webrtc.BaseClass
import com.khushwaqt.android_webrtc.constants.*
import com.khushwaqt.android_webrtc.models.SocketEventModel
import com.khushwaqt.android_webrtc.models.Users
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.webrtc.SessionDescription
import timber.log.Timber
import java.net.URISyntaxException

class SocketLiveData : LiveData<SocketEventModel>() {

    init {
        connect()
    }

    @Synchronized
    override fun onActive() {
        super.onActive()
        if (!socket.connected()) {
            socket.connect()
        }
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in SocketEventModel>) {
        super.observe(owner, observer)
        Timber.d("Observe is $observer and owner is $owner")
    }

    @Synchronized
    private fun connect() {
        createSocket()
        socket.connect()

    }

    override fun onInactive() {
        super.onInactive()
        Timber.d("Observer is inactive")
    }

    @Synchronized
    fun sendEvent(eventModel: SocketEventModel) {
        Timber.d("emitting event ${eventModel.socketEvent}")
        if (eventModel.payload0 != null && eventModel.payload1 != null && eventModel.payload2 != null && eventModel.payload3 != null) {
            socket.emit(
                eventModel.socketEvent,
                eventModel.payload0,
                eventModel.payload1,
                eventModel.payload2,
                eventModel.payload3,
            )
        } else if (eventModel.payload0 != null && eventModel.payload1 != null && eventModel.payload2 != null) {
            socket.emit(
                eventModel.socketEvent,
                eventModel.payload0,
                eventModel.payload1,
                eventModel.payload2
            )
        } else if (eventModel.payload0 != null && eventModel.payload1 != null) {
            socket.emit(eventModel.socketEvent, eventModel.payload0, eventModel.payload1)
        } else if (eventModel.payload0 != null) {
            socket.emit(eventModel.socketEvent, eventModel.payload0)
        } else {
            socket.emit(eventModel.socketEvent)
        }
    }


    @Synchronized
    fun resetLastValue() {
        postEventValues(null)
    }

    @Synchronized
    private fun postEventValues(socketEventModel: SocketEventModel?) {
        Timber.tag("SocketToActivity").d("Posting event ${socketEventModel?.socketEvent}")
        CoroutineScope(Dispatchers.Main).launch {
            value = socketEventModel
        }


    }

    private fun createSocket() {
        Timber.d("Creating socket....")
        try {
            socket = IO.socket(SOCKET_URL)
            socket.on(Socket.EVENT_CONNECT) {
                Timber.d("Socket connected.")
                postEventValues(SocketEventModel(SOCKET_CONNECTED, ""))
            }
            socket.on(Manager.EVENT_TRANSPORT) { args ->
                val transport = args[0]
                Timber.d("Transport Event")

                Timber.d(args.toString())
                postEventValues(SocketEventModel(SOCKET_TRANSPORT, ""))
            }
            socket.on(Socket.EVENT_DISCONNECT) {
                Timber.d("Socket disconnected!")
                postEventValues(SocketEventModel(SOCKET_DISCONNECTED, ""))
            }
            socket.on(Socket.EVENT_RECONNECTING) {
                Timber.d("Socket reconnecting ...")
                postEventValues(SocketEventModel(SOCKET_RECONNECTING, ""))
            }
            socket.on("on_join") { params ->
                Timber.d("On Join.${BaseClass.gSon.toJson(params)}")
                postEventValues(SocketEventModel(ON_JOIN, params[0].toString()))

            }

            socket.on("on_login") { params ->
                Timber.d("On login.  ${BaseClass.gSon.toJson(params)}")
                postEventValues(SocketEventModel(ON_LOGIN, params[0].toString()))
            }
            socket.on("on_connection") { params ->
                Timber.d("on Connection.  ${BaseClass.gSon.toJson(params)}")
                postEventValues(SocketEventModel(ON_CONNECTION, params[0].toString()))
            }
            socket.on("new_user") { params ->
                Timber.d("New user joined ${BaseClass.gSon.toJson(params)}")
                postEventValues(SocketEventModel(ON_NEW_USER, params[0].toString()))
            }
            socket.on("on_offer") { params ->
                Timber.d("SDP data is.  ${BaseClass.gSon.toJson(params)}")
                val sdp =
                    BaseClass.gSon.fromJson(params[0].toString(), SessionDescription::class.java)
                val userId = params[1].toString()
                if (userId.toLong() == BaseClass.userId) {
                    return@on
                }
                if (sdp.type.toString() == "OFFER") {
                    Timber.d("on Offer.  ${BaseClass.gSon.toJson(params)}")
                    Timber.d("Offer received from $userId")
                    postEventValues(
                        SocketEventModel(
                            ON_OFFER,
                            params[0].toString(),
                            params[1].toString()
                        )
                    )
                } else {
                    Timber.d("on Answer.  ${BaseClass.gSon.toJson(params)}")
                    Timber.d("Answer received from $userId ")
                    postEventValues(
                        SocketEventModel(
                            ON_ANSWER,
                            params[0].toString(),
                            params[1].toString()
                        )
                    )
                }
            }
            socket.on("on_ice") { params ->
                Timber.d("ICE data is.  ${BaseClass.gSon.toJson(params)}")
                val userId = params[1].toString()
                Timber.d("ICE received from $userId")
                if (userId.toLong() == BaseClass.userId) {
                    return@on
                }
                postEventValues(
                    SocketEventModel(
                        ON_ICE,
                        params[0].toString(),
                        params[1].toString()
                    )
                )

            }
            socket.on("on_left") { params ->
                Timber.d("On user left.  ${BaseClass.gSon.toJson(params)}")
                postEventValues(SocketEventModel(ON_LEFT, params[0].toString()))

            }

            socket.on("on_server_messages") { params ->
                Timber.d("On server messages. ${BaseClass.gSon.toJson(params)}")
            }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    companion object {
        private val instance = SocketLiveData()
        private lateinit var socket: Socket
        fun get(): SocketLiveData {
            return instance
        }
    }
}