package com.iven.vectorify.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.drawToBitmap
import com.iven.vectorify.R
import com.iven.vectorify.addToRecentSetups
import com.iven.vectorify.models.Metrics
import com.iven.vectorify.models.VectorifyWallpaper
import com.iven.vectorify.utils.Utils
import com.iven.vectorify.vectorifyPreferences

class VectorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    var onSetWallpaper: ((Boolean, Bitmap) -> Unit)? = null

    private var mBackgroundColor = Color.BLACK
    private var mVectorColor = Color.WHITE
    private var mVector = R.drawable.android_logo_2019
    private var mCategory = 0
    private var mScaleFactor = 0.35F
    private var mHorizontalOffset = 0F
    private var mVerticalOffset = 0F

    private var mStep = 15F

    private lateinit var mDeviceMetrics: Metrics

    private var mDrawable: Drawable? = null

    fun updateVectorView(vectorifyWallpaper: VectorifyWallpaper) {
        mBackgroundColor = vectorifyWallpaper.backgroundColor
        mVectorColor = vectorifyWallpaper.vectorColor
        mVector = vectorifyWallpaper.resource
        mCategory = vectorifyWallpaper.category
        mScaleFactor = vectorifyWallpaper.scale
        mHorizontalOffset = vectorifyWallpaper.horizontalOffset
        mVerticalOffset = vectorifyWallpaper.verticalOffset

        mDeviceMetrics = vectorifyPreferences.vectorifyMetrics
        mDrawable = Utils.tintDrawable(
                context,
                mVector,
                mVectorColor
        )
    }

    fun vectorifyDaHome(isSetAsWallpaper: Boolean) {
        onSetWallpaper?.invoke(isSetAsWallpaper, drawToBitmap())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

            canvas?.let { cv ->

                cv.drawColor(mBackgroundColor)

                //draw the vector!
                Utils.drawBitmap(
                    mDrawable,
                    cv,
                    mDeviceMetrics.width,
                    mDeviceMetrics.height,
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
        invalidate()
    }

    fun moveDown() {
        mVerticalOffset += mStep
        invalidate()
    }

    fun moveLeft() {
        mHorizontalOffset -= mStep
        invalidate()
    }

    fun moveRight() {
        mHorizontalOffset += mStep
        invalidate()
    }

    fun centerHorizontal() {
        mHorizontalOffset = 0F
        invalidate()
    }

    fun centerVertical() {
        mVerticalOffset = 0F
        invalidate()
    }

    fun resetPosition() {

        mHorizontalOffset = 0F
        mVerticalOffset = 0F

        vectorifyPreferences.liveVectorifyWallpaper?.let { recent ->
            mHorizontalOffset = recent.horizontalOffset
            mVerticalOffset = recent.verticalOffset
        }

        invalidate()
    }

    fun saveToPrefs() {
        //save wallpaper to prefs
        vectorifyPreferences.liveVectorifyWallpaper = VectorifyWallpaper(
            mBackgroundColor,
            mVectorColor,
            mVector,
            mCategory,
            mScaleFactor,
            mHorizontalOffset,
            mVerticalOffset
        )
    }

    fun saveToRecentSetups() {

        //update recent setups
        VectorifyWallpaper(
            mBackgroundColor,
            mVectorColor,
            mVector,
            mCategory,
            mScaleFactor,
            mHorizontalOffset,
            mVerticalOffset
        ).addToRecentSetups()
    }
}
