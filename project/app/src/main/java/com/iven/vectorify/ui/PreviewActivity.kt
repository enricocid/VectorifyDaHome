package com.iven.vectorify.ui

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.slider.Slider
import com.iven.vectorify.*
import com.iven.vectorify.databinding.PreviewActivityBinding
import com.iven.vectorify.models.VectorifyWallpaper
import com.iven.vectorify.utils.PermissionsUtils
import com.iven.vectorify.utils.Utils
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class PreviewActivity : AppCompatActivity() {

    // View binding class
    private lateinit var mPreviewActivityBinding: PreviewActivityBinding

    private var sUserIsSeeking = false

    private var mTempBackgroundColor = Color.BLACK
    private var mTempVectorColor = Color.WHITE
    private var mTempVector = R.drawable.android_logo_2019
    private var mTempCategory = 0
    private var mTempScale = 0.35F
    private var mTempHorizontalOffset = 0F
    private var mTempVerticalOffset = 0F

    private lateinit var mSaveWallpaperDialog: MaterialDialog

    private val mHandler = CoroutineExceptionHandler { _, exception ->
        exception.printStackTrace()
    }

    private val mUiDispatcher = Dispatchers.Main
    private val mIoDispatcher = Dispatchers.IO + mHandler
    private val mUiScope = CoroutineScope(mUiDispatcher)

    override fun onBackPressed() {
        super.onBackPressed()
        finishAndRemoveTask()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPreviewActivityBinding = PreviewActivityBinding.inflate(layoutInflater)
        setContentView(mPreviewActivityBinding.root)

        //set immersive mode
        hideSystemUI()

        intent?.extras?.let { ext ->
            mTempBackgroundColor = ext.getInt(TEMP_BACKGROUND_COLOR)
            mTempVectorColor = ext.getInt(TEMP_VECTOR_COLOR)
            mTempVector = ext.getInt(TEMP_VECTOR)
            mTempCategory = ext.getInt(TEMP_CATEGORY)
            mTempScale = ext.getFloat(TEMP_SCALE)
            mTempHorizontalOffset = ext.getFloat(TEMP_H_OFFSET)
            mTempVerticalOffset = ext.getFloat(TEMP_V_OFFSET)
        }

        initViews()
    }

    private fun initViews() {

        with(mPreviewActivityBinding) {

            // init click listeners
            up.setOnClickListener { mTempVerticalOffset = vectorView.moveUp() }
            down.setOnClickListener { mTempVerticalOffset = vectorView.moveDown() }
            left.setOnClickListener { mTempHorizontalOffset = vectorView.moveLeft() }
            right.setOnClickListener { mTempHorizontalOffset = vectorView.moveRight() }
            centerHorizontal.setOnClickListener {
                vectorView.centerHorizontal()
                mTempHorizontalOffset = 0F
            }
            centerVertical.setOnClickListener {
                vectorView.centerVertical()
                mTempVerticalOffset = 0F
            }

            resetPosition.setOnClickListener { resetVectorPosition(false) }
            resetPosition.setOnLongClickListener {
                resetVectorPosition(true)
                return@setOnLongClickListener true
            }

            //set vector view
            vectorView.updateVectorView(
                    VectorifyWallpaper(
                            mTempBackgroundColor,
                            mTempVectorColor.toContrastColor(mTempBackgroundColor),
                            mTempVector,
                            mTempCategory,
                            mTempScale,
                            mTempHorizontalOffset,
                            mTempVerticalOffset
                    )
            )
        }

        mPreviewActivityBinding.vectorView.onSetWallpaper = { setWallpaper, bitmap ->

            mSaveWallpaperDialog = MaterialDialog(this).apply {
                title(R.string.app_name)
                customView(R.layout.progress_dialog)
                cancelOnTouchOutside(false)
                cancelable(false)
                window?.run {
                    setFlags(
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    )
                }

                show()

                onShow {

                    this.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

                    mUiScope.launch {

                       val resultUri = saveWallpaperAsync(bitmap, setWallpaper)

                        withContext(mUiDispatcher) {

                            resultUri?.let { uri ->
                                val wallpaperManager = WallpaperManager.getInstance(this@PreviewActivity)
                                try {
                                    //start crop and set wallpaper intent
                                    startActivity(wallpaperManager.getCropAndSetWallpaperIntent(uri))
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                }
                            }

                            if (mSaveWallpaperDialog.isShowing) {
                                mSaveWallpaperDialog.dismiss()
                            }

                            val directory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                 getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            } else {
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                            }
                            Toast.makeText(
                                    this@PreviewActivity,
                                    getString(R.string.message_saved_to, directory?.name),
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }

        //match theme with background luminance
        setToolbarAndSeekBarColors()

        mPreviewActivityBinding.scaleText.text = mTempScale.toFormattedScale()

        initializeSeekBar()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            window.decorView.afterMeasured {

                val displayCutoutCompat =
                    WindowInsetsCompat.toWindowInsetsCompat(window.decorView.rootWindowInsets).displayCutout

                if (Utils.isDeviceLand(resources)) {
                    val lpOptionsCard = mPreviewActivityBinding.seekbarCard.layoutParams as FrameLayout.LayoutParams
                    lpOptionsCard.width = mPreviewActivityBinding.root.width / 2

                    val lpBtnContainer =
                            mPreviewActivityBinding.moveBtnContainer.layoutParams as FrameLayout.LayoutParams
                    lpBtnContainer.setMargins(0, mPreviewActivityBinding.toolbar.height, 0, 0)

                    displayCutoutCompat?.let { dc ->
                        val left = dc.safeInsetLeft
                        if (left != 0) {
                            lpOptionsCard.setMargins(left, 0, left, 0)
                        }
                    }
                } else {

                    val lpToolbar = mPreviewActivityBinding.toolbar.layoutParams as FrameLayout.LayoutParams
                    displayCutoutCompat?.let { dc ->
                        val top = dc.safeInsetTop
                        if (top != 0) {
                            lpToolbar.setMargins(0, top, 0, 0)
                        }
                    }
                }
            }
        }

        mPreviewActivityBinding.root.animate().run {
            duration = 750
            alpha(1.0F)
        }

        setDoubleTapListener()
    }

    private fun initializeSeekBar() {
        //observe SeekBar changes
        mPreviewActivityBinding.run {

            var userProgress = 0F

            seekSize.addOnChangeListener { _, value, fromUser ->
                if (fromUser && value > 0.05F) {
                    vectorView.setScaleFactor(value)
                    userProgress = value
                }
            }

            seekSize.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    sUserIsSeeking = true
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    sUserIsSeeking = false
                    mTempScale = userProgress
                    scaleText.text = mTempScale.toFormattedScale()
                }
            })

            //restore saved scale value
            seekSize.value = mTempScale
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setDoubleTapListener() {
        val gestureDetector = GestureDetector(this, object : SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                onBackPressed()
                return false
            }
        })
        mPreviewActivityBinding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    private fun setWallpaper(set: Boolean) {
        if (PermissionsUtils.hasToAskForReadStoragePermission(this)) {
            PermissionsUtils.manageAskForReadStoragePermission(this, if (set) {
                PermissionsUtils.SAVE_WALLPAPER
            } else {
                PermissionsUtils.SET_WALLPAPER
            })
        } else {
            mPreviewActivityBinding.vectorView.run {
            saveToRecentSetups()
            vectorifyDaHome(set)
            }
        }
    }

    private fun setToolbarAndSeekBarColors() {

        //determine if background color is dark or light and select
        //the appropriate color for UI widgets
        val widgetColor = mTempBackgroundColor.toSurfaceColor()

        val cardColor = ColorUtils.setAlphaComponent(mTempBackgroundColor, 100)

        with(mPreviewActivityBinding) {
            toolbar.run {
                setBackgroundColor(cardColor)
                setTitleTextColor(widgetColor)
                setNavigationIcon(R.drawable.ic_navigate_before)
                inflateMenu(R.menu.toolbar_menu)
                setNavigationOnClickListener { finishAndRemoveTask() }
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.save -> setWallpaper(false)
                        R.id.set -> setWallpaper(true)
                        else -> updatePrefsAndSetLiveWallpaper()
                    }
                    return@setOnMenuItemClickListener true
                }

                //set menu items color according the the background luminance
                menu.run {
                    val drawablesList = children.map { it.icon }.toMutableList().apply {
                        add(navigationIcon)
                    }
                    val iterator = drawablesList.iterator()
                    while (iterator.hasNext()) {
                        iterator.next().mutate().setTint(widgetColor)
                    }
                }
            }

            //set SeekBar colors
            seekbarCard.run {
                setCardBackgroundColor(cardColor)
                strokeColor = ColorUtils.setAlphaComponent(widgetColor, 25)
            }

            seekSize.run {
                val color = ColorStateList.valueOf(widgetColor)
                thumbTintList = color
                trackTintList = color
                trackInactiveTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this@PreviewActivity, R.color.slider_fg))
                haloTintList = color
            }

            seekbarTitle.setTextColor(widgetColor)
            scaleText.setTextColor(widgetColor)

            listOf(
                    up,
                    down,
                    left,
                    right,
                    centerHorizontal,
                    centerVertical,
                    resetPosition
            ).applyTint(this@PreviewActivity, widgetColor)
        }
    }

    private fun updatePrefsAndSetLiveWallpaper() {

        //update prefs
        with(mPreviewActivityBinding.vectorView) {
            saveToPrefs()
            saveToRecentSetups()
        }

        vectorifyPreferences.liveWallpaper = if (Utils.isDeviceLand(resources)) {
            vectorifyPreferences.savedWallpaperLand
        } else {
            vectorifyPreferences.savedWallpaper
        }

        //check if the live wallpaper is already running
        //if so, don't open the live wallpaper picker, just updated preferences
        if (!Utils.isLiveWallpaperRunning(this)) {
            Utils.openLiveWallpaperIntent(this)
        } else {
            Toast.makeText(this, R.string.title_already_live, Toast.LENGTH_LONG).show()
        }
    }

    private fun resetVectorPosition(isResetToDefault: Boolean) {

        mTempScale = 0.35F
        mTempHorizontalOffset = 0F
        mTempVerticalOffset = 0F

        if (!isResetToDefault) {
           val savedWallpaper = if (Utils.isDeviceLand(resources)) {
               vectorifyPreferences.savedWallpaperLand
           } else {
               vectorifyPreferences.savedWallpaper
           }
           with(savedWallpaper) {
               mTempScale = scale
               mTempHorizontalOffset = horizontalOffset
               mTempVerticalOffset = verticalOffset
           }
        }

        mPreviewActivityBinding.run {
            scaleText.text = mTempScale.toFormattedScale()
            vectorView.setScaleFactor(mTempScale)
            seekSize.value = mTempScale
            vectorView.resetPosition(mTempHorizontalOffset, mTempVerticalOffset)
        }
    }

    private suspend fun saveWallpaperAsync(bitmapToProcess: Bitmap, isSetWallpaper: Boolean) : Uri? = withContext(mIoDispatcher) {
        saveWallpaper(cropBitmapFromCenterAndScreenSize(bitmapToProcess), isSetWallpaper)
    }

    //https://stackoverflow.com/a/59536115
    private fun saveWallpaper(bitmap: Bitmap, isSetWallpaper: Boolean) : Uri? {

        return try {

            var fos: OutputStream? = null
            val format = SimpleDateFormat(
                    getString(R.string.time_pattern),
                    Locale.getDefault()
            ).format(Date())

            val name = "${getString(R.string.save_pattern) + format}.png"

            val wallpaperToSave: File

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.run {
                    fos = contentResolver.openOutputStream(Objects.requireNonNull(this))
                }
                val imagesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                wallpaperToSave = File(imagesDir, name)
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                wallpaperToSave = File(imagesDir, name)
                fos = FileOutputStream(wallpaperToSave)
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            Objects.requireNonNull(fos)?.close()

            //refresh media store database
            MediaScannerConnection.scanFile(this, arrayOf(wallpaperToSave.toString()),
                    null, null)

            if (isSetWallpaper) {
                FileProvider.getUriForFile(
                        this,
                        getString(R.string.app_name),
                        wallpaperToSave
                )
            } else {
                null
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    override fun onPause() {
        super.onPause()
        with(VectorifyWallpaper(
                mTempBackgroundColor,
                mTempVectorColor,
                mTempVector,
                mTempCategory,
                mTempScale,
                mTempHorizontalOffset,
                mTempVerticalOffset
        )) {
            if (Utils.isDeviceLand(resources)) {
                vectorifyPreferences.savedWallpaperLand = this
            } else {
                vectorifyPreferences.savedWallpaper = this
            }
        }
    }

    //manage request permission result, continue loading ui if permissions is granted
    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.boo, Toast.LENGTH_LONG).show()
        } else {
            when (requestCode) {
                PermissionsUtils.SAVE_WALLPAPER -> setWallpaper(false)
                PermissionsUtils.SET_WALLPAPER -> setWallpaper(true)
            }
        }
    }

    //immersive mode
    //https://developer.android.com/training/system-ui/immersive
    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //https://stackoverflow.com/a/62643518
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
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

    companion object {
        const val TEMP_BACKGROUND_COLOR = "TEMP_BACKGROUND_COLOR"
        const val TEMP_VECTOR_COLOR = "TEMP_VECTOR_COLOR"
        const val TEMP_VECTOR = "TEMP_VECTOR"
        const val TEMP_CATEGORY = "TEMP_CATEGORY"
        const val TEMP_SCALE = "TEMP_SCALE"
        const val TEMP_H_OFFSET = "TEMP_H_OFFSET"
        const val TEMP_V_OFFSET = "TEMP_V_OFFSET"
    }
}
