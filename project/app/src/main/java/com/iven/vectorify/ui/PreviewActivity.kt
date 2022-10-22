package com.iven.vectorify.ui

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.ColorUtils
import androidx.core.view.*
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

    private val mHandler = CoroutineExceptionHandler { _, exception ->
        exception.printStackTrace()
    }

    private val mUiDispatcher = Dispatchers.Main
    private val mIoDispatcher = Dispatchers.IO + mHandler
    private val mUiScope = CoroutineScope(mUiDispatcher)

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finishAndRemoveTask()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        mPreviewActivityBinding = PreviewActivityBinding.inflate(layoutInflater)
        setContentView(mPreviewActivityBinding.root)

        //set immersive mode
        hideSystemBars()

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

            mPreviewActivityBinding.progressIndicator.show()
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

                    if (mPreviewActivityBinding.progressIndicator.isVisible) {
                        mPreviewActivityBinding.progressIndicator.hide()
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

        //match theme with background luminance
        setToolbarAndSeekBarColors()

        mPreviewActivityBinding.scaleText.text = mTempScale.toFormattedScale()

        initializeSeekBar()

        window.decorView.afterMeasured {
            if (Utils.isDeviceLand(resources)) {
                mPreviewActivityBinding.seekbarCard.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    width = mPreviewActivityBinding.root.width / 2
                }
            }
            updateLayoutForDisplayCutoutIfNeeded(this)
        }

        mPreviewActivityBinding.root.animate().run {
            duration = 750
            alpha(1.0F)
        }

        setDoubleTapListener()
    }

    private fun updateLayoutForDisplayCutoutIfNeeded(view: View) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return
        val dc =
            WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets).displayCutout ?: return

        with(mPreviewActivityBinding) {

            // expand toolbar background to the top edge
            toolbar.setPadding(0, dc.safeInsetTop, 0, 0)

            toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                height += dc.safeInsetTop
            }

            val newToolbarHeight = toolbar.layoutParams.height

            // update progress bar position
            progressIndicator.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = newToolbarHeight
            }

            // update size card position
            seekbarCard.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin += dc.safeInsetBottom
            }

            if (Utils.isDeviceLand(resources)) {
                // update move button container position
                moveBtnContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = newToolbarHeight
                    rightMargin += dc.safeInsetRight
                }
                // prevent toolbar buttons form being cutout
                toolbar.setPadding(dc.safeInsetLeft, dc.safeInsetTop, dc.safeInsetRight, 0)
            }
        }
    }

    @SuppressLint("RestrictedApi")
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
                    if (sUserIsSeeking) {
                        sUserIsSeeking = false
                        mTempScale = userProgress
                        scaleText.text = mTempScale.toFormattedScale()
                    }
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
                onBackPressedCallback.handleOnBackPressed()
                return false
            }
        })
        mPreviewActivityBinding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    private fun setWallpaper(set: Boolean) {
        if (PermissionsUtils.hasToAskForReadStoragePermission(this)) {
            var kind = PermissionsUtils.SET_WALLPAPER
            if (set) {
                kind = PermissionsUtils.SAVE_WALLPAPER
            }
            PermissionsUtils.manageAskForReadStoragePermission(this, kind)
            return
        }
        mPreviewActivityBinding.vectorView.run {
            saveToRecentSetups()
            vectorifyDaHome(set)
        }
    }

    private fun setToolbarAndSeekBarColors() {

        //determine if background color is dark or light and select
        //the appropriate color for UI widgets
        val cardColor = ColorUtils.setAlphaComponent(mTempBackgroundColor, 100)
        val vectorColor = mTempBackgroundColor.toSurfaceColor()
        val vectorColorAlpha = ColorUtils.setAlphaComponent(vectorColor, 25)

        with(mPreviewActivityBinding) {
            toolbar.run {
                setBackgroundColor(cardColor)
                setTitleTextColor(vectorColor)
                setNavigationIcon(R.drawable.ic_navigate_before)
                inflateMenu(R.menu.toolbar_menu)
                setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
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
                        iterator.next()?.mutate()?.setTint(vectorColor)
                    }
                }
            }

            progressIndicator.run {
                setIndicatorColor(vectorColor)
                trackColor = vectorColorAlpha
            }

            //set SeekBar colors
            seekbarCard.run {
                setCardBackgroundColor(cardColor)
                strokeColor = vectorColorAlpha
            }

            seekSize.run {
                val color = ColorStateList.valueOf(vectorColor)
                thumbTintList = color
                trackTintList = color
                trackInactiveTintList = ColorStateList.valueOf(vectorColorAlpha)
                haloTintList = color
            }

            seekbarTitle.setTextColor(vectorColor)
            scaleText.setTextColor(vectorColor)

            listOf(up, down, left, right, centerHorizontal, centerVertical, resetPosition)
                .applyTint(this@PreviewActivity, vectorColor)
        }
    }

    private fun getWallpaperToSave() = if (Utils.isDeviceLand(resources)) {
        vectorifyPreferences.savedWallpaperLand
    } else {
        vectorifyPreferences.savedWallpaper
    }

    private fun updatePrefsAndSetLiveWallpaper() {

        //update prefs
        with(mPreviewActivityBinding.vectorView) {
            saveToPrefs()
            saveToRecentSetups()
        }

        vectorifyPreferences.liveWallpaper = getWallpaperToSave()

        //check if the live wallpaper is already running
        //if so, don't open the live wallpaper picker, just updated preferences
        if (!Utils.isLiveWallpaperRunning(this)) {
            Utils.openLiveWallpaperIntent(this)
            return
        }
        Toast.makeText(this, R.string.title_already_live, Toast.LENGTH_LONG).show()
    }

    private fun resetVectorPosition(isResetToDefault: Boolean) {

        mTempScale = 0.35F
        mTempHorizontalOffset = 0F
        mTempVerticalOffset = 0F

        if (!isResetToDefault) {
           val savedWallpaper = getWallpaperToSave()
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
        doSave(cropBitmapFromCenterAndScreenSize(bitmapToProcess), isSetWallpaper)
    }

    //https://stackoverflow.com/a/59536115
    private fun doSave(bitmap: Bitmap, isSetWallpaper: Boolean) : Uri? {

        return try {

            var fos: OutputStream? = null
            val format = SimpleDateFormat(
                getString(R.string.time_pattern),
                Locale.getDefault()
            ).format(Date())

            val name = "${getString(R.string.save_pattern) + format}.png"

            var returnUri = Uri.EMPTY

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                    returnUri = uri
                    fos = contentResolver.openOutputStream(Objects.requireNonNull(returnUri))
                }

            } else {

                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val wallpaperToSave = File(imagesDir, name)

                returnUri = FileProvider.getUriForFile(
                    this,
                    getString(R.string.app_name),
                    wallpaperToSave
                )
                fos = FileOutputStream(wallpaperToSave)
                //refresh media store database
                MediaScannerConnection.scanFile(this, arrayOf(wallpaperToSave.toString()),
                    null, null)
            }

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos?.flush()
            fos?.close()

            if (isSetWallpaper) returnUri else null
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
        var bitmapNewHeight = deviceHeight
        var bitmapNewWidth = (bitmapNewHeight * bitmapRatio).toInt()

        if (screenRatio > bitmapRatio) {
            bitmapNewWidth = deviceWidth
            bitmapNewHeight = (bitmapNewWidth / bitmapRatio).toInt()
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
            hideSystemBars()
        }
    }

    override fun onPause() {
        super.onPause()
        val toSave = VectorifyWallpaper(mTempBackgroundColor, mTempVectorColor, mTempVector,
            mTempCategory, mTempScale, mTempHorizontalOffset, mTempVerticalOffset
        )
        if (Utils.isDeviceLand(resources)) {
            vectorifyPreferences.savedWallpaperLand = toSave
            return
        }
        vectorifyPreferences.savedWallpaper = toSave
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
            return
        }
        when (requestCode) {
            PermissionsUtils.SAVE_WALLPAPER -> setWallpaper(false)
            PermissionsUtils.SET_WALLPAPER -> setWallpaper(true)
        }
    }

    //immersive mode
    //https://developer.android.com/training/system-ui/immersive
    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).run {
            // Configure the behavior of the hidden system bars
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            // Hide both the status bar and the navigation bar
            hide(WindowInsetsCompat.Type.systemBars())
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
