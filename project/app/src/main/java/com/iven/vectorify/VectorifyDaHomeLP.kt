package com.iven.vectorify

import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.iven.vectorify.utils.Utils

class VectorifyDaHomeLP : WallpaperService() {

    private var mBackgroundColor = Color.BLACK
    private var mVectorColor = Color.WHITE
    private var mScaleFactor = 0.35F

    private var mDeviceWidth = 0
    private var mDeviceHeight = 0

    //the vectorify live wallpaper service and engine
    override fun onCreateEngine(): Engine {

        mDeviceWidth = mDeviceMetrics.first
        mDeviceHeight = mDeviceMetrics.second

        updatePaintProps()

        return VectorifyEngine()
    }

    private fun updatePaintProps() {
        //set paints props
        mBackgroundColor = mVectorifyPreferences.backgroundColor
        mVectorColor = mVectorifyPreferences.vectorColor
        mScaleFactor = mVectorifyPreferences.scale
    }

    private inner class VectorifyEngine : WallpaperService.Engine() {

        private val handler = Handler()
        private var sVisible = true
        private val drawRunner = Runnable { draw() }

        override fun onVisibilityChanged(visible: Boolean) {
            sVisible = visible
            if (visible) {
                updatePaintProps()
                handler.post(drawRunner)
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            sVisible = false
            handler.removeCallbacks(drawRunner)
        }

        override fun onDestroy() {
            super.onDestroy()
            sVisible = false
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

                    val vectorDrawable = Utils.tintVectorDrawable(
                        baseContext,
                        mVectorifyPreferences.vector, mBackgroundColor, mVectorColor, false
                    )

                    Utils.drawBitmap(
                        vectorDrawable,
                        canvas,
                        mDeviceWidth,
                        mDeviceHeight,
                        mScaleFactor,
                        mVectorifyPreferences.horizontalOffset,
                        mVectorifyPreferences.verticalOffset
                    )
                }
            } finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas)
            }
            handler.removeCallbacks(drawRunner)
        }
    }
}
