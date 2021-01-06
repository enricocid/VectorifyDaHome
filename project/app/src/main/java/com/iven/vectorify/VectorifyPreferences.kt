package com.iven.vectorify

import android.content.Context
import android.content.SharedPreferences
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
    private val prefSavedVectorifyWallpaper =
            context.getString(R.string.saved_vectorify_wallpaper_key)
    private val prefRestoreVectorifyWallpaper =
            context.getString(R.string.restore_vectorify_wallpaper_key)
    private val prefRecentVectorifySetups =
            context.getString(R.string.recent_vectorify_wallpapers_key)
    private val prefSavedVectorifyMetrics =
            context.getString(R.string.saved_vectorify_metrics_key)

    private val mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val mMoshi = Moshi.Builder().build()

    private val typeWallpapersList =
            Types.newParameterizedType(MutableList::class.java, VectorifyWallpaper::class.java)

    var theme
        get() = mPrefs.getString(prefTheme, prefsThemeDefault)
        set(value) = mPrefs.edit { putString(prefTheme, value) }

    var restoreVectorifyWallpaper: VectorifyWallpaper?
        get() = getObjectForClass(
                prefRestoreVectorifyWallpaper,
                VectorifyWallpaper::class.java
        )
        set(value) = putObjectForClass(
                prefRestoreVectorifyWallpaper,
                value,
                VectorifyWallpaper::class.java
        )

    var vectorifyMetrics: Metrics
        get() = getObjectForClass(
                prefSavedVectorifyMetrics,
                Metrics::class.java
        ) ?: Metrics(720, 1280)
        set(value) = putObjectForClass(prefSavedVectorifyMetrics, value, Metrics::class.java)

    var liveVectorifyWallpaper: VectorifyWallpaper?
        get() = getObjectForClass(
                prefSavedVectorifyWallpaper,
                VectorifyWallpaper::class.java
        )
        set(value) = putObjectForClass(
                prefSavedVectorifyWallpaper,
                value,
                VectorifyWallpaper::class.java
        )

    var vectorifyWallpaperSetups: MutableList<VectorifyWallpaper>?
        get() = getObjectForType(
                prefRecentVectorifySetups,
                typeWallpapersList
        )
        set(value) = putObjectForType(prefRecentVectorifySetups, value, typeWallpapersList)

    // Saves object into the Preferences using Moshi
    private fun <T : Any> getObjectForClass(key: String, clazz: Class<T>): T? {
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

    private fun <T : Any> putObjectForClass(key: String, value: T?, clazz: Class<T>) {
        val json = mMoshi.adapter(clazz).toJson(value)
        mPrefs.edit { putString(key, json) }
    }

    // Saves object into the Preferences using Moshi
    private fun <T : Any> putObjectForType(key: String, value: T?, type: Type) {
        val json = mMoshi.adapter<T>(type).toJson(value)
        mPrefs.edit { putString(key, json) }
    }

    private fun <T : Any> getObjectForType(key: String, type: Type): T? {
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
}
