package com.iven.vectorify

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.material.bottomsheet.BottomSheetBehavior
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

    var toUpdate = vectorifyPreferences.recentSetups
    if (isLand) {
        toUpdate = vectorifyPreferences.recentSetupsLand
    }

    if (toUpdate.isNullOrEmpty()) {
        toUpdate = mutableListOf()
    }
    //update recent setups
    if (!toUpdate.contains(this)) {
        toUpdate.add(this)
    }

    if (isLand) {
        vectorifyPreferences.recentSetupsLand = toUpdate
    } else {
        vectorifyPreferences.recentSetups = toUpdate
    }
}

@SuppressLint("DefaultLocale")
fun Int.toHex(context: Context) =
    context.getString(R.string.hex, Integer.toHexString(this)).uppercase()

//method to calculate ui elements color according to main color luminance
@ColorInt
fun Int.toSurfaceColor(): Int {
    if (ColorUtils.calculateLuminance(this) < 0.35) {
        return Color.WHITE
    }
    return Color.BLACK
}

@ColorInt
fun Int.darkenOrLighten() = ColorUtils.blendARGB(this, toSurfaceColor(), 0.20F)

@ColorInt
fun Int.toContrastColor(compareColor: Int): Int {
    if (this == compareColor) {
        return darkenOrLighten()
    }
    return this
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
    setOnClickListener {
        if (!SingleClickHelper.isBlockingClick()) {
            safeClickListener(it)
        }
    }
}

fun Dialog?.applyFullHeightDialog(activity: Activity) {
    // to ensure full dialog's height

    val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity)
    val height = windowMetrics.bounds.height()

    this?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let { bs ->
        BottomSheetBehavior.from(bs).peekHeight = height
    }
}
