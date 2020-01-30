package com.iven.vectorify

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.graphics.ColorUtils

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

@SuppressLint("DefaultLocale")
fun Int.toHex(context: Context) =
    context.getString(R.string.hex, Integer.toHexString(this)).toUpperCase()

//method to determine colors luminance
fun Int.isDark() = ColorUtils.calculateLuminance(this) < 0.35

//method to calculate colors for cards titles
fun Int.toSurfaceColor() = if (isDark()) Color.WHITE else Color.BLACK

fun Int.lighten(factor: Float) = ColorUtils.blendARGB(this, Color.WHITE, factor)

fun Int.darken(factor: Float) = ColorUtils.blendARGB(this, Color.BLACK, factor)

//method to get rounded float string
fun Float.toDecimalFormat() = try {
    String.format("%.2f", this)
} catch (e: Exception) {
    e.printStackTrace()
    ""
}

fun String.toToast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_LONG)
        .show()
}
