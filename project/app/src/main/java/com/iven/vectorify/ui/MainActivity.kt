package com.iven.vectorify.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.color.ColorPalette
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.list.customListAdapter
import com.afollestad.materialdialogs.list.getRecyclerView
import com.afollestad.materialdialogs.list.listItems
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.iven.vectorify.*
import com.iven.vectorify.adapters.PresetsAdapter
import com.iven.vectorify.adapters.RecentsAdapter
import com.iven.vectorify.adapters.VectorsAdapter
import com.iven.vectorify.utils.Utils
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import de.halfbit.edgetoedge.Edge
import de.halfbit.edgetoedge.edgeToEdge
import kotlinx.android.synthetic.main.background_color_pref_card.*
import kotlinx.android.synthetic.main.cards_container.*
import kotlinx.android.synthetic.main.presets_card.*
import kotlinx.android.synthetic.main.vector_color_pref_card.*
import kotlinx.android.synthetic.main.vectorify_activity.*
import kotlinx.android.synthetic.main.vectors_card.*

@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity(R.layout.vectorify_activity),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var mFab: FloatingActionButton
    private lateinit var mVectorFrame: ImageView
    private lateinit var mBottomBar: BottomAppBar
    private lateinit var mCategoriesChip: Chip
    private lateinit var mVectorsRecyclerView: RecyclerView
    private lateinit var mVectorsRecyclerViewLayoutManager: LinearLayoutManager
    private lateinit var mVectorsAdapter: VectorsAdapter

    private lateinit var mRecentSetupsDialog: MaterialDialog

    private val mBackupRecent = vectorifyPreferences.vectorifyWallpaperBackup

    private var mTempBackgroundColor = mBackupRecent.backgroundColor
    private var mTempVectorColor = mBackupRecent.vectorColor
    private var mTempVector = mBackupRecent.resource
    private var mTempCategory = mBackupRecent.category
    private var mTempScale = mBackupRecent.scale
    private var mTempHorizontalOffset = mBackupRecent.horizontalOffset
    private var mTempVerticalOffset = mBackupRecent.verticalOffset

    private var sSaveLatestSetup = true

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(R.string.recent_vectorify_wallpapers_key) && vectorifyPreferences.vectorifyWallpaperSetups?.isNullOrEmpty()!! && ::mRecentSetupsDialog.isInitialized && mRecentSetupsDialog.isShowing)
            mRecentSetupsDialog.dismiss()
    }

    override fun onResume() {
        super.onResume()
        sSaveLatestSetup = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getViewsAndResources()

        //update background card color and text from preferences
        setBackgroundColorForUI(mTempBackgroundColor)

        //update vector card color and text from preferences
        setVectorColorForUI(mTempVectorColor)

        setupFabButton()

        swap_card_colors.setOnClickListener { swapBtn ->
            ObjectAnimator.ofFloat(swapBtn, View.ROTATION, 0f, 180f).apply {
                duration = 500
                start()
                doOnEnd {
                    val tempBackgroundColorBackup = mTempBackgroundColor
                    setBackgroundColorForUI(mTempVectorColor)
                    setVectorColorForUI(tempBackgroundColorBackup)
                }
            }
        }

        setupBottomBar()

        setupRecyclerViews()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            window?.apply {
                Utils.handleLightSystemBars(this@MainActivity, this, decorView, false)
                edgeToEdge {
                    decorView.fit { Edge.Top }
                    mBottomBar.fit { Edge.Bottom + Edge.Right }
                    mFab.apply {
                        fit { Edge.Right }
                        fitPadding { Edge.Bottom }
                    }
                }
            }
        }
    }

    private fun getViewsAndResources() {

        //get wallpaper shiz
        val latestSetup =
            if (vectorifyPreferences.savedVectorifyWallpaper != null) vectorifyPreferences.savedVectorifyWallpaper else vectorifyPreferences.vectorifyWallpaperBackup

        //init temp preferences
        latestSetup?.let { setup ->
            mTempBackgroundColor = setup.backgroundColor
            mTempVectorColor = setup.vectorColor
            mTempVector = setup.resource
            mTempCategory = setup.category
            mTempScale = setup.scale
            mTempHorizontalOffset = setup.horizontalOffset
            mTempVerticalOffset = setup.verticalOffset
        }

        mVectorFrame = vector_frame
        mCategoriesChip = categories_chip
        mVectorsRecyclerView = vectors_rv
        mBottomBar = bar
        mFab = fab
    }

    private fun setupFabButton() {
        //get the fab (don't move from this position)
        mFab.apply {
            setOnClickListener {

                sSaveLatestSetup = false

                //start preview activity
                val intent = Intent(this@MainActivity, PreviewActivity::class.java).apply {
                    putExtras(Bundle().apply {
                        putInt(TEMP_BACKGROUND_COLOR, mTempBackgroundColor)
                        putInt(TEMP_VECTOR_COLOR, mTempVectorColor)
                        putInt(TEMP_VECTOR, mTempVector)
                        putInt(TEMP_CATEGORY, mTempCategory)
                        putFloat(TEMP_SCALE, mTempScale)
                        putFloat(TEMP_H_OFFSET, mTempHorizontalOffset)
                        putFloat(TEMP_V_OFFSET, mTempVerticalOffset)
                    })
                }
                startActivity(intent)
            }
        }

    }

    private fun setupBottomBar() {
        //set the bottom bar menu
        mBottomBar.apply {

            replaceMenu(R.menu.bottom_menu)
            menu.findItem(R.id.app_bar_restore).title = getString(
                R.string.title_reset
            )
            val menuThemeItem = menu.findItem(R.id.app_bar_theme).apply {
                icon = ContextCompat.getDrawable(
                    this@MainActivity,
                    Utils.getDefaultNightModeIcon(this@MainActivity)
                )
            }

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.app_bar_info -> Utils.openCustomTab(this@MainActivity)
                    R.id.app_bar_theme -> {

                        vectorifyPreferences.theme =
                            Utils.getProgressiveDefaultNightMode(this@MainActivity)

                        AppCompatDelegate.setDefaultNightMode(
                            Utils.getDefaultNightMode(
                                this@MainActivity
                            )
                        )

                        menuThemeItem.icon = ContextCompat.getDrawable(
                            this@MainActivity,
                            Utils.getDefaultNightModeIcon(this@MainActivity)
                        )
                    }

                    R.id.app_bar_restore -> showOptionsPopup(
                        findViewById(
                            R.id.app_bar_restore
                        )
                    )
                }
                return@setOnMenuItemClickListener true
            }

            setNavigationOnClickListener {
                if (!vectorifyPreferences.vectorifyWallpaperSetups.isNullOrEmpty())
                    openRecentSetups()
                else
                    DynamicToast.makeWarning(
                        this@MainActivity,
                        getString(R.string.message_no_recent_setups)
                    )
                        .show()
            }

            afterMeasured {
                val version = version
                val lp = version.layoutParams as CoordinatorLayout.LayoutParams
                lp.setMargins(0, 0, 0, height)
                version.layoutParams = lp
                cards_container.setPadding(0, 0, 0, height + version.height)
            }
        }
    }

    private fun setupRecyclerViews() {

        //setup presets
        presets_rv.apply {

            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.HORIZONTAL, false)
            setHasFixedSize(true)

            val presetsAdapter = PresetsAdapter(this@MainActivity).apply {
                onPresetClick = { combo ->

                    //update background and vector colors
                    setBackgroundColorForUI(ContextCompat.getColor(this@MainActivity, combo.first))
                    setVectorColorForUI(ContextCompat.getColor(this@MainActivity, combo.second))

                    //update vector frame colors
                    setVectorFrameColors(tintBackground = true, showErrorDialog = false)
                }
            }
            adapter = presetsAdapter
        }

        //setup vectors
        mVectorsRecyclerView = vectors_rv

        mVectorsRecyclerView.apply {

            mVectorsRecyclerViewLayoutManager =
                GridLayoutManager(this@MainActivity, 2, GridLayoutManager.HORIZONTAL, false)
            layoutManager = mVectorsRecyclerViewLayoutManager
            setHasFixedSize(true)

            mVectorsAdapter = VectorsAdapter(this@MainActivity).apply {
                onVectorClick = { vector ->

                    if (mTempVector != vector) {
                        try {
                            mVectorFrame.setImageResource(Utils.getVectorProps(vector).first)
                            mTempVector = vector
                        } catch (e: Exception) {
                            e.printStackTrace()
                            mTempVector = Utils.getDefaultVectorForApi()
                            mVectorFrame.setImageResource(Utils.getVectorProps(mTempVector).first)
                        }

                        //update drawable tint
                        setVectorFrameColors(tintBackground = false, showErrorDialog = false)
                    }
                }

                @SuppressLint("DefaultLocale")
                onVectorLongClick = { vector ->

                    try {
                        val iconName = resources.getResourceEntryName(vector)
                            .replace(
                                getString(R.string.underscore_delimiter),
                                getString(R.string.space_delimiter)
                            )
                            .capitalize()

                        DynamicToast.make(
                            this@MainActivity,
                            iconName,
                            AppCompatResources.getDrawable(this@MainActivity, vector),
                            mTempVectorColor,
                            mTempBackgroundColor
                        )
                            .show()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        DynamicToast.makeError(
                            this@MainActivity,
                            getString(R.string.error_get_resource),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
            }
            adapter = mVectorsAdapter

            scrollToPosition(
                mVectorsAdapter.getVectorPosition(
                    mTempVector
                )
            )
        }

        mCategoriesChip.text = Utils.getCategory(this@MainActivity, mTempVector).first
    }

    //update UI on recent selected
    private fun onRecentSelected(
        selectedBackgroundColor: Int,
        selectedVectorColor: Int,
        selectedVector: Int,
        selectedCategory: Int,
        selectedScale: Float,
        selectedHorizontalOffset: Float,
        selectedVerticalOffset: Float
    ) {

        setBackgroundColorForUI(selectedBackgroundColor)
        setVectorColorForUI(selectedVectorColor)

        updateSelectedCategory(selectedCategory)

        scrollToVector(selectedVector)

        mTempScale = selectedScale
        mTempHorizontalOffset = selectedHorizontalOffset
        mTempVerticalOffset = selectedVerticalOffset
    }

    //update vector frame
    private fun setVectorFrameColors(tintBackground: Boolean, showErrorDialog: Boolean) {
        if (tintBackground) mVectorFrame.setBackgroundColor(mTempBackgroundColor)
        val vector = Utils.tintDrawable(
            this,
            mTempVector,
            mTempBackgroundColor,
            mTempVectorColor,
            showErrorDialog
        )
        mVectorFrame.setImageDrawable(vector)
    }

    //update background card colors
    private fun setBackgroundColorForUI(color: Int) {

        mTempBackgroundColor = color

        val textColor = mTempBackgroundColor.toSurfaceColor()

        //update shit colors
        background_color.setCardBackgroundColor(mTempBackgroundColor)

        background_color_head.setTextColor(textColor)
        background_color_subhead.apply {
            setTextColor(textColor)
            text = color.toHex(this@MainActivity)
            setTextColor(textColor)
        }

        mFab.backgroundTintList = ColorStateList.valueOf(mTempBackgroundColor)

        //check if colors are the same so we enable stroke to make vector visible
        val fabDrawableColor = if (Utils.checkIfColorsEqual(
                mTempBackgroundColor,
                mTempVectorColor
            )
        ) textColor else mTempVectorColor
        mFab.drawable.setTint(fabDrawableColor)

        //update vector frame colors
        setVectorFrameColors(tintBackground = true, showErrorDialog = false)
    }

    //update vector card colors
    private fun setVectorColorForUI(color: Int) {

        mTempVectorColor = color

        val textColor = mTempVectorColor.toSurfaceColor()

        //update shit colors
        vector_color.setCardBackgroundColor(mTempVectorColor)

        vector_color_head.setTextColor(textColor)
        vector_color_subhead.apply {
            setTextColor(textColor)
            text = mTempVectorColor.toHex(this@MainActivity)
        }

        //check if colors are the same so we enable stroke to make vector visible
        val fabDrawableColor =
            if (Utils.checkIfColorsEqual(
                    mTempBackgroundColor,
                    mTempVectorColor
                )
            ) textColor else mTempVectorColor
        mFab.drawable.setTint(fabDrawableColor)

        setVectorFrameColors(tintBackground = false, showErrorDialog = true)
    }

    private fun scrollToVector(vector: Int) {
        mVectorsAdapter.onVectorClick?.invoke(vector)
        mVectorsAdapter.swapSelectedDrawable(mTempVector)
        mVectorsRecyclerView.scrollToPosition(mVectorsAdapter.getVectorPosition(mTempVector))
    }

    //restore default wallpaper
    private fun restoreDefaultWallpaper() {

        vectorifyPreferences.vectorifyWallpaperBackup.apply {

            setBackgroundColorForUI(backgroundColor)
            setVectorColorForUI(vectorColor)

            updateSelectedCategory(category)
            scrollToVector(vectorColor)

            mTempScale = scale
            mTempHorizontalOffset = horizontalOffset
            mTempVerticalOffset = verticalOffset
        }
    }

    //start material dialog
    private fun startColorPicker(key: String, title: Int) {
        MaterialDialog(this).show {

            title(title)

            colorChooser(
                colors = ColorPalette.Primary,
                subColors = ColorPalette.PrimarySub,
                allowCustomArgb = true,
                showAlphaSelector = false

            ) { _, color ->
                when (key) {
                    getString(R.string.background_color_key) -> {
                        //update the color only if it really changed
                        if (mTempBackgroundColor != color) setBackgroundColorForUI(color)
                    }
                    else -> {
                        //update the color only if it really changed
                        if (mTempVectorColor != color) setVectorColorForUI(color)
                    }
                }
            }
            positiveButton()
        }
    }

    //method to start color picker for background
    fun startBackgroundColorPicker(view: View) {
        startColorPicker(
            getString(R.string.background_color_key),
            R.string.title_background_dialog
        )
    }

    //method to start color picker for vector
    fun startVectorColorPicker(view: View) {
        startColorPicker(
            getString(R.string.vectors_color_key),
            R.string.title_vector_dialog
        )
    }

    //method to start categories chooser
    fun startCategoryChooser(view: View) {
        MaterialDialog(this).show {
            title(R.string.title_categories)
            listItems(R.array.categories) { _, index, _ ->
                updateSelectedCategory(index)
            }
        }
    }

    fun applyAccentToVector(view: View) {
        setVectorColorForUI(Utils.getSystemAccentColor(this@MainActivity))
    }

    fun applyAccentToBackground(view: View) {
        setBackgroundColorForUI(Utils.getSystemAccentColor(this@MainActivity))
    }

    private fun updateSelectedCategory(index: Int) {
        if (mTempCategory != index) {

            mTempCategory = index

            val category = Utils.getCategory(this, mTempCategory)

            mVectorsRecyclerView.scrollToPosition(0)
            mVectorsAdapter.swapCategory(category.second)
            mCategoriesChip.text = category.first
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (sSaveLatestSetup) {
            super.onSaveInstanceState(outState)
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
    }

    private fun openRecentSetups() {
        mRecentSetupsDialog = MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {

            title(res = R.string.title_recent_setups)

            RecentsAdapter(context).apply {
                onRecentClick = { recent ->
                    recent.apply {
                        onRecentSelected(
                            backgroundColor,
                            vectorColor,
                            resource,
                            category,
                            scale,
                            horizontalOffset,
                            verticalOffset
                        )
                    }
                    dismiss()
                }
                customListAdapter(this)
            }

            getRecyclerView().layoutManager =
                GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) window?.apply {
                Utils.handleLightSystemBars(context, this, decorView, true)
                edgeToEdge {
                    getRecyclerView().fit { Edge.Bottom }
                    decorView.fit { Edge.Top }
                }
            }
        }
    }

    private fun showOptionsPopup(view: View) {

        PopupMenu(this, view).apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.clear_recents -> Utils.clearRecentSetups(this@MainActivity)
                    else -> restoreDefaultWallpaper()
                }
                return@setOnMenuItemClickListener true
            }
            inflate(R.menu.menu_do_something)
            if (vectorifyPreferences.vectorifyWallpaperSetups.isNullOrEmpty()) menu.removeItem(
                R.id.clear_recents
            )
            gravity = Gravity.END
            show()
        }
    }
}
