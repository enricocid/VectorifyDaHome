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

    fun updateVectorView(wallpaper: VectorifyWallpaper) {
        with(wallpaper) {
            mBackgroundColor = backgroundColor
            mVectorColor = vectorColor
            mVector = resource
            mCategory = category
            mScaleFactor = scale
            mHorizontalOffset = horizontalOffset
            mVerticalOffset = verticalOffset
        }

        mDeviceMetrics = vectorifyPreferences.savedMetrics
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
                Utils.drawBitmap(mDrawable, cv, mDeviceMetrics.width, mDeviceMetrics.height,
                    mScaleFactor, mHorizontalOffset, mVerticalOffset
                )
            }
    }

    //update scale factor when the user stop seeking
    fun setScaleFactor(scale: Float) {
        mScaleFactor = scale
        invalidate()
    }

    fun moveUp(): Float {
        mVerticalOffset -= mStep
        invalidate()
        return mVerticalOffset
    }

    fun moveDown(): Float {
        mVerticalOffset += mStep
        invalidate()
        return mVerticalOffset
    }

    fun moveLeft(): Float {
        mHorizontalOffset -= mStep
        invalidate()
        return mHorizontalOffset
    }

    fun moveRight(): Float {
        mHorizontalOffset += mStep
        invalidate()
        return mHorizontalOffset
    }

    fun centerHorizontal() {
        mHorizontalOffset = 0F
        invalidate()
    }

    fun centerVertical() {
        mVerticalOffset = 0F
        invalidate()
    }

    fun resetPosition(horizontalOffset: Float, verticalOffset: Float) {

        mHorizontalOffset = horizontalOffset
        mVerticalOffset = verticalOffset

        invalidate()
    }

    fun saveToPrefs() : VectorifyWallpaper {
        //save wallpaper to prefs
        val toSave = VectorifyWallpaper(mBackgroundColor, mVectorColor,
            mVector, mCategory, mScaleFactor, mHorizontalOffset, mVerticalOffset
        )
        toSave.addToRecentSetups(Utils.isDeviceLand(resources))
        if (Utils.isDeviceLand(resources)) {
            vectorifyPreferences.savedWallpaperLand = toSave
            return toSave
        }
        vectorifyPreferences.savedWallpaper = toSave
        return toSave
    }
}
