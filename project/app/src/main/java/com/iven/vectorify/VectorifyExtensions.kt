package com.iven.vectorify

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.google.android.material.card.MaterialCardView


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
            setTextColor(vectorColor)
            text = this@toColouredToast

            icon?.let { dw ->
                dw.mutate().setTint(vectorColor)
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
