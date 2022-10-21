package com.iven.vectorify

import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.iven.vectorify.utils.Utils

class LiveWallpaper : WallpaperService() {

    private var mBackgroundColor = Color.BLACK
    private var mVectorColor = Color.WHITE
    private var mVector = R.drawable.android_logo_2019
    private var mScale = 0.35F
    private var mHorizontalOffSet = 0F
    private var mVerticalOffSet = 0F

    //the vectorify live wallpaper service and engine
    override fun onCreateEngine(): Engine {

        updatePaintProps()

        return VectorifyEngine()
    }

    private fun updatePaintProps() {

        with(vectorifyPreferences.liveWallpaper) {
            mBackgroundColor = backgroundColor
            mVectorColor = vectorColor.toContrastColor(mBackgroundColor)
            mVector = resource
            mScale = scale
            mHorizontalOffSet = horizontalOffset
            mVerticalOffSet = verticalOffset
        }
    }

    private inner class VectorifyEngine : WallpaperService.Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private val drawRunner = Runnable { draw() }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                updatePaintProps()
                handler.post(drawRunner)
                return
            }
            handler.removeCallbacks(drawRunner)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(drawRunner)
        }

        override fun onDestroy() {
            super.onDestroy()
            handler.removeCallbacks(drawRunner)
        }

        //draw potato according to battery level
        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                //draw wallpaper
                canvas = holder.lockCanvas()
                if (canvas != null && baseContext != null) {

                    //draw potato!
                    canvas.drawColor(mBackgroundColor)

                    val drawable = Utils.tintDrawable(
                        baseContext,
                        mVector,
                        mVectorColor
                    )

                    Utils.drawBitmap(
                        drawable,
                        canvas,
                        vectorifyPreferences.savedMetrics.width,
                        vectorifyPreferences.savedMetrics.height,
                        mScale,
                        mHorizontalOffSet,
                        mVerticalOffSet
                    )
                }
            } finally {
                canvas?.let { cv ->
                    holder.unlockCanvasAndPost(cv)
                }
            }
            handler.removeCallbacks(drawRunner)
        }
    }
}
