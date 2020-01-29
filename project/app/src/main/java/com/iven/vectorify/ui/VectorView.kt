package com.iven.vectorify.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.drawToBitmap
import com.iven.vectorify.VectorifyWallpaper
import com.iven.vectorify.deviceMetrics
import com.iven.vectorify.utils.SaveWallpaperAsync
import com.iven.vectorify.utils.Utils
import com.iven.vectorify.vectorifyPreferences
import java.lang.ref.WeakReference

class VectorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var mDeviceWidth = deviceMetrics.first
    private var mDeviceHeight = deviceMetrics.second

    private val mBackupRecent = vectorifyPreferences.vectorifyWallpaperBackup

    private var mBackgroundColor = mBackupRecent.backgroundColor
    private var mVectorColor = mBackupRecent.vectorColor
    private var mVector = mBackupRecent.resource
    private var mScaleFactor = mBackupRecent.scale
    private var mHorizontalOffset = mBackupRecent.horizontalOffset
    private var mVerticalOffset = mBackupRecent.verticalOffset

    private var mStep = 15F

    fun updateVectorView(vectorifyWallpaper: VectorifyWallpaper) {
        mBackgroundColor = vectorifyWallpaper.backgroundColor
        mVectorColor = vectorifyWallpaper.vectorColor
        mVector = vectorifyWallpaper.resource
        mScaleFactor = vectorifyWallpaper.scale
        mHorizontalOffset = vectorifyWallpaper.horizontalOffset
        mVerticalOffset = vectorifyWallpaper.verticalOffset
    }

    fun vectorifyDaHome(isSetAsWallpaper: Boolean) {
        SaveWallpaperAsync(
            WeakReference(context),
            drawToBitmap(),
            mDeviceWidth,
            mDeviceHeight,
            isSetAsWallpaper
        ).execute()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        context?.let { ctx ->
            canvas?.apply {

                drawColor(mBackgroundColor)
                //draw the vector!
                val drawable =
                    Utils.tintDrawable(
                        ctx,
                        mVector,
                        mBackgroundColor,
                        mVectorColor,
                        false
                    )

                Utils.drawBitmap(
                    drawable,
                    this,
                    mDeviceWidth,
                    mDeviceHeight,
                    mScaleFactor,
                    mHorizontalOffset,
                    mVerticalOffset
                )
            }
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
        vectorifyPreferences.savedVectorifyWallpaper?.let { recent ->
            mHorizontalOffset = recent.horizontalOffset
            mVerticalOffset = recent.verticalOffset
        }
        invalidate()
    }

}
