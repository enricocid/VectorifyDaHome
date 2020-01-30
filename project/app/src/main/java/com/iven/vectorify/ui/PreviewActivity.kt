package com.iven.vectorify.ui

import android.app.WallpaperManager
import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.iven.vectorify.*
import com.iven.vectorify.utils.SaveWallpaperLoader
import com.iven.vectorify.utils.Utils
import kotlinx.android.synthetic.main.position_controls.*
import kotlinx.android.synthetic.main.preview_activity.*
import kotlinx.android.synthetic.main.size_position_card.*

@Suppress("UNUSED_PARAMETER")
class PreviewActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Uri?> {

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

    private lateinit var mSaveWallpaperDialog: MaterialDialog
    private lateinit var mSaveImageLoader: Loader<Uri?>

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Uri?> {

        mSaveWallpaperDialog = MaterialDialog(this).apply {
            title(R.string.live_wallpaper_name)
            customView(R.layout.progress_dialog)
            cancelOnTouchOutside(false)
            cancelable(false)
            this.window?.apply {
                setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                )
                decorView.systemUiVisibility =
                    this@PreviewActivity.window.decorView.systemUiVisibility
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

        if (mSaveWallpaperDialog.isShowing) mSaveWallpaperDialog.dismiss()

        getString(R.string.message_saved_to, getExternalFilesDir(null)).toColouredToast(
            this,
            ContextCompat.getDrawable(this, R.drawable.ic_check),
            mTempVectorColor,
            mTempBackgroundColor
        )
    }

    override fun onLoaderReset(loader: Loader<Uri?>) {
    }

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
            R.id.save -> setWallpaper(false)
            R.id.set -> setWallpaper(true)
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
                mTempVectorColor.toContrastColor(mTempBackgroundColor),
                mTempVector,
                mTempCategory,
                mTempScale,
                mTempHorizontalOffset,
                mTempVerticalOffset
            )
        )

        mVectorView.onSetWallpaper = { setWallpaper, bitmap ->
            mSaveImageLoader = SaveWallpaperLoader(this, bitmap, setWallpaper)
            LoaderManager.getInstance(this).initLoader(SAVE_WALLPAPER_LOADER_ID, null, this)
        }

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
        mVectorView.saveToRecentSetups()
        mVectorView.vectorifyDaHome(set)
    }

    private fun setToolbarAndSeekBarColors() {

        //determine if background color is dark or light and select
        //the appropriate color for UI widgets
        val widgetColor = mTempBackgroundColor.toSurfaceColor()

        val cardColor = ColorUtils.setAlphaComponent(mTempBackgroundColor, 100)

        mToolbar.apply {
            setBackgroundColor(cardColor)
            setTitleTextColor(widgetColor)
            setNavigationIcon(R.drawable.ic_navigate_before)
        }

        //set SeekBar colors
        seekbar_card.apply {
            setCardBackgroundColor(cardColor)
            strokeColor = ColorUtils.setAlphaComponent(widgetColor, 25)
        }

        mSeekBar.apply {
            progressTintList = ColorStateList.valueOf(widgetColor)
            thumbTintList = ColorStateList.valueOf(widgetColor)
            progressBackgroundTintList = ColorStateList.valueOf(widgetColor)
        }

        seekbar_title.setTextColor(widgetColor)
        mSeekBarScale.setTextColor(widgetColor)

        listOf<ImageButton>(
            up,
            down,
            left,
            right,
            center_horizontal,
            center_vertical,
            reset_position
        ).applyTint(this, widgetColor)
    }

    private fun updatePrefsAndSetLiveWallpaper() {

        //check if the live wallpaper is already running
        //if so, don't open the live wallpaper picker, just updated preferences
        if (!Utils.isLiveWallpaperRunning(this)) Utils.openLiveWallpaperIntent(this)
        else getString(R.string.title_already_live).toColouredToast(
            this,
            ContextCompat.getDrawable(this, R.drawable.ic_check),
            mTempVectorColor,
            mTempBackgroundColor
        )

        //update prefs
        mVectorView.saveToPrefs()
        mVectorView.saveToRecentSetups()
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
