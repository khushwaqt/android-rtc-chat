package com.khushwaqt.android_webrtc.apprtc

import android.app.Application
import android.content.Context
import com.khushwaqt.android_webrtc.BaseClass
import com.khushwaqt.android_webrtc.models.RtcClientModel
import org.webrtc.*
import org.webrtc.PeerConnection.IceServer
import timber.log.Timber
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import org.webrtc.VideoCapturer





object RtcManager {

    val rctClientList = CopyOnWriteArrayList<RtcClientModel>()
    private val mediaConstraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }


    private val rootEglBase by lazy { EglBase.create() }
    private val videoCapturer by lazy { getVideoCapturer(BaseClass.appContext) }
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
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
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    fun initSurfaceRendrer(view: SurfaceViewRenderer) {
        view.setMirror(true)
        view.setEnableHardwareScaler(true)
        view.init(rootEglBase.eglBaseContext, null)
    }


    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory
            .builder()
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = true
                disableNetworkMonitor = true

            }).setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    rootEglBase.eglBaseContext,
                    true,
                    true
                )
            )
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()
    }

    fun createPeerConnections(
        userId: String,
        socketId: String,
        localStream: SurfaceViewRenderer,
        remoteView: SurfaceViewRenderer
    ) {
        Timber.tag("VoiceChat").d("Init  video capturer.")

        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
        (videoCapturer as VideoCapturer).initialize(
            surfaceTextureHelper,
            localStream.context,
            localVideoSource.capturerObserver
        )
        videoCapturer.startCapture(320, 240, 60)
        Timber.tag("VoiceChat").d("Creating peer connection observer for user $userId")
        val peerConnectionObserver = PeerConnectionObserver()
        peerConnectionObserver.userId = userId
        peerConnectionObserver.socketId = socketId
        peerConnectionObserver.remoteView = remoteView
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
        val localVideoTrack =
            peerConnectionFactory.createVideoTrack(userId + "_video", localVideoSource)
        localVideoTrack?.addSink(localStream)
        val localMediaStream = peerConnectionFactory.createLocalMediaStream(userId + "local_stream")
        localMediaStream.addTrack(audioTrack)
        localMediaStream.addTrack(localVideoTrack)
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

    private fun getVideoCapturer(context: Context) =
        Camera2Enumerator(context).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }

}