@file:JvmName("Utils")

package com.iven.vectorify.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.iven.vectorify.R
import com.iven.vectorify.VectorifyDaHomeLP
import com.iven.vectorify.adapters.VectorsAdapter

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
    fun drawBitmap(
        vectorDrawable: VectorDrawable,
        canvas: Canvas,
        deviceWidth: Int,
        deviceHeight: Int,
        scale: Float,
        horizontalOffset: Float,
        verticalOffset: Float
    ): Float {

        val dimension = if (deviceWidth > deviceHeight) deviceHeight else deviceWidth
        val bitmap = Bitmap.createBitmap(
            (dimension * scale).toInt(),
            (dimension * scale).toInt(), Bitmap.Config.ARGB_8888
        )

        val drawableCanvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, drawableCanvas.width, drawableCanvas.height)

        vectorDrawable.draw(drawableCanvas)

        val left = canvas.width / 2F - drawableCanvas.width / 2F + horizontalOffset
        val top = canvas.height / 2F - drawableCanvas.width / 2F + verticalOffset

        canvas.drawBitmap(
            bitmap,
            left,
            top,
            null
        )
        return drawableCanvas.width * 0.10F
    }

    @JvmStatic
    fun hasToRequestWriteStoragePermission(activity: Activity): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    }

    @TargetApi(Build.VERSION_CODES.M)
    @JvmStatic
    fun requestPermissions(activity: Activity, code: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), code)
    }

    //method to get rounded float string
    @JvmStatic
    fun getDecimalFormattedString(number: Float): String {
        var formattedNumber = ""
        try {
            formattedNumber = String.format("%.2f", number)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return formattedNumber
    }

    //determine if the live wallpaper is already applied
    @JvmStatic
    fun isLiveWallpaperRunning(context: Context): Boolean {
        val wpm = WallpaperManager.getInstance(context)
        val info = wpm.wallpaperInfo
        return info != null && info.packageName == context.packageName
    }

    //make rationale permission dialog
    @JvmStatic
    @Suppress("DEPRECATION")
    fun makeRationaleDialog(activity: Activity, which: Int, shouldRequestRationale: Boolean) {

        val message = if (shouldRequestRationale) R.string.rationale else R.string.rationale_denied

        MaterialDialog(activity).show {

            cornerRadius(res = R.dimen.md_corner_radius)
            title(R.string.title_rationale)
            message(message)
            positiveButton {
                if (shouldRequestRationale) requestPermissions(activity, which)
            }
            negativeButton {
                Toast.makeText(activity, activity.getString(R.string.boo), Toast.LENGTH_LONG)
                    .show()
                dismiss()
            }
            if (!shouldRequestRationale)
                neutralButton(R.string.go_to_info) {
                    openVectorifyDaHomeDetails(activity)
                }
        }
    }

    //make rationale permission dialog
    @JvmStatic
    private fun openVectorifyDaHomeDetails(context: Context) {
        try {
            //Open the specific App Info page:
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + context.packageName)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    //check if two colors are the same
    @JvmStatic
    fun checkIfColorsEqual(color1: Int, color2: Int): Boolean {
        return color1 == color2
    }

    //get categories start position
    @JvmStatic
    fun getCategoryStartPosition(index: Int): Int {
        return when (index) {
            0 -> R.drawable.android
            1 -> R.drawable.dot
            2 -> R.drawable.cat
            3 -> R.drawable.face
            4 -> R.drawable.toys
            5 -> R.drawable.ice_pop
            6 -> R.drawable.nature
            7 -> R.drawable.pi
            8 -> R.drawable.periodic_table
            9 -> R.drawable.music_note
            10 -> R.drawable.space_invaders
            11 -> R.drawable.factory
            12 -> R.drawable.high
            13 -> R.drawable.alpha_a
            14 -> R.drawable.zodiac_aries
            else -> R.drawable.school
        }
    }

    //get categories labels while scrolling recycler view
    @JvmStatic
    fun getCategoryForPosition(
        resources: Resources,
        layoutManager: LinearLayoutManager,
        vectorsAdapter: VectorsAdapter
    ): String {
        return when (layoutManager.findFirstCompletelyVisibleItemPosition()) {
            in vectorsAdapter.getVectorPosition(R.drawable.android)..vectorsAdapter.getVectorPosition(R.drawable.map_marker_check)
            -> resources.getString(R.string.title_tech)

            in vectorsAdapter.getVectorPosition(R.drawable.dot)..vectorsAdapter.getVectorPosition(R.drawable.sticker)
            -> resources.getString(R.string.title_symbols)

            in vectorsAdapter.getVectorPosition(R.drawable.cat)..vectorsAdapter.getVectorPosition(R.drawable.jellyfish)
            -> resources.getString(R.string.title_animals)

            in vectorsAdapter.getVectorPosition(R.drawable.face)..vectorsAdapter.getVectorPosition(R.drawable.alien)
            -> resources.getString(R.string.title_emoticons)

            in vectorsAdapter.getVectorPosition(R.drawable.toys)..vectorsAdapter.getVectorPosition(R.drawable.balloon)
            -> resources.getString(R.string.title_fun)

            in vectorsAdapter.getVectorPosition(R.drawable.ice_pop)..vectorsAdapter.getVectorPosition(R.drawable.carrot)
            -> resources.getString(R.string.title_food)

            in vectorsAdapter.getVectorPosition(R.drawable.nature)..vectorsAdapter.getVectorPosition(R.drawable.cloud_outline)
            -> resources.getString(R.string.title_nature)

            in vectorsAdapter.getVectorPosition(R.drawable.pi)..vectorsAdapter.getVectorPosition(R.drawable.infinity)
            -> resources.getString(R.string.title_math)

            in vectorsAdapter.getVectorPosition(R.drawable.periodic_table)..vectorsAdapter.getVectorPosition(R.drawable.chart_bell_curve)
            -> resources.getString(R.string.title_science)

            in vectorsAdapter.getVectorPosition(R.drawable.music_note)..vectorsAdapter.getVectorPosition(R.drawable.saxophone)
            -> resources.getString(R.string.title_music)

            in vectorsAdapter.getVectorPosition(R.drawable.space_invaders)..vectorsAdapter.getVectorPosition(R.drawable.ocarina)
            -> resources.getString(R.string.title_nerdy)

            in vectorsAdapter.getVectorPosition(R.drawable.factory)..vectorsAdapter.getVectorPosition(R.drawable.stadium)
            -> resources.getString(R.string.title_buildings)

            in vectorsAdapter.getVectorPosition(R.drawable.high)..vectorsAdapter.getVectorPosition(R.drawable.block)
            -> resources.getString(R.string.title_alert)

            in vectorsAdapter.getVectorPosition(R.drawable.alpha_a)..vectorsAdapter.getVectorPosition(R.drawable.alpha_z)
            -> resources.getString(R.string.title_zodiac)

            in vectorsAdapter.getVectorPosition(R.drawable.zodiac_aries)..vectorsAdapter.getVectorPosition(R.drawable.zodiac_virgo)
            -> resources.getString(R.string.title_zodiac)

            else -> resources.getString(R.string.title_others)
        }
    }
}
