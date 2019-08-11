package com.iven.vectorify

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.preference.PreferenceManager

class VectorifyPreferences(context: Context) {

    private val mDefaultBackgroundColor = Color.BLACK
    private val mDefaultVectorColor = Color.WHITE

    private val prefBackgroundColor = context.getString(R.string.background_color_key)
    private val prefVectorColor = context.getString(R.string.vectors_color_key)
    private val prefVector = context.getString(R.string.vector_key)
    private val prefTheme = context.getString(R.string.theme_key)
    private val prefScale = context.getString(R.string.scale_key)
    private val prefRecentSetups = context.getString(R.string.recent_setups_key)

    private val mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var backgroundColor: Int
        get() = mPrefs.getInt(prefBackgroundColor, mDefaultBackgroundColor)
        set(value) = mPrefs.edit().putInt(prefBackgroundColor, value).apply()

    var vectorColor: Int
        get() = mPrefs.getInt(prefVectorColor, mDefaultVectorColor)
        set(value) = mPrefs.edit().putInt(prefVectorColor, value).apply()

    var vector: Int
        get() = mPrefs.getInt(prefVector, R.drawable.android)
        set(value) = mPrefs.edit().putInt(prefVector, value).apply()

    var theme: Int
        get() = mPrefs.getInt(prefTheme, R.style.AppTheme)
        set(value) = mPrefs.edit().putInt(prefTheme, value).apply()

    var scale: Float
        get() = mPrefs.getFloat(prefScale, 0.35F)
        set(value) = mPrefs.edit().putFloat(prefScale, value).apply()

    var recentSetups: MutableSet<String>
        get() = mPrefs.getStringSet(prefRecentSetups, mutableSetOf())!!
        set(value) = mPrefs.edit().putStringSet(prefRecentSetups, value).apply()
}
