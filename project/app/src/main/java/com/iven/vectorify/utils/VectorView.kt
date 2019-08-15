package com.iven.vectorify.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.iven.vectorify.mDeviceMetrics
import com.iven.vectorify.mTempPreferences
import java.lang.ref.WeakReference

class VectorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var mDeviceWidth = 0
    private var mDeviceHeight = 0

    private var mBackgroundColor = Color.BLACK
    private var mVectorColor = Color.WHITE
    private var mScaleFactor = 0.35F

    fun vectorifyDaHome(isSetAsWallpaper: Boolean) {
        SaveWallpaperAsync(
            WeakReference(context),
            drawToBitmap(),
            mDeviceWidth,
            mDeviceHeight,
            isSetAsWallpaper
        ).execute()
    }

    init {
        //retrieve display specifications
        mDeviceWidth = mDeviceMetrics.first
        mDeviceHeight = mDeviceMetrics.second

        mScaleFactor = mTempPreferences.tempScale
        mBackgroundColor = mTempPreferences.tempBackgroundColor
        mVectorColor = mTempPreferences.tempVectorColor
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas != null && context != null) {
            //draw the vector!
            canvas.drawColor(mBackgroundColor)
            setColorFilter(mVectorColor)
            val bit = ContextCompat.getDrawable(context, mTempPreferences.tempVector) as VectorDrawable
            bit.mutate()
            bit.setTint(mVectorColor)

            //darken or lighten color to increase vector visibility when the colors are the same
            if (Utils.checkIfColorsEqual(mBackgroundColor, mVectorColor)) {
                if (Utils.isColorDark(mVectorColor)) bit.setTint(Utils.lightenColor(mVectorColor, 0.20F))
                else bit.setTint(Utils.darkenColor(mVectorColor, 0.20F))
            }
            Utils.drawBitmap(bit, canvas, mDeviceWidth, mDeviceHeight, mScaleFactor)
        }
    }

    //update scale factor when the user stop seeking
    fun setScaleFactor(scale: Float) {
        mScaleFactor = scale
        invalidate()
    }
}
