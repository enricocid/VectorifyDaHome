@file:JvmName("Utils")

package com.iven.vectorify.ui

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.iven.vectorify.R
import com.iven.vectorify.VectorifyDaHomeLP

object Utils {

    //method to open live wallpaper intent
    @JvmStatic
    fun openLiveWallpaperIntent(context: Context) {
        val intent = Intent(
            WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
        )
        intent.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(context, VectorifyDaHomeLP::class.java)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        context.startActivity(intent)
    }

    //get system accent color
    @JvmStatic
    fun getSystemAccentColor(context: Context): Int {
        return try {
            ContextCompat.getColor(
                context,
                context.resources.getIdentifier("accent_device_default_dark", "color", "android")
            )
        } catch (e: Exception) {
            ContextCompat.getColor(context, R.color.default_accent_color)
        }
    }

    //method to calculate colors for cards titles
    @JvmStatic
    fun getSecondaryColor(color: Int): Int {
        return if (isColorDark(color)) Color.WHITE else Color.BLACK
    }

    //method to determine colors luminance
    fun isColorDark(color: Int): Boolean {
        return ColorUtils.calculateLuminance(color) < 0.35
    }

    @JvmStatic
    fun darkenColor(color: Int, factor: Float): Int {
        return ColorUtils.blendARGB(color, Color.BLACK, factor)
    }

    @JvmStatic
    fun lightenColor(color: Int, factor: Float): Int {
        return ColorUtils.blendARGB(color, Color.WHITE, factor)
    }
}