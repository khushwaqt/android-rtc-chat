package com.khushwaqt.android_webrtc.apprtc

import com.khushwaqt.android_webrtc.BaseClass
import org.webrtc.audio.JavaAudioDeviceModule

object CreateAudioDeviceModule {

    fun getAudiDeviceModule(): JavaAudioDeviceModule? {
        val isBuiltInAcousticEchoCancelerSupported = JavaAudioDeviceModule.isBuiltInAcousticEchoCancelerSupported()
        val isBuiltInNoiseSuppressorSupported = JavaAudioDeviceModule.isBuiltInNoiseSuppressorSupported()
        return JavaAudioDeviceModule.builder(BaseClass.appContext)
                .setUseHardwareAcousticEchoCanceler(isBuiltInAcousticEchoCancelerSupported)
                .setUseHardwareNoiseSuppressor(isBuiltInNoiseSuppressorSupported)
                .createAudioDeviceModule()
    }

}