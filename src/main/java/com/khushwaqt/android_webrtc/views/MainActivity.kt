package com.khushwaqt.android_webrtc.views

import android.Manifest
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.khushwaqt.android_webrtc.BaseClass
import com.khushwaqt.android_webrtc.R
import com.khushwaqt.android_webrtc.apprtc.RtcManager
import com.khushwaqt.android_webrtc.apprtc.RtcUtilities
import com.khushwaqt.android_webrtc.connections.SocketRepository
import com.khushwaqt.android_webrtc.constants.*
import com.khushwaqt.android_webrtc.databinding.ActivityMainBinding
import com.khushwaqt.android_webrtc.models.RtcClientModel
import com.khushwaqt.android_webrtc.models.SocketEventModel
import com.khushwaqt.android_webrtc.models.Users
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.util.*


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private var remoteInit = false
    private var localInit = false
    private var isOnSpeaker: Boolean = false
    private var toggleMyMic: Boolean = false
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var xUser: Users
    private val hashMapCallSetUp = HashMap<String, Boolean>()

    private lateinit var binding: ActivityMainBinding
    private val statsTimer by lazy { Timer() }
    private var me = Users(userId = BaseClass.userId, reqTime = System.currentTimeMillis())
    private val rtcManager by lazy { RtcManager(application as BaseClass) }


    companion object {
        var connectionStatus = MutableLiveData<String>()
        const val MIC_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initEvents()
    }

    private fun initEvents() {
        viewModel.socketLiveData?.observe(this, socketEventModelObserver)
        binding.tvUserId.text = me.userId.toString()
        connectionStatus.observe(this, Observer { status ->
            if (status.startsWith("connected", ignoreCase = true)) {
                binding.ivSpeaker.visibility = View.VISIBLE
                binding.ivMute.visibility = View.VISIBLE
                binding.ivMuteMe.visibility = View.VISIBLE
            }
            binding.tvStatus.text = status
        })
        ("User Id-" + BaseClass.userId).also { binding.tvUserId.text = it }
        binding.btnStartEnd.setOnClickListener {
            if (binding.btnStartEnd.text.toString().equals("Start Call", ignoreCase = true)) {
                binding.btnStartEnd.text = getString(R.string.end_call)
                checkPermissions()
            } else {
                binding.btnStartEnd.text = getString(R.string.start_call)
                leaveChat()
                binding.ivSpeaker.visibility = View.INVISIBLE
                binding.ivMute.visibility = View.INVISIBLE
                binding.ivMuteMe.visibility = View.INVISIBLE
            }
        }

        binding.ivMuteMe.setOnClickListener {
            Toast.makeText(this, "Mic toggled.", Toast.LENGTH_LONG).show()
            toggleMyMic = !toggleMyMic
            RtcUtilities.toggleMicMe(!toggleMyMic)

        }
        binding.ivMute.setOnClickListener {
            Toast.makeText(this, "Mic toggled for other user.", Toast.LENGTH_LONG).show()
            RtcUtilities.muteUser()
        }

        binding.ivSpeaker.setOnClickListener {
            isOnSpeaker = !isOnSpeaker
            toggleSpeaker()
        }
    }

    private fun toggleSpeaker() {
        Toast.makeText(this, "Speaker phone toggled", Toast.LENGTH_LONG).show()
        val mAudioMgr = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioMgr.isSpeakerphoneOn = isOnSpeaker
    }

    private val socketEventModelObserver: Observer<SocketEventModel> =
        Observer { response ->
            Timber.tag("SocketToActivity").d("Receiving event ${response?.socketEvent}")
            if (response == null) {
                Timber.d("Observer items are null")
                return@Observer
            }
            Timber.d("Observer items are ${BaseClass.gSon.toJson(response)}")
            when (response.socketEvent) {
                SOCKET_CONNECTED -> {
                    Timber.d("Socket connected successfully.")
                }
                ON_OFFER -> {
                    onOfferReceived(response)
                }
                ON_ANSWER -> {
                    onAnswerReceived(response)
                }
                ON_ICE -> {
                    onIceCandidateReceived(response)
                }
                ON_LEFT -> {
                    onUserLeft(response)
                }
                ON_NEW_USER -> {
                    Timber.d("On new User")
                    onJoin(response)
                }
                else -> {
                    Timber.d("Default event found. ${response.socketEvent}")
                }
            }
        }


    private fun leaveChat() {
        hashMapCallSetUp.clear()
        viewModel.setEvent(SocketEventModel(("leave")))
        rtcManager.rctClientList.forEach { users ->
            users.peerConnectionFactory.stopAecDump()
            users.peerConnection?.close()
        }
        rtcManager.rctClientList.clear()
    }

    private fun checkPermissions() {
        val grantedAudio =
            EasyPermissions.hasPermissions(this, Manifest.permission.RECORD_AUDIO)
        val grantedVideo =
            EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)
        if (grantedAudio && grantedVideo) {
            SocketRepository().getSocketLiveData()
                ?.sendEvent(SocketEventModel("join", BaseClass.gSon.toJson(me)))
        } else {
            if (!grantedAudio) {
                EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.permission_required),
                    MIC_PERMISSION,
                    Manifest.permission.RECORD_AUDIO
                )
            }
            if (!grantedVideo) {
                EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.permission_required),
                    MIC_PERMISSION,
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Timber.d("Permissions Granted: $requestCode :$perms.size")
        checkPermissions()

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Timber.d("Permissions Denied: $requestCode : $perms.size")
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    private fun onJoin(data: SocketEventModel) {
        Timber.d("On user joined. ${data.payload0}")
        xUser = BaseClass.gSon.fromJson(data.payload0, Users::class.java)
        initCall()
    }


    private fun onOfferReceived(
        data: SocketEventModel
    ) {
        val client = rtcManager.rctClientList.firstOrNull()
        client?.peerConnection?.setRemoteDescription(
            client.sdpObserver, BaseClass.gSon.fromJson(
                data.payload0,
                SessionDescription::class.java
            )
        )
        Timber.tag("VoiceChat").d("Offer received. Setting local and remote view")
        client?.peerConnection?.createAnswer(client.sdpObserver, rtcManager.getMediaConstraints())
    }


    private fun onAnswerReceived(
        data: SocketEventModel,
    ) {
        Timber.d("Answer received ")
        val client = rtcManager.rctClientList.firstOrNull()
        client?.peerConnection?.setRemoteDescription(
            client.sdpObserver,
            BaseClass.gSon.fromJson(data.payload0, SessionDescription::class.java)
        )
    }

    private fun onIceCandidateReceived(
        data: SocketEventModel
    ) {
        val client = rtcManager.rctClientList.firstOrNull()
        client?.peerConnection?.addIceCandidate(
            BaseClass.gSon.fromJson(
                data.payload0,
                IceCandidate::class.java
            )
        )
    }

    private fun onUserLeft(data: SocketEventModel) {
        val leftUserId = data.payload0.orEmpty()
        Timber.d("On user left. $leftUserId")
        val leftUser = rtcManager.rctClientList.filterIndexed { _, item ->
            item.userId == leftUserId.toLong()
        }.firstOrNull()
        rtcManager.rctClientList.removeAll { x -> x.userId == leftUserId.toLong() }.also {
            hashMapCallSetUp.remove(leftUserId)
            Timber.d("Closing peer connection...")
            leftUser?.peerConnectionFactory?.stopAecDump()
            leftUser?.peerConnection?.close()
            Timber.d("Closing peer connection Done.")
        }

    }

    private fun initCall() {
        Timber.tag("VoiceChat").d("Init call...")

        if (!remoteInit) {
            remoteInit = true
            RtcManager.initSurfaceRendrer(binding.remoteView)
        }
        if (!localInit) {
            localInit = true
            RtcManager.initSurfaceRendrer(binding.localView)
        }
        rtcManager.createPeerConnections(
            xUser.userId.toString(),
            xUser.userId.toString(), binding.localView,
            binding.remoteView
        )

        startStatsTimer()
        if (me.reqTime < xUser.reqTime) {
            val newUser = rtcManager.rctClientList.firstOrNull()
            newUser?.peerConnection?.createOffer(
                newUser.sdpObserver,
                rtcManager.getMediaConstraints()
            )
            Timber.tag("VoiceChat").d("Call offer created for ${xUser.userId}")

        }

    }

    @Synchronized
    private fun startStatsTimer() {
        try {
            statsTimer.schedule(object : TimerTask() {
                override fun run() {
                    val concurrentModiSecure = rtcManager.rctClientList
                    val iterator = concurrentModiSecure.iterator()
                    while (iterator.hasNext()) {
                        getStats(iterator.next())
                    }
                }
            }, 0, 1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getStats(item: RtcClientModel) {
        item.peerConnection?.getStats { rtcStatsReport ->
            val iterator = rtcStatsReport.statsMap.iterator()
            while (iterator.hasNext()) {
                val rtcStats = iterator.next()
                if (rtcStats.key.startsWith("RTCInboundRTPAudioStream", ignoreCase = true)) {
                    val audioLevel =
                        rtcStats.value.members["audioLevel"].toString().toDouble()
                    Timber.d("Audio level  $audioLevel")
                    if (audioLevel > 100) {
                        Timber.d("${item.userId} is talking")
                    }
                }
            }
        }
    }

}
