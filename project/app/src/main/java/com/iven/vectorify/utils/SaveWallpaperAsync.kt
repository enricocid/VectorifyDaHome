package com.iven.vectorify.utils

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.iven.vectorify.R
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class SaveWallpaperAsync(
    @NonNull private val contextReference: WeakReference<Context>?,
    @NonNull private val bitmap: Bitmap,
    private val deviceWidth: Int,
    private val deviceHeight: Int,
    private val isSetAsWallpaper: Boolean
) : AsyncTask<Void, Void, Pair<String, String>>() {

    private lateinit var mMaterialDialog: MaterialDialog

    // Method to save an image to external storage
    private fun saveImageToExternalStorage(bitmap: Bitmap): Pair<String, String>? {

        val context = contextReference?.get()!!

        // Get the external storage directory path
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        val s = SimpleDateFormat(context.getString(R.string.time_pattern), Locale.getDefault())
        val format = s.format(Date())

        val saveFormat = "${context.getString(R.string.save_pattern) + format}.png"

        // Create a file to save the image
        val file = File(path, saveFormat)

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

            // Flush the output stream
            stream.flush()

            // Close the output stream
            stream.close()

        } catch (e: Exception) { // Catch the exception
            e.printStackTrace()
        }

        //refresh media store database
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val uri = Uri.fromFile(file)
        mediaScanIntent.data = uri
        context.sendBroadcast(mediaScanIntent)
        if (isSetAsWallpaper) setWallpaper(
            FileProvider.getUriForFile(
                context,
                context.resources.getString(R.string.live_wallpaper_name),
                file
            )
        )
        return Pair(saveFormat, path!!.name)
    }

    override fun onPreExecute() {
        super.onPreExecute()

        mMaterialDialog = MaterialDialog(contextReference?.get()!!)
        mMaterialDialog.cornerRadius(res = R.dimen.md_corner_radius)
        mMaterialDialog.title(R.string.live_wallpaper_name)
        mMaterialDialog.message(R.string.loading)

        mMaterialDialog.cancelOnTouchOutside(false)
        mMaterialDialog.cancelable(false)

        if (!mMaterialDialog.isShowing) mMaterialDialog.show()
    }

    override fun doInBackground(vararg p0: Void?): Pair<String, String>? {
        //save wallpaper png before applying (applying directly freezes the ui)
        return saveImageToExternalStorage(cropBitmapFromCenterAndScreenSize(bitmap))
    }

    override fun onPostExecute(path: Pair<String, String>?) {

        if (mMaterialDialog.isShowing) mMaterialDialog.dismiss()

        val context = contextReference?.get()!!
        DynamicToast.makeSuccess(
            context,
            context.getString(R.string.message_saved_to, path!!.first, path.second),
            Toast.LENGTH_LONG
        )
            .show()

        //update recent setups
        Utils.updateRecentSetups()
    }

    //set view as wallpaper
    private fun setWallpaper(@NonNull uri: Uri) {

        val context = contextReference?.get()!!
        val wallpaperManager = WallpaperManager.getInstance(context)

        try {
            //start crop and set wallpaper intent
            context.startActivity(wallpaperManager.getCropAndSetWallpaperIntent(uri))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun cropBitmapFromCenterAndScreenSize(@NonNull bitmapToProcess: Bitmap): Bitmap {
        //https://stackoverflow.com/a/25699365

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
            bitmap, bitmapNewWidth,
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
