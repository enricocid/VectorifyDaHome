package com.iven.vectorify.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.loader.content.AsyncTaskLoader
import com.iven.vectorify.R
import com.iven.vectorify.deviceMetrics
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
                val s =
                    SimpleDateFormat(context.getString(R.string.time_pattern), Locale.getDefault())
                val format = s.format(Date())

                val saveFormat = "${context.getString(R.string.save_pattern) + format}.png"

                // Create a file to save the image
                val wallpaperToSave = File(path, saveFormat)

                // Get the file output stream
                val stream = FileOutputStream(wallpaperToSave)

                // Compress the bitmap
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

                // Flush the output stream
                stream.flush()

                // Close the output stream
                stream.close()
                if (isSetWallpaper) FileProvider.getUriForFile(
                    context,
                    context.getString(R.string.live_wallpaper_name),
                    wallpaperToSave
                ) else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun cropBitmapFromCenterAndScreenSize(): Bitmap {
        //https://stackoverflow.com/a/25699365

        val deviceWidth = deviceMetrics.first
        val deviceHeight = deviceMetrics.second

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
