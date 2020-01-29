package com.iven.vectorify

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.preference.PreferenceManager
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class VectorifyPreferences(context: Context) {

    private val prefTheme = context.getString(R.string.theme_key)
    private val prefsThemeDefault = context.getString(R.string.theme_pref_light)
    private val prefSavedVectorifyWallpaper =
        context.getString(R.string.saved_vectorify_wallpaper_key)
    private val prefRecentVectorifySetups =
        context.getString(R.string.recent_vectorify_wallpapers_key)

    private val prefShowError = context.getString(R.string.error_show_message_key)

    private val mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val typeSavedVectorifyWallpaper = object : TypeToken<VectorifyWallpaper>() {}.type
    private val typeRecents = object : TypeToken<MutableList<VectorifyWallpaper>>() {}.type

    private val mGson = GsonBuilder().create()

    var theme
        get() = mPrefs.getString(prefTheme, prefsThemeDefault)
        set(value) = mPrefs.edit().putString(prefTheme, value).apply()

    val vectorifyWallpaperBackup = VectorifyWallpaper(
        Color.BLACK,
        Color.WHITE,
        R.drawable.android_logo_2019,
        R.string.title_tech,
        0.35F,
        0F,
        0F
    )

    var savedVectorifyWallpaper: VectorifyWallpaper?
        get() = getObject(
            prefSavedVectorifyWallpaper,
            typeSavedVectorifyWallpaper
        )
        set(value) = putObject(prefSavedVectorifyWallpaper, value)

    var vectorifyWallpaperSetups: MutableList<VectorifyWallpaper>?
        get() = getObject(
            prefRecentVectorifySetups,
            typeRecents
        )
        set(value) = putObject(prefRecentVectorifySetups, value)

    var hasToShowError
        get() = mPrefs.getBoolean(prefShowError, true)
        set(value) = mPrefs.edit().putBoolean(prefShowError, value).apply()

    /**
     * Saves object into the Preferences.
     * Only the fields are stored. Methods, Inner classes, Nested classes and inner interfaces are not stored.
     **/
    private fun <T> putObject(key: String, y: T) {
        //Convert object to JSON String.
        val inString = mGson.toJson(y)
        //Save that String in SharedPreferences
        mPrefs.edit().putString(key, inString).apply()
    }

    /**
     * Get object from the Preferences.
     **/
    private fun <T> getObject(key: String, t: Type): T? {
        //We read JSON String which was saved.
        val value = mPrefs.getString(key, null)

        //JSON String was found which means object can be read.
        //We convert this JSON String to model object. Parameter "c" (of type Class<T>" is used to cast.
        return mGson.fromJson(value, t)
    }
}
