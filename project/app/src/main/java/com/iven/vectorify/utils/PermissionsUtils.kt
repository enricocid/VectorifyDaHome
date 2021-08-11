package com.iven.vectorify.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.iven.vectorify.R

object PermissionsUtils {

    const val SAVE_WALLPAPER = 0
    const val SET_WALLPAPER = 1

    @JvmStatic
    fun hasToAskForReadStoragePermission(activity: Activity) =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED

    @JvmStatic
    fun manageAskForReadStoragePermission(
        activity: Activity,
        requestCode: Int
    ) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {

            MaterialDialog(activity).show {

                cancelOnTouchOutside(false)

                title(R.string.app_name)

                message(R.string.rationale)
                positiveButton(android.R.string.ok) {
                    askForReadStoragePermission(
                        activity,
                        requestCode
                    )
                }
                negativeButton {
                    Toast.makeText(activity, R.string.boo, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            askForReadStoragePermission(
                activity,
                requestCode
            )
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun askForReadStoragePermission(activity: Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            requestCode
        )
    }
}
