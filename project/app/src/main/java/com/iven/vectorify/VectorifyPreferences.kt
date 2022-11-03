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
import java.lang.reflect.Type

class VectorifyPreferences(context: Context) {

    private val prefTheme = context.getString(R.string.theme_key)
    private val prefsThemeDefault = context.getString(R.string.theme_pref_auto)

    private val prefLiveWallpaper =
        context.getString(R.string.live_wallpaper_key)
    private val prefSavedWallpaper =
        context.getString(R.string.saved_wallpaper_key)
    private val prefSavedWallpaperLand =
        context.getString(R.string.saved_wallpaper_land_key)
    private val prefRecentSetups =
        context.getString(R.string.recent_wallpapers_key)
    private val prefRecentSetupsLand =
        context.getString(R.string.recent_wallpapers_land_key)
    private val prefSavedMetrics =
        context.getString(R.string.saved_metrics_key)

    private val mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val mMoshi = Moshi.Builder().build()

    private val typeWallpapersList =
            Types.newParameterizedType(MutableList::class.java, VectorifyWallpaper::class.java)

    var theme
        get() = mPrefs.getString(prefTheme, prefsThemeDefault)
        set(value) = mPrefs.edit { putString(prefTheme, value) }

    var savedWallpaper: VectorifyWallpaper
        get() = getObjectForClass(
            prefSavedWallpaper,
            VectorifyWallpaper::class.java
        ) ?: VectorifyWallpaper(Color.BLACK, Color.WHITE, R.drawable.android_logo_2019, 0, 0.35F, 0F, 0F)
        set(value) = putObjectForClass(
            prefSavedWallpaper,
            value,
            VectorifyWallpaper::class.java
        )

    var savedWallpaperLand: VectorifyWallpaper
        get() = getObjectForClass(
            prefSavedWallpaperLand,
            VectorifyWallpaper::class.java
        ) ?: VectorifyWallpaper(Color.BLACK, Color.WHITE, R.drawable.android_logo_2019, 0, 0.35F, 0F, 0F)
        set(value) = putObjectForClass(
            prefSavedWallpaperLand,
            value,
            VectorifyWallpaper::class.java
        )

    var liveWallpaper: VectorifyWallpaper
        get() = getObjectForClass(
            prefLiveWallpaper,
            VectorifyWallpaper::class.java
        ) ?: VectorifyWallpaper(Color.BLACK, Color.WHITE, R.drawable.android_logo_2019, 0, 0.35F, 0F, 0F)
        set(value) = putObjectForClass(
            prefLiveWallpaper,
            value,
            VectorifyWallpaper::class.java
        )

    var savedMetrics: Metrics
        get() = getObjectForClass(
            prefSavedMetrics,
            Metrics::class.java
        ) ?: Metrics(720, 1280)
        set(value) = putObjectForClass(prefSavedMetrics, value, Metrics::class.java)

    var recentSetups: MutableList<VectorifyWallpaper>?
        get() = getObjectForType(
            prefRecentSetups,
            typeWallpapersList
        )
        set(value) = putObjectForType(prefRecentSetups, value, typeWallpapersList)

    var recentSetupsLand: MutableList<VectorifyWallpaper>?
        get() = getObjectForType(
            prefRecentSetupsLand,
            typeWallpapersList
        )
        set(value) = putObjectForType(prefRecentSetupsLand, value, typeWallpapersList)

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
    private fun <T: Any> putObjectForType(key: String, value: T?, type: Type) {
        val json = mMoshi.adapter<T>(type).toJson(value)
        mPrefs.edit { putString(key, json) }
    }

    private fun <T: Any> getObjectForType(key: String, type: Type): T? {
        val json = mPrefs.getString(key, null)
        return if (json == null) {
            null
        } else {
            try {
                mMoshi.adapter<T>(type).fromJson(json)
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
