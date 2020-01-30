package com.iven.vectorify

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.google.android.material.card.MaterialCardView
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

@SuppressLint("DefaultLocale")
fun Int.toHex(context: Context) =
    context.getString(R.string.hex, Integer.toHexString(this)).toUpperCase()

//method to determine colors luminance
fun Int.isDark() = ColorUtils.calculateLuminance(this) < 0.35

//method to calculate colors for cards titles
fun Int.toSurfaceColor() = if (isDark()) Color.WHITE else Color.BLACK

fun Int.darkenOrLighten() =
    ColorUtils.blendARGB(this, if (isDark()) Color.WHITE else Color.BLACK, 0.20F)

//method to get rounded float string
fun Float.toDecimalFormat() = try {
    String.format("%.2f", this)
} catch (e: Exception) {
    e.printStackTrace()
    ""
}

fun Int.toContrastColor(compareColor: Int) = if (this == compareColor)
    darkenOrLighten() else this

fun String.toColouredToast(
    context: Context,
    icon: Drawable?,
    backgroundColor: Int,
    vectorColor: Int
) {
    Toast.makeText(context, this, Toast.LENGTH_LONG).apply {

        val toastView = View.inflate(context, R.layout.custom_toast, null) as MaterialCardView

        toastView.setCardBackgroundColor(ColorStateList.valueOf(backgroundColor))
        toastView.findViewById<TextView>(R.id.toast_text).apply {

            val contentColor = vectorColor.toContrastColor(backgroundColor)

            setTextColor(contentColor)
            text = this@toColouredToast

            icon?.let { dw ->
                dw.mutate().setTint(contentColor)
                setCompoundDrawablesRelativeWithIntrinsicBounds(dw, null, null, null)
            }
        }

        view = toastView

    }.show()
}

fun String.toErrorToast(context: Context) {
    val info = ContextCompat.getDrawable(context, R.drawable.ic_info)
    val errorColor = ContextCompat.getColor(context, R.color.red)
    toColouredToast(context, info, errorColor, errorColor.toSurfaceColor())
}

fun List<ImageButton>.applyTint(context: Context, widgetColor: Int) {
    forEach { imageButton ->
        imageButton.drawable.mutate().setTint(widgetColor)
        imageButton.background = Utils.createColouredRipple(context, widgetColor)
    }
}

fun MutableList<VectorifyWallpaper>?.getLatestSetup() =
    if (!this.isNullOrEmpty()) get(size - 1) else vectorifyPreferences.vectorifyWallpaperBackup
