package com.iven.vectorify.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.iven.vectorify.R
import com.iven.vectorify.vectorifyPreferences
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SaveWallpaperViewModel(application: Application) : AndroidViewModel(application) {

    val wallpaperUri = MutableLiveData<Uri?>()

    /**
     * This is the job for all coroutines started by this ViewModel.
     * Cancelling this job will cancel all coroutines started by this ViewModel.
     */
    private val mViewModelJob = SupervisorJob()

    private val mHandler = CoroutineExceptionHandler { _, exception ->
        exception.printStackTrace()
        wallpaperUri.value = null
    }

    private val mUiDispatcher = Dispatchers.Main
    private val mIoDispatcher = Dispatchers.IO + mViewModelJob + mHandler
    private val mUiScope = CoroutineScope(mUiDispatcher)

    /**
     * Cancel all coroutines when the ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        mViewModelJob.cancel()
    }

    fun cancel() {
        onCleared()
    }

    fun startSaveWallpaper(bitmapToProcess: Bitmap, isSetWallpaper: Boolean) {
        mUiScope.launch {
            withContext(mIoDispatcher) {
               val uri = saveWallpaper(getApplication(), cropBitmapFromCenterAndScreenSize(bitmapToProcess), isSetWallpaper)
                withContext(mUiDispatcher) {
                    wallpaperUri.value = uri
                }
            }
        }
    }

        private fun saveWallpaper(application: Application, bitmap: Bitmap, isSetWallpaper: Boolean) : Uri? {

            return try {
                // Get the external storage directory path
                application.getExternalFilesDir(null)?.let { path ->

                    val format = SimpleDateFormat(
                            application.getString(R.string.time_pattern),
                            Locale.getDefault()
                    ).format(Date())

                    // Create a file to save the image
                    val wallpaperToSave =
                            File(path, "${application.getString(R.string.save_pattern) + format}.png")

                    // Get the file output stream
                    val stream = FileOutputStream(wallpaperToSave)

                    // Compress the bitmap
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

                    // Flush and close the output stream
                    stream.run {
                        flush()
                        close()
                    }

                    if (isSetWallpaper) {
                        FileProvider.getUriForFile(
                                application,
                                application.getString(R.string.app_name),
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

    private fun cropBitmapFromCenterAndScreenSize(bitmapToProcess: Bitmap): Bitmap {
        //https://stackoverflow.com/a/25699365

        val displayMetrics = vectorifyPreferences.savedMetrics
        val deviceWidth = displayMetrics.width
        val deviceHeight = displayMetrics.height

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
