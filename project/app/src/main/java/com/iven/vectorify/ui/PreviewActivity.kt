package com.iven.vectorify.ui

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import com.iven.vectorify.*
import com.iven.vectorify.utils.Utils
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import kotlinx.android.synthetic.main.position_controls.*
import kotlinx.android.synthetic.main.preview_activity.*
import kotlinx.android.synthetic.main.size_position_card.*

const val SAVE_WALLPAPER = 0
const val SET_WALLPAPER = 1

const val TEMP_BACKGROUND_COLOR = "TEMP_BACKGROUND_COLOR"
const val TEMP_VECTOR_COLOR = "TEMP_VECTOR_COLOR"
const val TEMP_VECTOR = "TEMP_VECTOR"
const val TEMP_CATEGORY = "TEMP_CATEGORY"
const val TEMP_SCALE = "TEMP_SCALE"
const val TEMP_H_OFFSET = "TEMP_H_OFFSET"
const val TEMP_V_OFFSET = "TEMP_V_OFFSET"

@Suppress("UNUSED_PARAMETER")
class PreviewActivity : AppCompatActivity() {

    private var sUserIsSeeking = false

    private lateinit var mToolbar: Toolbar
    private lateinit var mVectorView: VectorView
    private lateinit var mSeekBar: SeekBar
    private lateinit var mSeekBarScale: TextView

    private val mBackupRecent = vectorifyPreferences.vectorifyWallpaperBackup

    private var mTempBackgroundColor = mBackupRecent.backgroundColor
    private var mTempVectorColor = mBackupRecent.vectorColor
    private var mTempVector = mBackupRecent.resource
    private var mTempCategory = mBackupRecent.category
    private var mTempScale = mBackupRecent.scale
    private var mTempHorizontalOffset = mBackupRecent.horizontalOffset
    private var mTempVerticalOffset = mBackupRecent.verticalOffset

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
                ) else mVectorView.vectorifyDaHome(false)

            R.id.set -> if (Utils.hasToRequestWriteStoragePermission(this)) Utils.makeRationaleDialog(
                this,
                SET_WALLPAPER,
                true
            ) else mVectorView.vectorifyDaHome(true)
            R.id.go_live -> updatePrefsAndSetLiveWallpaper()
            android.R.id.home -> finishAndRemoveTask()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        //set immersive mode
        hideSystemUI()
        return super.onCreateView(parent, name, context, attrs)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.extras?.let { ext ->
            mTempBackgroundColor = ext.getInt(TEMP_BACKGROUND_COLOR)
            mTempVectorColor = ext.getInt(TEMP_VECTOR_COLOR)
            mTempVector = ext.getInt(TEMP_VECTOR)
            mTempCategory = ext.getInt(TEMP_CATEGORY)
            mTempScale = ext.getFloat(TEMP_SCALE)
            mTempHorizontalOffset = ext.getFloat(TEMP_H_OFFSET)
            mTempVerticalOffset = ext.getFloat(TEMP_V_OFFSET)
        }

        setContentView(if (mTempBackgroundColor.isDark()) R.layout.preview_activity_dark else R.layout.preview_activity)

        getViews()

        //set vector view
        mVectorView.updateVectorView(
            VectorifyWallpaper(
                mTempBackgroundColor,
                mTempVectorColor,
                mTempVector,
                mTempCategory,
                mTempScale,
                mTempHorizontalOffset,
                mTempVerticalOffset
            )
        )

        setSupportActionBar(mToolbar)

        supportActionBar?.let { ab ->
            ab.setHomeButtonEnabled(true)
            ab.setDisplayHomeAsUpEnabled(true)
        }

        //set toolbar shit
        //match theme with background luminance
        setToolbarAndSeekBarColors()

        initializeSeekBar()
    }

    //manage request permission result, continue loading ui if permissions was granted
    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            val shouldShowRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            if (!shouldShowRationale)
                Utils.makeRationaleDialog(this, requestCode, shouldShowRationale) else
                DynamicToast.makeError(this, getString(R.string.boo), Toast.LENGTH_LONG)
                    .show()
        } else {
            when (requestCode) {
                SAVE_WALLPAPER -> setWallpaper(false)
                SET_WALLPAPER -> setWallpaper(true)
            }
        }
    }

    private fun getViews() {
        mToolbar = findViewById(R.id.toolbar)
        mVectorView = vector_view
        mSeekBar = seek_size
        mSeekBarScale = scale_text
    }

    private fun initializeSeekBar() {
        //observe SeekBar changes
        mSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            var userProgress = 0
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                sUserIsSeeking = true
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && progress >= 10) {
                    userProgress = progress
                    mSeekBarScale.text = (progress.toFloat() / 100).toDecimalFormat()
                    mVectorView.setScaleFactor(progress.toFloat() / 100)
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                sUserIsSeeking = false
                mTempScale = userProgress.toFloat() / 100
            }
        })

        //restore saved scale value
        mSeekBar.progress = (mTempScale * 100).toInt()

        //set scale text
        mSeekBarScale.text = mTempScale.toDecimalFormat()
    }

    private fun setWallpaper(set: Boolean) {
        updateLatestSetup()
        updateRecentSetups()
        mVectorView.vectorifyDaHome(set)
    }

    private fun setToolbarAndSeekBarColors() {

        //determine if background color is dark or light and select
        //the appropriate color for UI widgets
        val widgetColors = mTempBackgroundColor.toSurfaceColor()

        val cardColor = ColorUtils.setAlphaComponent(mTempBackgroundColor, 100)

        mToolbar.apply {
            //  this.context.setTheme(if (mTempBackgroundColor.isDark()) R.style.ToolbarStyle_Dark else R.style.ToolbarStyle)
            setBackgroundColor(cardColor)
            setNavigationIcon(R.drawable.ic_navigate_before)
        }

        //set SeekBar colors
        seekbar_card.apply {
            setCardBackgroundColor(cardColor)
            strokeColor = ColorUtils.setAlphaComponent(widgetColors, 25)
        }

        mSeekBar.apply {
            progressTintList = ColorStateList.valueOf(widgetColors)
            thumbTintList = ColorStateList.valueOf(widgetColors)
            progressBackgroundTintList = ColorStateList.valueOf(widgetColors)
        }

        seekbar_title.setTextColor(widgetColors)
        mSeekBarScale.setTextColor(widgetColors)

        up.drawable.mutate().setTint(widgetColors)
        down.drawable.mutate().setTint(widgetColors)
        left.drawable.mutate().setTint(widgetColors)
        right.drawable.mutate().setTint(widgetColors)
        center_horizontal.drawable.mutate().setTint(widgetColors)
        center_vertical.drawable.mutate().setTint(widgetColors)
        reset_position.drawable.mutate().setTint(widgetColors)
    }

    private fun updateLatestSetup() {
        vectorifyPreferences.savedVectorifyWallpaper = VectorifyWallpaper(
            mTempBackgroundColor,
            mTempVectorColor,
            mTempVector,
            mTempCategory,
            mTempScale,
            mTempHorizontalOffset,
            mTempVerticalOffset
        )
    }

    private fun updatePrefsAndSetLiveWallpaper() {

        updateLatestSetup()

        //check if the live wallpaper is already running
        //if so, don't open the live wallpaper picker, just updated preferences
        if (!Utils.isLiveWallpaperRunning(this)) Utils.openLiveWallpaperIntent(this)
        else
            DynamicToast.makeSuccess(this, getString(R.string.title_already_live))
                .show()

        //update recent setups
        updateRecentSetups()
    }

    private fun updateRecentSetups() {
        //update recent setups
        val recentSetups =
            if (vectorifyPreferences.vectorifyWallpaperSetups != null) vectorifyPreferences.vectorifyWallpaperSetups else mutableListOf()

        val recentToSave = VectorifyWallpaper(
            mTempBackgroundColor,
            mTempVectorColor,
            mTempVector,
            mTempCategory,
            mTempScale,
            mTempHorizontalOffset,
            mTempVerticalOffset
        )

        if (!recentSetups?.contains(recentToSave)!!) recentSetups.add(recentToSave)
        vectorifyPreferences.vectorifyWallpaperSetups = recentSetups
    }

    fun moveVectorUp(view: View) {
        mVectorView.moveUp()
    }

    fun moveVectorDown(view: View) {
        mVectorView.moveDown()
    }

    fun moveVectorLeft(view: View) {
        mVectorView.moveLeft()
    }

    fun moveVectorRight(view: View) {
        mVectorView.moveRight()
    }

    fun centerHorizontalVectorPosition(view: View) {
        mVectorView.centerHorizontal()
    }

    fun centerVerticalVectorPosition(view: View) {
        mVectorView.centerVertical()
    }

    fun resetVectorPosition(view: View) {
        val savedScale = vectorifyPreferences.savedVectorifyWallpaper?.scale!!
        mVectorView.setScaleFactor(savedScale)
        mTempScale = savedScale
        mSeekBar.progress = (savedScale * 100).toInt()
        mSeekBarScale.text = (mSeekBar.progress.toFloat() / 100).toDecimalFormat()
        mVectorView.resetPosition()
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
