package com.khushwaqt.android_webrtc

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import timber.log.Timber
import timber.log.Timber.DebugTree


class BaseClass : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this

        Timber.plant(DebugTree())

    }


    companion object {
        lateinit var appContext: Context
        val userId = System.currentTimeMillis()
        val gSon = Gson()
    }
}