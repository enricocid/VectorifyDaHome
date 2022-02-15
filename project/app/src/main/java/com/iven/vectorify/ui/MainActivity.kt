package com.iven.vectorify.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.iven.vectorify.*
import com.iven.vectorify.adapters.PresetsAdapter
import com.iven.vectorify.adapters.VectorsAdapter
import com.iven.vectorify.databinding.MainActivityBinding
import com.iven.vectorify.models.Metrics
import com.iven.vectorify.models.VectorifyWallpaper
import com.iven.vectorify.ui.PreviewActivity.Companion.TEMP_BACKGROUND_COLOR
import com.iven.vectorify.ui.PreviewActivity.Companion.TEMP_CATEGORY
import com.iven.vectorify.ui.PreviewActivity.Companion.TEMP_H_OFFSET
import com.iven.vectorify.ui.PreviewActivity.Companion.TEMP_SCALE
import com.iven.vectorify.ui.PreviewActivity.Companion.TEMP_VECTOR
import com.iven.vectorify.ui.PreviewActivity.Companion.TEMP_VECTOR_COLOR
import com.iven.vectorify.ui.PreviewActivity.Companion.TEMP_V_OFFSET
import com.iven.vectorify.utils.Utils
import com.maxkeppeler.sheets.color.ColorSheet
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.windowInsetTypesOf

private const val TAG_BG_COLOR_RESTORE = "TAG_BG_COLOR_RESTORE"
private const val TAG_VECTOR_COLOR_RESTORE = "TAG_VECTOR_COLOR_RESTORE"
private const val TAG_VECTOR_RESTORE = "TAG_VECTOR_RESTORE"
private const val TAG_CATEGORY_RESTORE = "TAG_CATEGORY_RESTORE"
private const val TAG_SCALE_RESTORE = "TAG_SCALE_RESTORE"
private const val TAG_H_OFFSET_RESTORE = "TAG_H_OFFSET_RESTORE"
private const val TAG_V_OFFSET_RESTORE = "TAG_V_OFFSET_RESTORE"


class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener, ModalSheet.ModalSheetCallback {

    // View binding class
    private lateinit var mMainActivityBinding: MainActivityBinding

    private lateinit var mVectorsRecyclerViewLayoutManager: LinearLayoutManager
    private lateinit var mVectorsAdapter: VectorsAdapter

    private var mTempBackgroundColor = Color.BLACK
    private var mTempVectorColor = Color.WHITE
    private var mTempVector = R.drawable.android_logo_2019
    private var mTempCategory = 0
    private var mTempScale = 0.35F
    private var mTempHorizontalOffset = 0F
    private var mTempVerticalOffset = 0F

    private val sSwapColor get() = mTempVectorColor != mTempBackgroundColor

    private var sThemeChanged = false
    private var sRestoreVector = false

    override fun onSaveInstanceState(outState: Bundle) {
        if (sThemeChanged) {
            super.onSaveInstanceState(outState)
            with(outState) {
                putInt(TAG_BG_COLOR_RESTORE, mTempBackgroundColor)
                putInt(TAG_VECTOR_COLOR_RESTORE, mTempVectorColor)
                putInt(TAG_VECTOR_RESTORE, mTempVector)
                putInt(TAG_CATEGORY_RESTORE, mTempCategory)
                putFloat(TAG_SCALE_RESTORE, mTempScale)
                putFloat(TAG_H_OFFSET_RESTORE, mTempHorizontalOffset)
                putFloat(TAG_V_OFFSET_RESTORE, mTempVerticalOffset)
            }
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            getString(R.string.theme_key) -> sThemeChanged = true
            getString(R.string.saved_wallpaper_key) -> onWallpaperPrefChanged(vectorifyPreferences.savedWallpaper)
            getString(R.string.saved_wallpaper_land_key) -> onWallpaperPrefChanged(vectorifyPreferences.savedWallpaperLand)
        }
    }

    private fun onWallpaperPrefChanged(wallpaper: VectorifyWallpaper) {
        //get wallpaper from prefs
        with(wallpaper) {
            mTempBackgroundColor = backgroundColor
            mTempVectorColor = vectorColor
            mTempVector = resource
            mTempCategory = category
            mTempScale = scale
            mTempHorizontalOffset = horizontalOffset
            mTempVerticalOffset = verticalOffset
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMainActivityBinding = MainActivityBinding.inflate(layoutInflater)

        setContentView(mMainActivityBinding.root)

        getDisplayMetrics()

        savedInstanceState?.let { bundle ->
            sRestoreVector = true
            mTempBackgroundColor = bundle.getInt(TAG_BG_COLOR_RESTORE)
            mTempVectorColor = bundle.getInt(TAG_VECTOR_COLOR_RESTORE)
            mTempVector = bundle.getInt(TAG_VECTOR_RESTORE)
            mTempCategory = bundle.getInt(TAG_CATEGORY_RESTORE)
            mTempScale = bundle.getFloat(TAG_SCALE_RESTORE)
            mTempHorizontalOffset = bundle.getFloat(TAG_H_OFFSET_RESTORE)
            mTempVerticalOffset = bundle.getFloat(TAG_V_OFFSET_RESTORE)
        }

        with(if (Utils.isDeviceLand(resources)) {
            vectorifyPreferences.savedWallpaperLand
        } else {
            vectorifyPreferences.savedWallpaper
        }) {
            onWallpaperPrefChanged(this)
        }

        initViews()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            window?.navigationBarColor = ContextCompat.getColor(this, R.color.bottom_bar_color)
            Insetter.builder()
                .padding(windowInsetTypesOf(navigationBars = true))
                .margin(windowInsetTypesOf(statusBars = true))
                .applyToView(mMainActivityBinding.root)
        }

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    private fun initViews() {

        //set all click listeners
        mMainActivityBinding.run {

            categoriesChip.setOnClickListener { startCategoryChooser() }
            backgroundColorPicker.safeClickListener {
                startColorPicker(
                    getString(R.string.background_color_key),
                    R.string.title_background_dialog
                )
            }

            accentBackground.setOnClickListener {
                setBackgroundColorForUI(
                    Utils.getSystemAccentColor(
                        this@MainActivity
                    ), true
                )
            }

            vectorColorPicker.safeClickListener {
                startColorPicker(
                    getString(R.string.vectors_color_key),
                    R.string.title_vector_dialog
                )
            }

            accentVector.setOnClickListener {
                setVectorColorForUI(Utils.getSystemAccentColor(this@MainActivity), true)
            }

            swapCardColors.setOnClickListener { swapBtn ->
                if (sSwapColor) {
                    ObjectAnimator.ofFloat(
                        swapBtn,
                        View.ROTATION,
                        0f,
                        180f
                    ).apply {
                        duration = 500
                        start()
                        doOnEnd {
                            val tempBackgroundColorBackup = mTempBackgroundColor
                            setBackgroundColorForUI(mTempVectorColor, true)
                            setVectorColorForUI(tempBackgroundColorBackup, true)
                        }
                    }
                }
            }
        }

        //update background card color and text from preferences
        setBackgroundColorForUI(mTempBackgroundColor, false)

        //update vector card color and text from preferences
        setVectorColorForUI(mTempVectorColor, false)

        setupFabButtonClick()
        setupBottomBar()
        setupRecyclerViews()
        updateSelectedCategory(mTempCategory, true)
    }

    @Suppress("DEPRECATION")
    private fun getDisplayMetrics() {
        //retrieve display specifications
        val window = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display
        } else {
            window.defaultDisplay
        }
        display?.let { d ->
            d.getRealMetrics(dm)
            vectorifyPreferences.savedMetrics = Metrics(dm.widthPixels, dm.heightPixels)
        }
    }

    private fun setupFabButtonClick() {
        mMainActivityBinding.fab.setOnClickListener {
            //start preview activity
            val intent = Intent(this@MainActivity, PreviewActivity::class.java).apply {
                putExtras(
                    bundleOf(
                        TEMP_BACKGROUND_COLOR to mTempBackgroundColor,
                        TEMP_VECTOR_COLOR to mTempVectorColor,
                        TEMP_VECTOR to mTempVector,
                        TEMP_CATEGORY to mTempCategory,
                        TEMP_SCALE to mTempScale,
                        TEMP_H_OFFSET to mTempHorizontalOffset,
                        TEMP_V_OFFSET to mTempVerticalOffset
                    )
                )
            }
            startActivity(intent)
        }
    }

    private fun setupBottomBar() {
        //set the bottom bar menu
        mMainActivityBinding.bar.run {

            replaceMenu(R.menu.bottom_menu)

            val menuThemeItem = menu.findItem(R.id.app_bar_theme).apply {
                icon = ContextCompat.getDrawable(this@MainActivity, Utils.getDefaultNightModeIcon(this@MainActivity))
            }

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.app_bar_info -> Utils.openCustomTab(this@MainActivity)
                    R.id.app_bar_theme -> {

                        vectorifyPreferences.theme =
                            Utils.getNextDefaultNightMode(this@MainActivity)

                        AppCompatDelegate.setDefaultNightMode(
                            Utils.getDefaultNightMode(
                                this@MainActivity
                            )
                        )

                        menuThemeItem.icon =
                            ContextCompat.getDrawable(this@MainActivity, Utils.getDefaultNightModeIcon(this@MainActivity))
                    }

                    R.id.app_bar_restore -> restoreDefaultWallpaper()
                }
                return@setOnMenuItemClickListener true
            }

            setNavigationOnClickListener {
                if (Utils.isDeviceLand(resources) && !vectorifyPreferences.recentSetupsLand.isNullOrEmpty() || !Utils.isDeviceLand(resources) && !vectorifyPreferences.recentSetups.isNullOrEmpty()) {
                    ModalSheet.newInstance().show(supportFragmentManager, ModalSheet.TAG_MODAL)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.message_no_recent_setups,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            afterMeasured {
                mMainActivityBinding.run {
                    val lp = version.layoutParams as CoordinatorLayout.LayoutParams
                    lp.setMargins(0, 0, 0, height)
                    cardsContainer.setPadding(
                        0,
                        0,
                        0,
                        height + version.height
                    )
                    root.animate().run {
                        duration = 750
                        alpha(1.0F)
                    }
                }
            }
        }
    }

    private fun setupRecyclerViews() {

        //setup presets
        mMainActivityBinding.presetsRv.run {

            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.HORIZONTAL, false)
            setHasFixedSize(true)

            val presetsAdapter = PresetsAdapter(this@MainActivity).apply {
                onPresetClick = { combo ->

                    //update background and vector colors
                    setBackgroundColorForUI(
                        ContextCompat.getColor(this@MainActivity, combo.first),
                        true
                    )
                    setVectorColorForUI(
                        ContextCompat.getColor(this@MainActivity, combo.second),
                        true
                    )

                    //update vector frame colors
                    setVectorFrameColors(true)
                }
            }
            adapter = presetsAdapter
        }

        //setup vectors
        mMainActivityBinding.vectorsRv.run {

            mVectorsRecyclerViewLayoutManager =
                GridLayoutManager(this@MainActivity, 2, GridLayoutManager.HORIZONTAL, false)
            layoutManager = mVectorsRecyclerViewLayoutManager
            setHasFixedSize(true)

            mVectorsAdapter = VectorsAdapter(this@MainActivity).apply {
                onVectorClick = { vector ->
                    if (!sRestoreVector) {
                        if (mTempVector != vector) {

                            mTempVector = vector

                            mMainActivityBinding.vectorFrame.setImageResource(
                                Utils.getVectorProps(
                                    vector
                                ).first
                            )

                            //update drawable tint
                            setVectorFrameColors(false)
                        }
                    } else {
                        sRestoreVector = false
                    }
                }

                @SuppressLint("DefaultLocale")
                onVectorLongClick = { vector ->
                    val iconName = resources.getResourceEntryName(vector)
                        .replace(
                            getString(R.string.underscore_delimiter),
                            getString(R.string.space_delimiter)
                        )
                        .replaceFirstChar(Char::uppercase)
                    Toast.makeText(this@MainActivity, iconName, Toast.LENGTH_LONG).show()
                }
            }
            adapter = mVectorsAdapter
        }
    }

    //update UI on recent selected
    override fun onRecentSelected(
        selectedBackgroundColor: Int,
        selectedVectorColor: Int,
        selectedVector: Int,
        selectedCategory: Int,
        selectedScale: Float,
        selectedHorizontalOffset: Float,
        selectedVerticalOffset: Float
    ) {

        updateFabColor()
        setBackgroundColorForUI(selectedBackgroundColor, true)
        setVectorColorForUI(selectedVectorColor, true)

        updateSelectedCategory(selectedCategory, false)

        scrollToVector(selectedVector)

        mTempScale = selectedScale
        mTempHorizontalOffset = selectedHorizontalOffset
        mTempVerticalOffset = selectedVerticalOffset
    }

    //update vector frame
    private fun setVectorFrameColors(tintBackground: Boolean) {

        if (tintBackground) {
            mMainActivityBinding.vectorFrame.setBackgroundColor(mTempBackgroundColor)
        }

        val vector = Utils.tintDrawable(
            this,
            mTempVector,
            mTempVectorColor.toContrastColor(mTempBackgroundColor)
        )
        mMainActivityBinding.vectorFrame.setImageDrawable(vector)
    }

    //update background card colors
    private fun setBackgroundColorForUI(color: Int, updateColor: Boolean) {

        if (updateColor) {
            mTempBackgroundColor = color
        }

        if (mTempBackgroundColor == mTempVectorColor) {
            setVectorColorForUI(mTempVectorColor, false)
        }
        val textColor = mTempBackgroundColor.toSurfaceColor()

        //update shit colors
        mMainActivityBinding.backgroundColor.setCardBackgroundColor(mTempBackgroundColor)

        mMainActivityBinding.backgroundColorHead.setTextColor(textColor)
        mMainActivityBinding.backgroundColorSubhead.run {
            setTextColor(textColor)
            text = color.toHex(this@MainActivity)
            setTextColor(textColor)
        }

        mMainActivityBinding.fab.backgroundTintList =
            ColorStateList.valueOf(mTempBackgroundColor)

        //check if colors are the same so we make vector color more visible
        updateFabColor()

        //update vector frame colors
        setVectorFrameColors(true)
    }

    //update vector card colors
    private fun setVectorColorForUI(color: Int, updateColor: Boolean) {

        if (updateColor) {
            mTempVectorColor = color
        }

        val textColor = mTempVectorColor.toSurfaceColor()

        //update shit colors
        mMainActivityBinding.vectorColor.setCardBackgroundColor(
            mTempVectorColor.toContrastColor(
                mTempBackgroundColor
            )
        )

        mMainActivityBinding.vectorColorHead.setTextColor(textColor)
        mMainActivityBinding.vectorColorSubhead.run {
            setTextColor(textColor)
            text = mTempVectorColor.toHex(this@MainActivity)
        }
        setVectorFrameColors(false)
        updateFabColor()
    }

    private fun updateFabColor() {

        mMainActivityBinding.fab.backgroundTintList = ColorStateList.valueOf(mTempBackgroundColor)
        mMainActivityBinding.fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check))

        //check if colors are the same so we enable stroke to make vector visible
        val fabDrawableColor = mTempVectorColor.toContrastColor(mTempBackgroundColor)
        mMainActivityBinding.fab.drawable.setTint(fabDrawableColor)
    }

    private fun scrollToVector(vector: Int) {
        mVectorsAdapter.onVectorClick?.invoke(vector)
        mVectorsAdapter.swapSelectedDrawable(mTempVector)
        mMainActivityBinding.vectorsRv.scrollToPosition(
            mVectorsAdapter.getVectorPosition(
                mTempVector
            )
        )
    }

    //restore default wallpaper
    private fun restoreDefaultWallpaper() {

        setBackgroundColorForUI(Color.BLACK, true)
        setVectorColorForUI(Color.WHITE, true)

        updateSelectedCategory(0, false)
        scrollToVector(R.drawable.android_logo_2019)

        mTempScale = 0.35F
        mTempHorizontalOffset = 0F
        mTempVerticalOffset = 0F
    }

    //start material dialog
    private fun startColorPicker(key: String, title: Int) {
        ColorSheet().show(this) {
            disableAlpha()
            colorsRes(Utils.colors)
            title(title)
            onPositive(android.R.string.ok) { color ->
                when (key) {
                    getString(R.string.background_color_key) -> {
                        //update the color only if it really changed
                        if (mTempBackgroundColor != color) {
                            setBackgroundColorForUI(color, true)
                        }
                    }
                    else -> {
                        //update the color only if it really changed
                        if (mTempVectorColor != color) {
                            setVectorColorForUI(color, true)
                        }
                    }
                }
            }
            onNegative(android.R.string.cancel)
        }
    }

    //method to start categories chooser
    private fun startCategoryChooser() {
        val items = resources.getStringArray(R.array.categories)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.title_categories)
            .setItems(items) { _, which ->
                // Respond to item chosen
                updateSelectedCategory(which, false)
            }
            .show()
    }

    private fun updateSelectedCategory(index: Int, force: Boolean) {
        if (mTempCategory != index && !force) {
            mTempCategory = index
            mMainActivityBinding.vectorsRv.scrollToPosition(0)
        }
        val category = Utils.getCategory(this, mTempCategory)
        mVectorsAdapter.swapCategory(category.second)
        mMainActivityBinding.categoriesChip.run {
            text = category.first
            contentDescription = getString(R.string.content_selected_category, category.first)
        }
        if (force) {
            scrollToVector(mTempVector)
        }
    }
}
