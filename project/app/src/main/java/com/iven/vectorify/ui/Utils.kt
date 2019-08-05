@file:JvmName("Utils")

package com.iven.vectorify.ui

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.VectorDrawable
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.iven.vectorify.R
import com.iven.vectorify.VectorifyDaHomeLP
import java.math.RoundingMode
import java.text.DecimalFormat

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

    @JvmStatic
    fun drawBitmap(vectorDrawable: VectorDrawable, canvas: Canvas, deviceWidth: Int, deviceHeight: Int, scale: Float) {

        val dimension = if (deviceWidth > deviceHeight) deviceHeight else deviceWidth
        val bitmap = Bitmap.createBitmap(
            (dimension * scale).toInt(),
            (dimension * scale).toInt(), Bitmap.Config.ARGB_8888
        )

        val drawableCanvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, drawableCanvas.width, drawableCanvas.height)

        vectorDrawable.draw(drawableCanvas)

        canvas.drawBitmap(
            bitmap,
            canvas.width / 2F - drawableCanvas.width / 2F,
            canvas.height / 2F - drawableCanvas.width / 2F,
            null
        )
    }

    @JvmStatic
    fun hasToRequestWriteStoragePermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
    }

    @TargetApi(Build.VERSION_CODES.M)
    @JvmStatic
    fun checkPermissions(activity: Activity, code: Int) {
        activity.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), code)
    }

    @JvmStatic
    fun getDecimalFormat(number: Float): Float {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        return df.format(number).toFloat()
    }
}