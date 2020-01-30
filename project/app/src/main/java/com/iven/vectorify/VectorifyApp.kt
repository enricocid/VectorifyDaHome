package com.iven.vectorify

import android.app.Application
import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import com.iven.vectorify.utils.Utils

val vectorifyPreferences: VectorifyPreferences by lazy {
    VectorifyApp.prefs
}

val deviceMetrics: Pair<Int, Int> by lazy {
    VectorifyApp.metrics
}

class VectorifyApp : Application() {

    companion object {
        lateinit var prefs: VectorifyPreferences
        lateinit var metrics: Pair<Int, Int>
    }

    override fun onCreate() {
        super.onCreate()

        prefs = VectorifyPreferences(applicationContext)

        //retrieve display specifications
        val window = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val d = DisplayMetrics()
        window.defaultDisplay.getRealMetrics(d)
        metrics = Pair(d.widthPixels, d.heightPixels)

        AppCompatDelegate.setDefaultNightMode(Utils.getDefaultNightMode(applicationContext))
    }
}
