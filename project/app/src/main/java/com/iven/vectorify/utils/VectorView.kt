package com.iven.vectorify.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.drawToBitmap
import com.iven.vectorify.mDeviceMetrics
import com.iven.vectorify.mTempPreferences
import com.iven.vectorify.mVectorifyPreferences
import java.lang.ref.WeakReference

class VectorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var mDeviceWidth = 0
    private var mDeviceHeight = 0

    private var mBackgroundColor = Color.BLACK
    private var mVectorColor = Color.WHITE
    private var mScaleFactor = 0.35F

    private var mVerticalOffset = 0F
    private var mHorizontalOffset = 0F

    private var mStep = 0F

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

        mBackgroundColor = mTempPreferences.tempBackgroundColor
        mVectorColor = mTempPreferences.tempVectorColor
        mScaleFactor = mTempPreferences.tempScale
        mHorizontalOffset = mTempPreferences.tempHorizontalOffset
        mVerticalOffset = mTempPreferences.tempVerticalOffset
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas != null && context != null) {

            //draw the vector!
            canvas.drawColor(mBackgroundColor)

            val vectorDrawable =
                Utils.tintVectorDrawable(context, mTempPreferences.tempVector, mBackgroundColor, mVectorColor)

            mStep = Utils.drawBitmap(
                vectorDrawable,
                canvas,
                mDeviceWidth,
                mDeviceHeight,
                mScaleFactor,
                mHorizontalOffset,
                mVerticalOffset
            )
        }
    }

    //update scale factor when the user stop seeking
    fun setScaleFactor(scale: Float) {
        mScaleFactor = scale
        invalidate()
    }

    fun moveUp() {
        mVerticalOffset -= mStep
        mTempPreferences.isVerticalOffsetChanged = true
        mTempPreferences.tempVerticalOffset = mVerticalOffset
        invalidate()
    }

    fun moveDown() {
        mVerticalOffset += mStep
        mTempPreferences.isVerticalOffsetChanged = true
        mTempPreferences.tempVerticalOffset = mVerticalOffset
        invalidate()
    }

    fun moveLeft() {
        mHorizontalOffset -= mStep
        mTempPreferences.isHorizontalOffsetChanged = true
        mTempPreferences.tempHorizontalOffset = mHorizontalOffset
        invalidate()
    }

    fun moveRight() {
        mHorizontalOffset += mStep
        mTempPreferences.isHorizontalOffsetChanged = true
        mTempPreferences.tempHorizontalOffset = mHorizontalOffset
        invalidate()
    }

    fun centerHorizontal() {
        mHorizontalOffset = 0F
        mTempPreferences.tempHorizontalOffset = mHorizontalOffset
        mTempPreferences.isHorizontalOffsetChanged = true
        invalidate()
    }

    fun centerVertical() {
        mVerticalOffset = 0F
        mTempPreferences.tempVerticalOffset = mVerticalOffset
        mTempPreferences.isVerticalOffsetChanged = true
        invalidate()
    }

    fun resetPosition() {
        mHorizontalOffset = mVectorifyPreferences.horizontalOffset
        mTempPreferences.tempHorizontalOffset = mHorizontalOffset
        mTempPreferences.isHorizontalOffsetChanged = true
        mVerticalOffset = mVectorifyPreferences.verticalOffset
        mTempPreferences.tempVerticalOffset = mVerticalOffset
        mTempPreferences.isVerticalOffsetChanged = true
        mTempPreferences.isScaleChanged = true
        invalidate()
    }
}
