package com.iven.vectorify

import android.app.Application

val mVectorifyPreferences: VectorifyPreferences by lazy {
    IconifyApp.prefs
}

class IconifyApp : Application() {
    companion object {
        lateinit var prefs: VectorifyPreferences
    }

    override fun onCreate() {
        prefs = VectorifyPreferences(applicationContext)
        super.onCreate()
    }
}