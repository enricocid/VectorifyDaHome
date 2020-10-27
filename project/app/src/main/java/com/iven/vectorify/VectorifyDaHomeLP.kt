package com.iven.vectorify

import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.iven.vectorify.utils.Utils

class VectorifyDaHomeLP : WallpaperService() {

    private val mBackupRecent = vectorifyPreferences.vectorifyWallpaperBackup

    private var mSelectedBackgroundColor = mBackupRecent.backgroundColor
    private var mSelectedVectorColor = mBackupRecent.vectorColor
    private var mSelectedVector = mBackupRecent.resource
    private var mSelectedScaleFactor = mBackupRecent.scale
    private var mHorizontalOffSet = mBackupRecent.horizontalOffset
    private var mVerticalOffSet = mBackupRecent.verticalOffset

    private val mVectorifyMetrics get() = vectorifyPreferences.vectorifyMetrics
    private var mDeviceWidth = mVectorifyMetrics!!.first
    private var mDeviceHeight = mVectorifyMetrics!!.second

    //the vectorify live wallpaper service and engine
    override fun onCreateEngine(): Engine {

        updatePaintProps()

        return VectorifyEngine()
    }

    private fun updatePaintProps() {

        val selectedWallpaper = vectorifyPreferences.liveVectorifyWallpaper

        //set paints props
        selectedWallpaper?.let { recent ->
            mSelectedBackgroundColor = recent.backgroundColor
            mSelectedVectorColor = recent.vectorColor.toContrastColor(mSelectedBackgroundColor)
            mSelectedVector = recent.resource
            mSelectedScaleFactor = recent.scale
            mHorizontalOffSet = recent.horizontalOffset
            mVerticalOffSet = recent.verticalOffset
        }
    }

    private inner class VectorifyEngine : WallpaperService.Engine() {

        private val handler = Handler(Looper.getMainLooper())
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
                            mSelectedVector,
                            mSelectedVectorColor
                    )

                    Utils.drawBitmap(
                            drawable,
                            canvas,
                            mDeviceWidth,
                            mDeviceHeight,
                            mSelectedScaleFactor,
                            mHorizontalOffSet,
                            mVerticalOffSet
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
