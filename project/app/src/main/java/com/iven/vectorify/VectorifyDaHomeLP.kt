package com.iven.vectorify

import android.graphics.Canvas
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.iven.vectorify.utils.Utils

class VectorifyDaHomeLP : WallpaperService() {

    private val mBackupRecent = vectorifyPreferences.vectorifyWallpaperBackup

    private var mSelectedBackgroundColor = mBackupRecent.backgroundColor
    private var mSelectedVectorColor = mBackupRecent.vectorColor
    private var mSelectedScaleFactor = mBackupRecent.scale

    private var mDeviceWidth = 0
    private var mDeviceHeight = 0

    private var mLatestSetup: VectorifyWallpaper? = null

    //the vectorify live wallpaper service and engine
    override fun onCreateEngine(): Engine {

        mDeviceWidth = deviceMetrics.first
        mDeviceHeight = deviceMetrics.second

        updatePaintProps()

        return VectorifyEngine()
    }

    private fun updatePaintProps() {

        mLatestSetup = vectorifyPreferences.savedVectorifyWallpaper

        //set paints props
        mLatestSetup?.let { recent ->
            mSelectedBackgroundColor = recent.backgroundColor
            mSelectedVectorColor = recent.vectorColor
            mSelectedScaleFactor = recent.scale
        }
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
                    canvas.drawColor(mSelectedBackgroundColor)

                    val drawable = Utils.tintDrawable(
                        baseContext,
                        mLatestSetup?.resource!!,
                        mSelectedBackgroundColor,
                        mSelectedVectorColor,
                        false
                    )

                    Utils.drawBitmap(
                        drawable,
                        canvas,
                        mDeviceWidth,
                        mDeviceHeight,
                        mSelectedScaleFactor,
                        mLatestSetup?.horizontalOffset!!,
                        mLatestSetup?.verticalOffset!!
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
