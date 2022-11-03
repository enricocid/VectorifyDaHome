package com.iven.vectorify

import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.preference.PreferenceManager
import com.iven.vectorify.utils.Utils

class LiveWallpaper: WallpaperService() {

    private var mBackgroundColor = Color.BLACK
    private var mVectorColor = Color.WHITE
    private var mVector = R.drawable.android_logo_2019
    private var mScale = 0.35F
    private var mHorizontalOffSet = 0F
    private var mVerticalOffSet = 0F

    private val mVectorifyPreferences get() = VectorifyPreferences.getPrefsInstance()

    //the vectorify live wallpaper service and engine
    override fun onCreateEngine(): Engine = VectorifyEngine()

    private fun updatePaintProps() {
        with(mVectorifyPreferences.liveWallpaper) {
            mBackgroundColor = backgroundColor
            mVectorColor = vectorColor.toContrastColor(mBackgroundColor)
            mVector = resource
            mScale = scale
            mHorizontalOffSet = horizontalOffset
            mVerticalOffSet = verticalOffset
        }
    }

    private inner class VectorifyEngine: WallpaperService.Engine(), SharedPreferences.OnSharedPreferenceChangeListener {

        private val handler = Handler(Looper.getMainLooper())
        private val drawRunner = Runnable { draw() }
        private var sDrawn = false

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (key == getString(R.string.live_wallpaper_key) && sDrawn) {
                updatePaintProps()
                handler.post(drawRunner)
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            PreferenceManager.getDefaultSharedPreferences(baseContext)
                .registerOnSharedPreferenceChangeListener(this)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible && !sDrawn) {
                updatePaintProps()
                handler.post(drawRunner)
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
            PreferenceManager.getDefaultSharedPreferences(baseContext)
                .unregisterOnSharedPreferenceChangeListener(this)
        }

        //draw potato according to battery level
        fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                //draw wallpaper
                canvas = holder.lockCanvas()
                if (canvas != null && baseContext != null) {

                    //draw background!
                    canvas.drawColor(mBackgroundColor)

                    val drawable = Utils.tintDrawable(baseContext, mVector, mVectorColor)

                    val metrics = mVectorifyPreferences.savedMetrics

                    // draw vector!
                    Utils.drawBitmap(
                        drawable,
                        canvas,
                        metrics.width,
                        metrics.height,
                        mScale,
                        mHorizontalOffSet,
                        mVerticalOffSet
                    )
                }
            } finally {
                canvas?.let { cv ->
                    holder.unlockCanvasAndPost(cv)
                    if (!sDrawn) sDrawn = true
                }
            }
            handler.removeCallbacks(drawRunner)
        }
    }

    companion object {
        const val BACKGROUND_COLOR = "BACKGROUND_COLOR"
        const val VECTOR_COLOR = "VECTOR_COLOR"
        const val VECTOR = "VECTOR"
        const val CATEGORY = "CATEGORY"
        const val SCALE = "SCALE"
        const val H_OFFSET = "H_OFFSET"
        const val V_OFFSET = "V_OFFSET"
    }
}
