package com.iven.vectorify

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.iven.vectorify.models.VectorifyWallpaper
import com.iven.vectorify.utils.SingleClickHelper
import com.iven.vectorify.utils.Utils
import kotlin.math.roundToInt


fun VectorifyWallpaper.addToRecentSetups() {

    val prefs = VectorifyPreferences.getPrefsInstance()
    var toUpdate = prefs.recentSetups

    if (toUpdate.isNullOrEmpty()) toUpdate = mutableListOf()

    //update recent setups
    if (!toUpdate.contains(this)) toUpdate.add(this)

    prefs.recentSetups = toUpdate
}

fun Int.toHex() = String.format("#FF%06X", 0xFFFFFF and this)

//method to calculate ui elements color according to main color luminance
@ColorInt
fun Int.toSurfaceColor(): Int {
    if (ColorUtils.calculateLuminance(this) < 0.35) return Color.WHITE
    return Color.BLACK
}

@ColorInt
fun Int.darkenOrLighten() = ColorUtils.blendARGB(this, toSurfaceColor(), 0.20F)

@ColorInt
fun Int.toContrastColor(compareColor: Int): Int {
    if (this == compareColor) return darkenOrLighten()
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
        if (!SingleClickHelper.isBlockingClick()) safeClickListener(it)
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

@SuppressLint("RestrictedApi", "VisibleForTests")
fun Dialog?.disableShapeAnimation() {
    try {
        val bottomSheetDialog = this as BottomSheetDialog
        bottomSheetDialog.behavior.disableShapeAnimations()
    } catch (ex: Exception) {
        Log.e("BaseBottomSheet", "disableShapeAnimation Exception:", ex)
    }
}

fun ViewPager2.reduceDragSensitivity() {

    // By default, ViewPager2's sensitivity is high enough to result in vertical
    // scroll events being registered as horizontal scroll events. Reflect into the
    // internal recyclerview and change the touch slope so that touch actions will
    // act more as a scroll than as a swipe.
    try {

        val recycler = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recycler.isAccessible = true
        val recyclerView = recycler.get(this) as RecyclerView

        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop*3) // 3x seems to be the best fit here

    } catch (e: Exception) {
        Log.e("MainActivity", "Unable to reduce ViewPager sensitivity")
        Log.e("MainActivity", e.stackTraceToString())
    }
}

fun Float.toSliderValue() = roundToInt().toString().padStart(3, '0')
