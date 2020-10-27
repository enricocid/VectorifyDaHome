package com.iven.vectorify

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.iven.vectorify.utils.Utils

val vectorifyPreferences: VectorifyPreferences by lazy {
    VectorifyApp.prefs
}

class VectorifyApp : Application() {

    companion object {
        lateinit var prefs: VectorifyPreferences
    }

    override fun onCreate() {
        super.onCreate()

        prefs = VectorifyPreferences(applicationContext)

        AppCompatDelegate.setDefaultNightMode(Utils.getDefaultNightMode(applicationContext))
    }
}
