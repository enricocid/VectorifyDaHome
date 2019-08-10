package com.iven.vectorify

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.VectorDrawable
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat
import com.iven.vectorify.ui.Utils

class VectorifyDaHomeLP : WallpaperService() {

    private var mBackgroundColor = Color.BLACK
    private var mDrawableColor = Color.WHITE
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
        mDrawableColor = mVectorifyPreferences.vectorColor
        mScaleFactor = mVectorifyPreferences.scale
    }

    private fun checkSystemAccent() {

        val isBackgroundAccented = mVectorifyPreferences.isBackgroundAccented
        val isDrawableAccented = mVectorifyPreferences.isVectorAccented

        if (isBackgroundAccented || isDrawableAccented) {
            //change only if system accent has changed
            val systemAccentColor = Utils.getSystemAccentColor(this)
            if (isBackgroundAccented && mBackgroundColor != systemAccentColor) mBackgroundColor =
                systemAccentColor
            if (isDrawableAccented && mDrawableColor != systemAccentColor) mDrawableColor = systemAccentColor
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
                checkSystemAccent()
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
                    val bit = ContextCompat.getDrawable(baseContext, mVectorifyPreferences.vector) as VectorDrawable
                    bit.setTint(mDrawableColor)

                    if (mBackgroundColor == mDrawableColor) {
                        if (Utils.isColorDark(mDrawableColor)) bit.setTint(Utils.lightenColor(mDrawableColor, 0.20F))
                        else bit.setTint(Utils.darkenColor(mDrawableColor, 0.20F))
                    }
                    Utils.drawBitmap(bit, canvas, mDeviceWidth, mDeviceHeight, mScaleFactor)
                }
            } finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas)
            }
            handler.removeCallbacks(drawRunner)
        }
    }
}
