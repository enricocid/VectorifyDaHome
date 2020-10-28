package com.iven.vectorify

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageButton
import androidx.core.graphics.ColorUtils
import com.iven.vectorify.utils.Utils


//viewTreeObserver extension to measure layout params
//https://antonioleiva.com/kotlin-ongloballayoutlistener/
inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}

fun VectorifyWallpaper.addToRecentSetups() {
    //update recent setups
    val recentSetups =
            if (vectorifyPreferences.vectorifyWallpaperSetups != null) {
                vectorifyPreferences.vectorifyWallpaperSetups
            } else {
                mutableListOf()
            }

    if (!recentSetups?.contains(this)!!) {
        recentSetups.add(this)
    }
    vectorifyPreferences.vectorifyWallpaperSetups = recentSetups
}

@SuppressLint("DefaultLocale")
fun Int.toHex(context: Context) =
        context.getString(R.string.hex, Integer.toHexString(this)).toUpperCase()

//method to determine colors luminance
fun Int.isDark() = ColorUtils.calculateLuminance(this) < 0.35

//method to calculate colors for cards titles
fun Int.toSurfaceColor() = if (isDark()) {
    Color.WHITE
} else {
    Color.BLACK
}

fun Int.darkenOrLighten(): Int {
    val mask = if (isDark()) {
        Color.WHITE
    } else {
        Color.BLACK
    }
    return ColorUtils.blendARGB(this, mask, 0.20F)
}

//method to get rounded float string
fun Float.toDecimalFormat() = try {
    String.format("%.2f", this)
} catch (e: Exception) {
    e.printStackTrace()
    ""
}

fun Int.toContrastColor(compareColor: Int) = if (this == compareColor) {
    darkenOrLighten()
} else {
    this
}

fun List<ImageButton>.applyTint(context: Context, widgetColor: Int) {
    forEach { imageButton ->
        imageButton.drawable.mutate().setTint(widgetColor)
        imageButton.background = Utils.createColouredRipple(context, widgetColor)
    }
}
