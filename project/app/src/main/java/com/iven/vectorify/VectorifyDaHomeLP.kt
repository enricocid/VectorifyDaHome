package com.iven.vectorify

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.util.DisplayMetrics
import android.view.SurfaceHolder
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.iven.vectorify.ui.Utils

class VectorifyDaHomeLP : WallpaperService() {

    private var mBackgroundColor = 0
    private var mDrawableColor = 0

    private var mDeviceWidth = 0F
    private var mDeviceHeight = 0F

    //the potato battery live vectorify_wallpaper service and engine
    override fun onCreateEngine(): Engine {

        if (baseContext != null) {
            //retrieve display specifications
            val window = baseContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val d = DisplayMetrics()
            window.defaultDisplay.getRealMetrics(d)
            mDeviceWidth = d.widthPixels.toFloat()
            mDeviceHeight = d.heightPixels.toFloat()
        }

        //set paints props
        mBackgroundColor = mVectorifyPreferences.backgroundColor
        mDrawableColor = mVectorifyPreferences.iconColor

        return VectorifyEngine()
    }

    private fun checkSystemAccent() {

        val isBackgroundAccented = mVectorifyPreferences.isBackgroundAccented
        val isPotatoAccented = mVectorifyPreferences.isIconAccented

        if (isBackgroundAccented || isPotatoAccented) {
            //change only if system accent has changed
            val systemAccentColor = Utils.getSystemAccentColor(this)
            if (isBackgroundAccented && mBackgroundColor != systemAccentColor) mBackgroundColor =
                systemAccentColor
            if (isPotatoAccented && mDrawableColor != systemAccentColor) mDrawableColor = systemAccentColor
        }
    }

    private inner class VectorifyEngine : WallpaperService.Engine() {

        private val handler = Handler()
        private var sVisible = true
        private val drawRunner = Runnable { draw() }

        override fun onVisibilityChanged(visible: Boolean) {
            sVisible = visible
            if (visible) {
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

        private fun drawBitmap(vectorDrawable: VectorDrawable, canvas: Canvas) {

            val bitmap = Bitmap.createBitmap(
                (mDeviceWidth * 0.35f).toInt(),
                (mDeviceWidth * 0.35f).toInt(), Bitmap.Config.ARGB_8888
            )

            val drawableCanvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, drawableCanvas.width, drawableCanvas.height)

            vectorDrawable.draw(drawableCanvas)

            canvas.drawBitmap(
                bitmap,
                canvas.width / 2F - drawableCanvas.width / 2F,
                canvas.height / 2F - drawableCanvas.width / 2F,
                null
            )
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
                    val bit = ContextCompat.getDrawable(baseContext, mVectorifyPreferences.icon) as VectorDrawable
                    bit.setTint(mDrawableColor)

                    if (mBackgroundColor == mDrawableColor) {
                        if (Utils.isColorDark(mDrawableColor)) bit.setTint(Utils.lightenColor(mDrawableColor, 0.20F))
                        else bit.setTint(Utils.darkenColor(mDrawableColor, 0.20F))
                    }
                    drawBitmap(bit, canvas)
                }
            } finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas)
            }
            handler.removeCallbacks(drawRunner)
        }
    }
}