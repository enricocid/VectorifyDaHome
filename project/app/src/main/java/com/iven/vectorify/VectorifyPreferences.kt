package com.iven.vectorify

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.preference.PreferenceManager

class VectorifyPreferences(context: Context) {

    private val mDefaultBackgroundColor = Color.BLACK
    private val mDefaultIconColor = Color.WHITE

    private val prefBackgroundColor = context.getString(R.string.background_color_key)
    private val prefIconColor = context.getString(R.string.icon_color_key)
    private val prefIsBackgroundAccented = context.getString(R.string.accent_background_set)
    private val prefIsIconAccented = context.getString(R.string.accent_icon_set)
    private val prefIcon = context.getString(R.string.icon_key)
    private val prefTheme = context.getString(R.string.theme_key)

    private val mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var backgroundColor: Int
        get() = mPrefs.getInt(prefBackgroundColor, mDefaultBackgroundColor)
        set(value) = mPrefs.edit().putInt(prefBackgroundColor, value).apply()

    var iconColor: Int
        get() = mPrefs.getInt(prefIconColor, mDefaultIconColor)
        set(value) = mPrefs.edit().putInt(prefIconColor, value).apply()

    var isBackgroundAccented: Boolean
        get() = mPrefs.getBoolean(prefIsBackgroundAccented, false)
        set(value) = mPrefs.edit().putBoolean(prefIsBackgroundAccented, value).apply()

    var isIconAccented: Boolean
        get() = mPrefs.getBoolean(prefIsIconAccented, false)
        set(value) = mPrefs.edit().putBoolean(prefIsIconAccented, value).apply()

    var icon: Int
        get() = mPrefs.getInt(prefIcon, R.drawable.android)
        set(value) = mPrefs.edit().putInt(prefIcon, value).apply()

    var theme: Int
        get() = mPrefs.getInt(prefTheme, R.style.AppTheme)
        set(value) = mPrefs.edit().putInt(prefTheme, value).apply()
}