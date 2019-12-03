package com.iven.vectorify.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.drawToBitmap
import com.iven.vectorify.deviceMetrics
import com.iven.vectorify.tempPreferences
import com.iven.vectorify.vectorifyPreferences
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

    private var mStep = 15F

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
        mDeviceWidth = deviceMetrics.first
        mDeviceHeight = deviceMetrics.second

        mBackgroundColor = tempPreferences.tempBackgroundColor
        mVectorColor = tempPreferences.tempVectorColor
        mScaleFactor = tempPreferences.tempScale
        mHorizontalOffset = tempPreferences.tempHorizontalOffset
        mVerticalOffset = tempPreferences.tempVerticalOffset
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas != null && context != null) {

            //draw the vector!
            canvas.drawColor(mBackgroundColor)

            val drawable =
                Utils.tintDrawable(
                    context,
                    tempPreferences.tempVector,
                    mBackgroundColor,
                    mVectorColor,
                    false
                )

            Utils.drawBitmap(
                drawable,
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
        tempPreferences.isVerticalOffsetChanged = true
        tempPreferences.tempVerticalOffset = mVerticalOffset
        invalidate()
    }

    fun moveDown() {
        mVerticalOffset += mStep
        tempPreferences.isVerticalOffsetChanged = true
        tempPreferences.tempVerticalOffset = mVerticalOffset
        invalidate()
    }

    fun moveLeft() {
        mHorizontalOffset -= mStep
        tempPreferences.isHorizontalOffsetChanged = true
        tempPreferences.tempHorizontalOffset = mHorizontalOffset
        invalidate()
    }

    fun moveRight() {
        mHorizontalOffset += mStep
        tempPreferences.isHorizontalOffsetChanged = true
        tempPreferences.tempHorizontalOffset = mHorizontalOffset
        invalidate()
    }

    fun centerHorizontal() {
        mHorizontalOffset = 0F
        tempPreferences.tempHorizontalOffset = mHorizontalOffset
        tempPreferences.isHorizontalOffsetChanged = true
        invalidate()
    }

    fun centerVertical() {
        mVerticalOffset = 0F
        tempPreferences.tempVerticalOffset = mVerticalOffset
        tempPreferences.isVerticalOffsetChanged = true
        invalidate()
    }

    fun resetPosition() {
        mHorizontalOffset = vectorifyPreferences.horizontalOffset
        tempPreferences.tempHorizontalOffset = mHorizontalOffset
        tempPreferences.isHorizontalOffsetChanged = true
        mVerticalOffset = vectorifyPreferences.verticalOffset
        tempPreferences.tempVerticalOffset = mVerticalOffset
        tempPreferences.isVerticalOffsetChanged = true
        invalidate()
    }
}
