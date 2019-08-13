package com.iven.vectorify

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.iven.vectorify.ui.Utils
import com.iven.vectorify.ui.VectorView
import kotlinx.android.synthetic.main.preview_activity.*
import kotlinx.android.synthetic.main.seekbar_card.*
import kotlinx.android.synthetic.main.toolbar.*

const val SAVE_WALLPAPER = 0
const val SET_WALLPAPER = 1
const val SET_LIVE_WALLPAPER = 2

class PreviewActivity : AppCompatActivity() {

    private var sUserIsSeeking = false

    private lateinit var mVectorView: VectorView

    override fun onBackPressed() {
        super.onBackPressed()
        finishAndRemoveTask()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.save ->
                if (Utils.hasToRequestWriteStoragePermission(this)) Utils.makeRationaleDialog(
                    this,
                    SAVE_WALLPAPER,
                    true
                ) else handleWallpaperChanges(SAVE_WALLPAPER)

            R.id.set -> if (Utils.hasToRequestWriteStoragePermission(this)) Utils.makeRationaleDialog(
                this,
                SET_WALLPAPER,
                true
            ) else handleWallpaperChanges(SET_WALLPAPER)
            R.id.go_live -> handleWallpaperChanges(SET_LIVE_WALLPAPER)
            android.R.id.home -> finishAndRemoveTask()
        }
        return super.onOptionsItemSelected(item)
    }

    //manage request permission result, continue loading ui if permissions was granted
    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            val shouldShowRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (!shouldShowRationale)
                Utils.makeRationaleDialog(this, requestCode, shouldShowRationale) else
                Toast.makeText(this, getString(R.string.boo), Toast.LENGTH_LONG)
                    .show()
        } else {
            when (requestCode) {
                SAVE_WALLPAPER -> handleWallpaperChanges(SAVE_WALLPAPER)
                SET_WALLPAPER -> handleWallpaperChanges(SET_WALLPAPER)
            }
        }
    }

    private fun setToolbarAndSeekBarColors() {

        if (Utils.isColorDark(mTempPreferences.tempBackgroundColor)) toolbar.context.setTheme(R.style.ToolbarStyle_Dark)
        else toolbar.context.setTheme(R.style.ToolbarStyle)

        //determine if background color is dark or light and select
        //the appropriate color for UI widgets
        val widgetColors = Utils.getSecondaryColor(mTempPreferences.tempBackgroundColor)

        toolbar.setBackgroundColor(mTempPreferences.tempBackgroundColor)
        toolbar.setTitleTextColor(widgetColors)
        toolbar.setNavigationIcon(R.drawable.ic_back)

        //set seekbar colors
        seekbar_card.setCardBackgroundColor(mTempPreferences.tempBackgroundColor)
        seek_size.progressTintList = ColorStateList.valueOf(widgetColors)
        seek_size.thumbTintList = ColorStateList.valueOf(widgetColors)
        seek_size.progressBackgroundTintList = ColorStateList.valueOf(widgetColors)

        seekbar_title.setTextColor(widgetColors)
        scale_text.setTextColor(widgetColors)
    }

    private fun handleWallpaperChanges(which: Int) {

        //do all the save shit here
        if (mTempPreferences.isBackgroundColorChanged) {
            mVectorifyPreferences.backgroundColor = mTempPreferences.tempBackgroundColor
            mTempPreferences.isBackgroundColorChanged = false
        }

        if (mTempPreferences.isVectorColorChanged) {
            mVectorifyPreferences.vectorColor = mTempPreferences.tempVectorColor
            mTempPreferences.isVectorColorChanged = false
        }

        if (mTempPreferences.isVectorChanged) {
            mVectorifyPreferences.vector = mTempPreferences.tempVector
            mTempPreferences.isVectorChanged = false
        }

        if (mTempPreferences.isScaleChanged) {
            mVectorifyPreferences.scale = mTempPreferences.tempScale
            mTempPreferences.isScaleChanged = false
        }

        saveToRecentSetups()

        when (which) {
            SAVE_WALLPAPER -> mVectorView.vectorifyDaHome(false)
            SET_WALLPAPER -> mVectorView.vectorifyDaHome(true)
            SET_LIVE_WALLPAPER -> {
                if (!Utils.isLiveWallpaperRunning(this)) Utils.openLiveWallpaperIntent(this)
                else
                    Toast.makeText(this, getString(R.string.title_already_live), Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun saveToRecentSetups() {
        val recentSetups = mVectorifyPreferences.recentSetups.toMutableList()
        val stringToSave = getString(
            R.string.recent_setups_save_pattern,
            mTempPreferences.tempBackgroundColor.toString(),
            mTempPreferences.tempVector.toString(),
            mTempPreferences.tempVectorColor.toString()
        )
        recentSetups.add(stringToSave)
        mVectorifyPreferences.recentSetups = recentSetups.toMutableSet()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //set ui theme and immersive mode
        setTheme(mVectorifyPreferences.theme)
        hideSystemUI()

        setContentView(R.layout.preview_activity)

        //get all the views
        mVectorView = vector_view

        //set toolbar shit
        //match theme with background luminance
        setToolbarAndSeekBarColors()

        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        //observe seekbar changes
        seek_size.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            var userProgress = 0
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                sUserIsSeeking = true
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && progress >= 10) {
                    userProgress = progress
                    scale_text.text = Utils.getDecimalFormattedString(progress.toFloat() / 100)
                    mVectorView.setScaleFactor(progress.toFloat() / 100)
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                sUserIsSeeking = false
                mTempPreferences.isScaleChanged = true
                mTempPreferences.tempScale = userProgress.toFloat() / 100
            }
        })

        //restore saved scale value
        seek_size.progress = (mTempPreferences.tempScale * 100).toInt()

        //set scale text
        scale_text.text = Utils.getDecimalFormattedString(mTempPreferences.tempScale)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    //immersive mode
    //https://developer.android.com/training/system-ui/immersive
    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}
