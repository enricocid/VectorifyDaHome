package com.iven.vectorify.preferences

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.preference.PreferenceManager
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.iven.vectorify.R
import com.iven.vectorify.utils.Utils
import java.lang.reflect.Type

class VectorifyPreferences(context: Context) {

    private val mDefaultBackgroundColor = Color.BLACK
    private val mDefaultVectorColor = Color.WHITE

    private val prefBackgroundColor = context.getString(R.string.background_color_key)
    private val prefVectorColor = context.getString(R.string.vectors_color_key)
    private val prefVector = context.getString(R.string.vector_key)
    private val prefCategory = context.getString(R.string.category_key)
    private val prefTheme = context.getString(R.string.theme_key)
    private val prefsThemeDefault = context.getString(R.string.theme_pref_light)
    private val prefScale = context.getString(R.string.scale_key)
    private val prefRecentSetups = context.getString(R.string.recent_setups_key)
    private val prefHorizontalOffset = context.getString(R.string.horizontal_offset_key)
    private val prefVerticalOffset = context.getString(R.string.vertical_offset_key)

    private val prefShowError = context.getString(R.string.error_show_message_key)

    private val mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val typeRecents = object : TypeToken<MutableList<Recent>>() {}.type

    private val mGson = GsonBuilder().create()

    var backgroundColor
        get() = mPrefs.getInt(prefBackgroundColor, mDefaultBackgroundColor)
        set(value) = mPrefs.edit().putInt(prefBackgroundColor, value).apply()

    var vectorColor
        get() = mPrefs.getInt(prefVectorColor, mDefaultVectorColor)
        set(value) = mPrefs.edit().putInt(prefVectorColor, value).apply()

    var vector
        get() = mPrefs.getInt(prefVector, Utils.getDefaultVectorForApi())
        set(value) = mPrefs.edit().putInt(prefVector, value).apply()

    var category
        get() = mPrefs.getInt(prefCategory, 0)
        set(value) = mPrefs.edit().putInt(prefCategory, value).apply()

    var theme
        get() = mPrefs.getString(prefTheme, prefsThemeDefault)
        set(value) = mPrefs.edit().putString(prefTheme, value).apply()

    var scale
        get() = mPrefs.getFloat(prefScale, 0.35F)
        set(value) = mPrefs.edit().putFloat(prefScale, value).apply()

    var horizontalOffset
        get() = mPrefs.getFloat(prefHorizontalOffset, 0F)
        set(value) = mPrefs.edit().putFloat(prefHorizontalOffset, value).apply()

    var verticalOffset
        get() = mPrefs.getFloat(prefVerticalOffset, 0F)
        set(value) = mPrefs.edit().putFloat(prefVerticalOffset, value).apply()

    var recentSetups: MutableList<Recent>?
        get() = getObject(
            prefRecentSetups,
            typeRecents
        )
        set(value) = putObject(prefRecentSetups, value)

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
