package com.iven.vectorify.ui

import android.app.WallpaperManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.slider.Slider
import com.iven.vectorify.*
import com.iven.vectorify.databinding.PreviewActivityBinding
import com.iven.vectorify.models.VectorifyWallpaper
import com.iven.vectorify.utils.SaveWallpaperLoader
import com.iven.vectorify.utils.Utils


class PreviewActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Uri?> {

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
    private lateinit var mSaveImageLoader: Loader<Uri?>

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Uri?> {

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
                this.window?.clearFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                )
            }
        }

        return mSaveImageLoader
    }

    override fun onLoadFinished(loader: Loader<Uri?>, wallpaperUri: Uri?) {

        wallpaperUri?.let { uri ->
            val wallpaperManager = WallpaperManager.getInstance(this)
            try {
                //start crop and set wallpaper intent
                startActivity(wallpaperManager.getCropAndSetWallpaperIntent(uri))
            } catch (e: Throwable) {
                e.printStackTrace()
            }

        }

        LoaderManager.getInstance(this).destroyLoader(SAVE_WALLPAPER_LOADER_ID)

        if (mSaveWallpaperDialog.isShowing) {
            mSaveWallpaperDialog.dismiss()
        }

        Toast.makeText(
            this,
            getString(R.string.message_saved_to, getExternalFilesDir(null)),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onLoaderReset(loader: Loader<Uri?>) {
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAndRemoveTask()
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

        // init click listeners
        mPreviewActivityBinding.run {
            up.setOnClickListener { vectorView.moveUp() }
            down.setOnClickListener { vectorView.moveDown() }
            left.setOnClickListener { vectorView.moveLeft() }
            right.setOnClickListener { vectorView.moveRight() }
            centerHorizontal.setOnClickListener { vectorView.centerHorizontal() }
            centerVertical.setOnClickListener { vectorView.centerVertical() }
            resetPosition.setOnClickListener { resetVectorPosition() }
        }

        //set vector view
        mPreviewActivityBinding.vectorView.updateVectorView(
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

        mPreviewActivityBinding.vectorView.onSetWallpaper = { setWallpaper, bitmap ->
            mSaveImageLoader = SaveWallpaperLoader(this, bitmap, setWallpaper)
            LoaderManager.getInstance(this).initLoader(SAVE_WALLPAPER_LOADER_ID, null, this)
        }

        //match theme with background luminance
        setToolbarAndSeekBarColors()

        initializeSeekBar()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            window.decorView.afterMeasured {
                val top = WindowInsetsCompat.toWindowInsetsCompat(window.decorView.rootWindowInsets).displayCutout?.safeInsetTop
                val lp = mPreviewActivityBinding.toolbar.layoutParams as FrameLayout.LayoutParams
                if (top != 0) {
                    lp.setMargins(0, top!!, 0,0)
                }
            }
        }
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
                }
            })

            //restore saved scale value
            seekSize.value = mTempScale
        }
    }

    private fun setWallpaper(set: Boolean) {
        mPreviewActivityBinding.vectorView.run {
            saveToRecentSetups()
            vectorifyDaHome(set)
        }
    }

    private fun setToolbarAndSeekBarColors() {

        //determine if background color is dark or light and select
        //the appropriate color for UI widgets
        val widgetColor = mTempBackgroundColor.toSurfaceColor(this)

        val cardColor = ColorUtils.setAlphaComponent(mTempBackgroundColor, 100)

        mPreviewActivityBinding.toolbar.run {
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
        mPreviewActivityBinding.seekbarCard.run {
            setCardBackgroundColor(cardColor)
            strokeColor = ColorUtils.setAlphaComponent(widgetColor, 25)
        }

        mPreviewActivityBinding.seekSize.run {
            val color = ColorStateList.valueOf(widgetColor)
            thumbTintList = color
            trackTintList = color
            trackInactiveTintList = ColorStateList.valueOf(mTempBackgroundColor.toForegroundColor(this@PreviewActivity))
            haloTintList = color
        }

        mPreviewActivityBinding.seekbarTitle.setTextColor(widgetColor)

        mPreviewActivityBinding.run {
            listOf(
                aspectRatio,
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

        //check if the live wallpaper is already running
        //if so, don't open the live wallpaper picker, just updated preferences
        if (!Utils.isLiveWallpaperRunning(this)) {
            Utils.openLiveWallpaperIntent(this)
        } else {
            Toast.makeText(this, getString(R.string.title_already_live), Toast.LENGTH_LONG).show()
        }

        //update prefs
        mPreviewActivityBinding.vectorView.run {
            saveToPrefs()
            saveToRecentSetups()
        }
    }

    private fun resetVectorPosition() {
        mTempScale = 0.35F

        vectorifyPreferences.liveVectorifyWallpaper?.let {
            mTempScale = it.scale
        }

        mPreviewActivityBinding.run {
            vectorView.setScaleFactor(mTempScale)
            seekSize.value = mTempScale
            vectorView.resetPosition()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    //immersive mode
    //https://developer.android.com/training/system-ui/immersive
    @Suppress("DEPRECATION")
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

        private const val SAVE_WALLPAPER_LOADER_ID = 25

        internal const val TEMP_BACKGROUND_COLOR = "TEMP_BACKGROUND_COLOR"
        internal const val TEMP_VECTOR_COLOR = "TEMP_VECTOR_COLOR"
        internal const val TEMP_VECTOR = "TEMP_VECTOR"
        internal const val TEMP_CATEGORY = "TEMP_CATEGORY"
        internal const val TEMP_SCALE = "TEMP_SCALE"
        internal const val TEMP_H_OFFSET = "TEMP_H_OFFSET"
        internal const val TEMP_V_OFFSET = "TEMP_V_OFFSET"
    }
}
