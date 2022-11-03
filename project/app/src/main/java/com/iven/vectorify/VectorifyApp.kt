package com.iven.vectorify

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.iven.vectorify.utils.Utils


class VectorifyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        VectorifyPreferences.initPrefs(applicationContext)
        AppCompatDelegate.setDefaultNightMode(Utils.getDefaultNightMode(applicationContext))
    }
}
