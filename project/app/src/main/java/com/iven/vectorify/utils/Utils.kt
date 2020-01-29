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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.iven.vectorify.*
import com.pranavpandey.android.dynamic.toasts.DynamicToast

object Utils {

    @JvmStatic
    fun getDefaultNightMode(context: Context) = when (vectorifyPreferences.theme) {
        context.getString(R.string.theme_pref_light) -> AppCompatDelegate.MODE_NIGHT_NO
        context.getString(R.string.theme_pref_dark) -> AppCompatDelegate.MODE_NIGHT_YES
        else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
    }

    @JvmStatic
    fun getProgressiveDefaultNightMode(context: Context) = when (vectorifyPreferences.theme) {
        context.getString(R.string.theme_pref_light) -> context.getString(R.string.theme_pref_dark)
        context.getString(R.string.theme_pref_dark) -> context.getString(R.string.theme_pref_auto)
        else -> context.getString(R.string.theme_pref_light)
    }

    @JvmStatic
    fun getDefaultNightModeIcon(context: Context) = when (vectorifyPreferences.theme) {
        context.getString(R.string.theme_pref_light) -> R.drawable.ic_theme_light
        context.getString(R.string.theme_pref_dark) -> R.drawable.ic_theme_night
        else -> R.drawable.ic_theme_auto
    }

    @JvmStatic
    private fun isThemeNight() =
        AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

    //method to apply dark system bars
    @JvmStatic
    @TargetApi(Build.VERSION_CODES.O_MR1)
    fun handleLightSystemBars(context: Context, window: Window?, view: View, isDialog: Boolean) {

        val isThemeNight = isThemeNight()
        val color = if (!isDialog) ContextCompat.getColor(
            context, R.color.bottom_bar_color
        ) else Color.TRANSPARENT

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            window?.apply {
                statusBarColor = color
                navigationBarColor = color
            }

        }

        view.systemUiVisibility =
            if (isThemeNight) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

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
            e.printStackTrace()
            ContextCompat.getColor(context, R.color.default_accent_color)
        }
    }

    @JvmStatic
    fun drawBitmap(
        drawable: Drawable?,
        canvas: Canvas,
        deviceWidth: Int,
        deviceHeight: Int,
        scale: Float,
        horizontalOffset: Float,
        verticalOffset: Float
    ) {

        val dimension = if (deviceWidth > deviceHeight) deviceHeight else deviceWidth
        val bitmap = Bitmap.createBitmap(
            (dimension * scale).toInt(),
            (dimension * scale).toInt(), Bitmap.Config.ARGB_8888
        )

        val drawableCanvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, drawableCanvas.width, drawableCanvas.height)

        drawable?.draw(drawableCanvas)

        val left = canvas.width / 2F - drawableCanvas.width / 2F + horizontalOffset
        val top = canvas.height / 2F - drawableCanvas.width / 2F + verticalOffset

        canvas.drawBitmap(
            bitmap,
            left,
            top,
            null
        )
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
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            code
        )
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
    fun makeRationaleDialog(activity: Activity, which: Int, shouldRequestRationale: Boolean) {

        val message = if (shouldRequestRationale) R.string.rationale else R.string.rationale_denied

        MaterialDialog(activity).show {

            title(R.string.title_rationale)
            message(message)
            positiveButton(if (shouldRequestRationale) android.R.string.ok else R.string.go_to_info) {
                if (shouldRequestRationale) requestPermissions(activity, which) else
                    openVectorifyDaHomeDetails(activity)
            }
            negativeButton {
                if (shouldRequestRationale)
                    DynamicToast.makeError(
                        context,
                        activity.getString(R.string.boo),
                        Toast.LENGTH_LONG
                    )
                        .show()
                else
                    DynamicToast.makeWarning(
                        context,
                        activity.getString(R.string.boo_info),
                        Toast.LENGTH_LONG
                    )
                        .show()
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
            DynamicToast.make(
                context,
                context.getString(R.string.boo_almost_there),
                Toast.LENGTH_LONG
            )
                .show()
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
    fun getCategory(context: Context, index: Int): Pair<String, List<Int>> {
        return when (index) {
            0 -> Pair(context.getString(R.string.title_tech), VectorsCategories.TECH) //tech
            1 -> Pair(
                context.getString(R.string.title_symbols),
                VectorsCategories.SYMBOLS
            ) //symbols
            2 -> Pair(
                context.getString(R.string.title_animals),
                VectorsCategories.ANIMALS
            ) //animals
            3 -> Pair(
                context.getString(R.string.title_emoticons),
                VectorsCategories.EMOTICONS
            ) //emoticons
            4 -> Pair(context.getString(R.string.title_fun), VectorsCategories.FUN) //fun
            5 -> Pair(context.getString(R.string.title_food), VectorsCategories.FOOD) //food
            6 -> Pair(context.getString(R.string.title_nature), VectorsCategories.NATURE) //nature
            7 -> Pair(
                context.getString(R.string.title_weather),
                VectorsCategories.WEATHER
            ) //weather
            8 -> Pair(context.getString(R.string.title_sport), VectorsCategories.SPORT) //sport
            9 -> Pair(context.getString(R.string.title_math), VectorsCategories.MATH) //math
            10 -> Pair(
                context.getString(R.string.title_science),
                VectorsCategories.SCIENCE
            ) //science
            11 -> Pair(
                context.getString(R.string.title_chernoff),
                VectorsCategories.CHERNOFF
            ) //Chernoff faceS
            12 -> Pair(context.getString(R.string.title_music), VectorsCategories.MUSIC) //music
            13 -> Pair(context.getString(R.string.title_nerdy), VectorsCategories.NERDY) //nerdy
            14 -> Pair(
                context.getString(R.string.title_buildings),
                VectorsCategories.BUILDINGS
            ) //buildings
            15 -> Pair(context.getString(R.string.title_alert), VectorsCategories.ALERT) //alert
            16 -> Pair(
                context.getString(R.string.title_alpha),
                VectorsCategories.ALPHABET
            ) //letters
            17 -> Pair(context.getString(R.string.title_roman), VectorsCategories.ROMAN) //roman
            18 -> Pair(context.getString(R.string.title_zodiac), VectorsCategories.ZODIAC) //zodiac
            else -> Pair(context.getString(R.string.title_others), VectorsCategories.OTHERS)
        }
    }

    @JvmStatic
    fun getDefaultVectorForApi(): Int {
        return when (Build.VERSION.SDK_INT) {
            Build.VERSION_CODES.LOLLIPOP -> R.drawable.l
            Build.VERSION_CODES.LOLLIPOP_MR1 -> R.drawable.l
            Build.VERSION_CODES.M -> R.drawable.m_original
            Build.VERSION_CODES.N -> R.drawable.n_original
            Build.VERSION_CODES.N_MR1 -> R.drawable.n_original
            Build.VERSION_CODES.O -> R.drawable.o_original
            Build.VERSION_CODES.O_MR1 -> R.drawable.o_original
            Build.VERSION_CODES.P -> R.drawable.p
            else -> R.drawable.q
        }
    }

    @JvmStatic
    fun getVectorProps(vector: Int): Pair<Int, Boolean> {

        var isSpecial = false
        var returnedVector = vector
        when (vector) {

            R.drawable.m_original -> {
                returnedVector = R.drawable.m
                isSpecial = true
            }

            R.drawable.n_original -> {
                returnedVector = R.drawable.n
                isSpecial = true
            }

            R.drawable.o_original -> {
                returnedVector = R.drawable.o
                isSpecial = true
            }
        }
        return Pair(returnedVector, isSpecial)
    }

    @JvmStatic
    fun tintDrawable(
        context: Context,
        vector: Int,
        backgroundColor: Int,
        vectorColor: Int,
        showErrorDialog: Boolean
    ): Drawable? {

        //determine if we are facing android m, n, o vectors
        //so we can apply multiply tint mode to drawable
        var vectorProps = getVectorProps(vector)

        val bit = try {
            AppCompatResources.getDrawable(context, vectorProps.first)
        } catch (e: Exception) {
            e.printStackTrace()
            if (showErrorDialog && vectorifyPreferences.hasToShowError) makeErrorDialog(context)
            vectorProps = getVectorProps(getDefaultVectorForApi())
            AppCompatResources.getDrawable(context, vectorProps.first)
        }

        if (bit != null) {
            try {
                //to avoid shared drawables get tinted too!
                bit.mutate()

                //set tint mode multiply for special vectors
                if (vectorProps.second) bit.setTintMode(PorterDuff.Mode.MULTIPLY)

                //darken or lighten color to increase vector visibility when the colors are the same
                val finalVectorColor = if (checkIfColorsEqual(backgroundColor, vectorColor)) {
                    if (vectorColor.isDark())
                        vectorColor.lighten(
                            0.20F
                        )
                    else vectorColor.darken(0.20F)
                } else {
                    vectorColor
                }

                bit.setTint(finalVectorColor)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return bit
    }

    //make rationale permission dialog
    @JvmStatic
    private fun makeErrorDialog(context: Context) {

        MaterialDialog(context).show {
            title(R.string.title_info_error)
            message(R.string.info_error)
            positiveButton(R.string.info_error_ok) {
                vectorifyPreferences.hasToShowError = false
            }
        }
    }

    //clear recent setups
    @JvmStatic
    fun clearRecentSetups(context: Context) {

        MaterialDialog(context).show {

            title(res = R.string.title_recent_setups)
            message(R.string.message_clear_recent_setups)
            positiveButton {
                //add an empty list to preferences
                vectorifyPreferences.vectorifyWallpaperSetups = null
            }
            negativeButton { dismiss() }
        }
    }

    @JvmStatic
    fun openGitHubPage(context: Context) {
        //intent to open git link
        val openGitHubPageIntent = Intent(Intent.ACTION_VIEW)
        openGitHubPageIntent.data = Uri.parse(context.getString(R.string.app_github_link))

        //check if a browser is present
        if (openGitHubPageIntent.resolveActivity(context.packageManager) != null) context.startActivity(
            openGitHubPageIntent
        ) else
            DynamicToast.makeError(
                context,
                context.getString(R.string.install_browser_message),
                Toast.LENGTH_LONG
            )
                .show()
    }
}
