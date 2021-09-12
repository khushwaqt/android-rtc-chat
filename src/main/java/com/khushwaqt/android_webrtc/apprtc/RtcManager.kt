package com.khushwaqt.android_webrtc.apprtc

import android.app.Application
import com.khushwaqt.android_webrtc.models.RtcClientModel
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.IceServer
import org.webrtc.PeerConnectionFactory
import timber.log.Timber
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList


object RtcManager {

    val rctClientList = CopyOnWriteArrayList<RtcClientModel>()
    private val mediaConstraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
    }
    private val iceServers: ArrayList<IceServer> by lazy { createServeList() }
    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(mediaConstraints) }
    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private val audioDeviceModule by lazy { CreateAudioDeviceModule.getAudiDeviceModule() }
    operator fun invoke(context: Application): RtcManager {
        initPeerConnectionFactory(context)
        return this
    }

    private fun initPeerConnectionFactory(context: Application) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory
            .builder()
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = true
                disableNetworkMonitor = true

            }).setVideoDecoderFactory(null).setVideoEncoderFactory(null)
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()
    }

    fun createPeerConnections(userId: String, socketId: String) {
        Timber.tag("VoiceChat").d("Creating peer connection observer for user $userId")
        val peerConnectionObserver = PeerConnectionObserver()
        peerConnectionObserver.userId = userId
        peerConnectionObserver.socketId = socketId

        Timber.tag("VoiceChat").d("Creating SDP observer for user $userId")
        val sdpObserver = AppSdpObserver()
        sdpObserver.userId = userId
        sdpObserver.socketId = socketId

        Timber.tag("VoiceChat").d("creating peerConnection for $userId")
        val peerConnection = peerConnectionFactory.createPeerConnection(
            iceServers,
            peerConnectionObserver
        )
        Timber.tag("VoiceChat").d("Setting audio properties for $userId")
        val audioTrack = peerConnectionFactory.createAudioTrack(
            userId,
            localAudioSource
        )
        val localMediaStream = peerConnectionFactory.createLocalMediaStream(userId)
        localMediaStream.addTrack(audioTrack)
        peerConnection?.addStream(localMediaStream)
        Timber.tag("VoiceChat").d("Peer connection created for $userId.  Adding to list.")
        val rtcClient = RtcClientModel(
            peerConnection = peerConnection,
            peerConnectionFactory = peerConnectionFactory,
            userId = userId.toLong(),
            sdpObserver = sdpObserver,
        )
        rctClientList.add(rtcClient)
        Timber.tag("VoiceChat").d("Peer connection for $userId added to list")
    }


    fun getMediaConstraints(): MediaConstraints {
        return mediaConstraints
    }

    private fun createServeList(): ArrayList<IceServer> {
        val listOfServers = ArrayList<IceServer>()
        listOfServers.add(
            IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        return listOfServers
    }
}