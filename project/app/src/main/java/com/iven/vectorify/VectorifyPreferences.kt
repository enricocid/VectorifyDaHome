package com.iven.vectorify

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.iven.vectorify.models.Metrics
import com.iven.vectorify.models.VectorifyWallpaper
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types


class VectorifyPreferences(context: Context) {

    private val mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val mMoshi = Moshi.Builder().build()

    private val typeWallpapersList =
        Types.newParameterizedType(MutableList::class.java, VectorifyWallpaper::class.java)

    var theme
        get() = mPrefs.getString("theme_pref_key", "theme_pref_auto")
        set(value) = mPrefs.edit { putString("theme_pref_key", value) }

    var savedWallpaper: VectorifyWallpaper
        get() = getObjectForClass(
            "recentwallpaper_key",
            VectorifyWallpaper::class.java
        ) ?: VectorifyWallpaper(Color.BLACK, Color.WHITE, R.drawable.android_logo_2019, 0, 0.35F, 0F, 0F)
        set(value) = putObjectForClass(
            "recentwallpaper_key",
            value,
            VectorifyWallpaper::class.java
        )

    var liveWallpaper: VectorifyWallpaper
        get() = getObjectForClass(
            "livewallpaper_key",
            VectorifyWallpaper::class.java
        ) ?: VectorifyWallpaper(Color.BLACK, Color.WHITE, R.drawable.android_logo_2019, 0, 0.35F, 0F, 0F)
        set(value) = putObjectForClass(
            "livewallpaper_key",
            value,
            VectorifyWallpaper::class.java
        )

    var savedMetrics: Metrics
        get() = getObjectForClass(
            "wallmetrics_key",
            Metrics::class.java
        ) ?: Metrics(720, 1280)
        set(value) = putObjectForClass("wallmetrics_key", value, Metrics::class.java)

    var recentSetups: MutableList<VectorifyWallpaper>?
        get() = getRecentWallpapers()
        set(value) = putRecentWallpapers(value)

    // Saves object into the Preferences using Moshi
    private fun <T: Any> getObjectForClass(key: String, clazz: Class<T>): T? {
        val json = mPrefs.getString(key, null)
        return if (json == null) {
            null
        } else {
            try {
                mMoshi.adapter(clazz).fromJson(json)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun <T: Any> putObjectForClass(key: String, value: T?, clazz: Class<T>) {
        val json = mMoshi.adapter(clazz).toJson(value)
        mPrefs.edit { putString(key, json) }
    }

    // Saves object into the Preferences using Moshi
    private fun <T: Any> putRecentWallpapers(value: T?) {
        val json = mMoshi.adapter<T>(typeWallpapersList).toJson(value)
        mPrefs.edit { putString("recentwallpapers_key", json) }
    }

    private fun <T: Any> getRecentWallpapers(): T? {
        val json = mPrefs.getString("recentwallpapers_key", null)
        return if (json == null) {
            null
        } else {
            try {
                mMoshi.adapter<T>(typeWallpapersList).fromJson(json)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    companion object {
        // Singleton prevents multiple instances of
        // VectorifyPreferences opening at the same time.
        @Volatile
        private var INSTANCE: VectorifyPreferences? = null

        fun initPrefs(context: Context): VectorifyPreferences {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the preferences
            return INSTANCE ?: synchronized(this) {
                val instance = VectorifyPreferences(context)
                INSTANCE = instance
                // return instance
                instance
            }
        }

        fun getPrefsInstance(): VectorifyPreferences {
            return INSTANCE ?: error("Preferences not initialized!")
        }
    }
}
