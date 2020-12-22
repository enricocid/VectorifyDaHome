@file:JvmName("Utils")

package com.iven.vectorify.utils

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.afollestad.materialdialogs.MaterialDialog
import com.iven.vectorify.R
import com.iven.vectorify.VectorifyDaHomeLP
import com.iven.vectorify.vectorifyPreferences


object Utils {

    @JvmStatic
    fun getDefaultNightMode(context: Context) = when (vectorifyPreferences.theme) {
        context.getString(R.string.theme_pref_light) -> AppCompatDelegate.MODE_NIGHT_NO
        context.getString(R.string.theme_pref_dark) -> AppCompatDelegate.MODE_NIGHT_YES
        else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        } else {
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
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

        val dimension = if (deviceWidth > deviceHeight) {
            deviceHeight
        } else {
            deviceWidth
        }
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

    //determine if the live wallpaper is already applied
    @JvmStatic
    fun isLiveWallpaperRunning(context: Context): Boolean {
        val wpm = WallpaperManager.getInstance(context)
        val info = wpm.wallpaperInfo
        return info != null && info.packageName == context.packageName
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
        vectorColor: Int
    ): Drawable? {

        //determine if we are facing android m, n, o vectors
        //so we can apply multiply tint mode to drawable
        var vectorProps = getVectorProps(vector)

        val bit = try {
            context.getDrawable(vectorProps.first)
        } catch (e: Exception) {
            e.printStackTrace()
            vectorProps = getVectorProps(getDefaultVectorForApi())
            context.getDrawable(vectorProps.first)
        }

        if (bit != null) {
            try {
                //to avoid shared drawables get tinted too!
                bit.mutate()

                //set tint mode multiply for special vectors
                if (vectorProps.second) {
                    bit.setTintMode(PorterDuff.Mode.MULTIPLY)
                }
                bit.setTint(vectorColor)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return bit
    }

    //clear recent setups
    @JvmStatic
    fun clearRecentSetups(context: Context) {

        MaterialDialog(context).show {

            title(res = R.string.title_recent_setups)
            message(R.string.message_clear_recent_setups)
            positiveButton {
                //add an empty list to preferences
                vectorifyPreferences.vectorifyWallpaperSetups = mutableListOf()
            }
            negativeButton { dismiss() }
        }
    }

    @JvmStatic
    fun openCustomTab(
        context: Context
    ) {
        try {
            CustomTabsIntent.Builder().apply {
                setShareState(CustomTabsIntent.SHARE_STATE_ON)
                setShowTitle(true)
                val link = context.getString(R.string.app_github_link)
                build().launchUrl(context, link.toUri())
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.install_browser_message),
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun createColouredRipple(context: Context, rippleColor: Int): Drawable {
        val ripple = context.getDrawable(R.drawable.ripple) as RippleDrawable
        ripple.setColor(ColorStateList.valueOf(rippleColor))
        return ripple
    }
}
