package com.iven.vectorify.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.loader.content.AsyncTaskLoader
import com.iven.vectorify.R
import com.iven.vectorify.vectorifyPreferences
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SaveWallpaperLoader(
        context: Context,
        private val bitmapToProcess: Bitmap,
        private val isSetWallpaper: Boolean
) : AsyncTaskLoader<Uri?>(context) {

    override fun onStartLoading() {
        super.onStartLoading()
        forceLoad()
    }

    // This is where background code is executed
    override fun loadInBackground() =
            saveImageToExternalStorage(cropBitmapFromCenterAndScreenSize())


    // Method to save an image to external storage
    private fun saveImageToExternalStorage(bitmap: Bitmap): Uri? {

        return try {
            // Get the external storage directory path
            context.getExternalFilesDir(null)?.let { path ->

                val format = SimpleDateFormat(
                        context.getString(R.string.time_pattern),
                        Locale.getDefault()
                ).format(Date())

                // Create a file to save the image
                val wallpaperToSave =
                        File(path, "${context.getString(R.string.save_pattern) + format}.png")

                // Get the file output stream
                val stream = FileOutputStream(wallpaperToSave)

                // Compress the bitmap
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

                // Flush and close the output stream
                stream.apply {
                    flush()
                    close()
                }

                if (isSetWallpaper) {
                    FileProvider.getUriForFile(
                            context,
                            context.getString(R.string.live_wallpaper_name),
                            wallpaperToSave
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun cropBitmapFromCenterAndScreenSize(): Bitmap {
        //https://stackoverflow.com/a/25699365

        val displayMetrics = vectorifyPreferences.vectorifyMetrics
        val deviceWidth = displayMetrics!!.first
        val deviceHeight = displayMetrics.second

        val bitmapWidth = bitmapToProcess.width.toFloat()
        val bitmapHeight = bitmapToProcess.height.toFloat()

        val bitmapRatio = bitmapWidth / bitmapHeight
        val screenRatio = deviceWidth / deviceHeight
        val bitmapNewWidth: Int
        val bitmapNewHeight: Int

        if (screenRatio > bitmapRatio) {
            bitmapNewWidth = deviceWidth
            bitmapNewHeight = (bitmapNewWidth / bitmapRatio).toInt()
        } else {
            bitmapNewHeight = deviceHeight
            bitmapNewWidth = (bitmapNewHeight * bitmapRatio).toInt()
        }

        val newBitmap = Bitmap.createScaledBitmap(
                bitmapToProcess, bitmapNewWidth,
                bitmapNewHeight, true
        )

        val bitmapGapX = ((bitmapNewWidth - deviceWidth) / 2.0f).toInt()
        val bitmapGapY = ((bitmapNewHeight - deviceHeight) / 2.0f).toInt()

        //final bitmap
        return Bitmap.createBitmap(
                newBitmap, bitmapGapX, bitmapGapY,
                deviceWidth, deviceHeight
        )
    }
}
