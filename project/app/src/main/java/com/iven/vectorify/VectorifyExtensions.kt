package com.iven.vectorify

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.core.graphics.ColorUtils
import com.iven.vectorify.models.VectorifyWallpaper
import com.iven.vectorify.utils.SingleClickHelper
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

fun VectorifyWallpaper.addToRecentSetups(isLand: Boolean) {

    val recentSetupsToUpdate = if (isLand) {
        vectorifyPreferences.recentSetupsLand
    } else {
        vectorifyPreferences.recentSetups
    }

    //update recent setups
    val recentSetups =
        if (!recentSetupsToUpdate.isNullOrEmpty()) {
            recentSetupsToUpdate
        } else {
            mutableListOf()
        }

    if (!recentSetups.contains(this)) {
        recentSetups.add(this)
    }

    if (isLand) {
        vectorifyPreferences.recentSetupsLand = recentSetups
    } else {
        vectorifyPreferences.recentSetups = recentSetups
    }
}

@SuppressLint("DefaultLocale")
fun Int.toHex(context: Context) =
    context.getString(R.string.hex, Integer.toHexString(this)).uppercase()

//method to determine colors luminance
private fun Int.isDark() = ColorUtils.calculateLuminance(this) < 0.35

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

fun Int.toContrastColor(compareColor: Int) = if (this == compareColor) {
    darkenOrLighten()
} else {
    this
}

fun Float.toFormattedScale() = String.format("%.2f", this)

fun List<ImageView>.applyTint(context: Context, widgetColor: Int) {
    val iterator = iterator()
    while (iterator.hasNext()) {
        iterator.next().run {
            drawable.mutate().setTint(widgetColor)
            background = Utils.createColouredRipple(context, widgetColor)
        }
    }
}

fun View.safeClickListener(safeClickListener: (view: View) -> Unit) {
    this.setOnClickListener {
        if (!SingleClickHelper.isBlockingClick()) {
            safeClickListener(it)
        }
    }
}
