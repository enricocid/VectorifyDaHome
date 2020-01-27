package com.iven.vectorify

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
class VectorifyActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var mFab: FloatingActionButton
    private lateinit var mVectorFrame: ImageView
    private lateinit var mBottomBar: BottomAppBar
    private lateinit var mCategoriesChip: Chip
    private lateinit var mVectorsRecyclerView: RecyclerView
    private lateinit var mVectorsRecyclerViewLayoutManager: LinearLayoutManager
    private lateinit var mVectorsAdapter: VectorsAdapter

    private var mSelectedBackgroundColor = Color.BLACK
    private var mSelectedVectorColor = Color.WHITE
    private var mSelectedVector = R.drawable.android
    private var mSelectedCategory = 0

    private lateinit var mRecentSetupsDialog: MaterialDialog

    //interface to let recent  setups UI to let VectorifyActivity to update its shit
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

        setBackgroundAndVectorColorsChanged()

        updateSelectedCategory(selectedCategory)

        scrollToVector(selectedVector)

        tempPreferences.tempScale = selectedScale
        tempPreferences.isScaleChanged = true

        tempPreferences.isHorizontalOffsetChanged = true
        tempPreferences.tempHorizontalOffset = selectedHorizontalOffset
        tempPreferences.isVerticalOffsetChanged = true
        tempPreferences.tempVerticalOffset = selectedVerticalOffset
    }

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
        if (key == getString(R.string.recent_setups_key) && !vectorifyPreferences.recentSetups?.isNullOrEmpty()!!) {
            if (::mRecentSetupsDialog.isInitialized && mRecentSetupsDialog.isShowing) mRecentSetupsDialog.dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.vectorify_activity)

        //get wallpaper shit
        mSelectedBackgroundColor = vectorifyPreferences.backgroundColor
        mSelectedVectorColor = vectorifyPreferences.vectorColor
        mSelectedVector = vectorifyPreferences.vector
        mSelectedCategory = vectorifyPreferences.category

        //init temp preferences
        tempPreferences.tempBackgroundColor = mSelectedBackgroundColor
        tempPreferences.tempVectorColor = mSelectedVectorColor
        tempPreferences.tempVector = mSelectedVector
        tempPreferences.tempCategory = mSelectedCategory
        tempPreferences.tempScale = vectorifyPreferences.scale
        tempPreferences.tempHorizontalOffset = vectorifyPreferences.horizontalOffset
        tempPreferences.tempVerticalOffset = vectorifyPreferences.verticalOffset

        mVectorFrame = vector_frame
        mCategoriesChip = categories_chip

        //get the fab (don't move from this position)
        mFab = fab

        //apply live wallpaper on fab click!
        mFab.setOnClickListener {
            //start preview activity
            val intent = Intent(this, PreviewActivity::class.java)
            startActivity(intent)
        }

        //update background card color and text from preferences
        setBackgroundColorForUI(mSelectedBackgroundColor)

        //update vector card color and text from preferences
        setVectorColorForUI(mSelectedVectorColor)

        //set the bottom bar menu
        mBottomBar = bar

        mBottomBar.apply {
            replaceMenu(R.menu.bottom_menu)
            menu.findItem(R.id.app_bar_restore).title = getString(R.string.title_reset)
            val menuThemeItem = menu.findItem(R.id.app_bar_theme)
            menuThemeItem.icon = ContextCompat.getDrawable(
                this@VectorifyActivity,
                Utils.getDefaultNightModeIcon(this@VectorifyActivity)
            )
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.app_bar_info -> openGitHubPage()
                    R.id.app_bar_theme -> {

                        val newTheme = Utils.getProgressiveDefaultNightMode(this@VectorifyActivity)
                        vectorifyPreferences.theme = newTheme

                        AppCompatDelegate.setDefaultNightMode(
                            Utils.getDefaultNightMode(
                                applicationContext
                            )
                        )
                        if (newTheme == context.getString(R.string.theme_pref_light)) menuThemeItem.icon =
                            ContextCompat.getDrawable(
                                this@VectorifyActivity,
                                R.drawable.ic_theme_light
                            )
                    }
                    R.id.app_bar_restore -> showOptionsPopups(
                        this@VectorifyActivity,
                        findViewById(R.id.app_bar_restore)
                    )
                }
                return@setOnMenuItemClickListener true
            }

            setNavigationOnClickListener {
                if (!vectorifyPreferences.recentSetups.isNullOrEmpty())
                    startRecentsDialog()
                else
                    DynamicToast.makeWarning(
                        this@VectorifyActivity,
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

        //setup presets
        presets_rv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        presets_rv.setHasFixedSize(true)
        val presetsAdapter = PresetsAdapter(this)
        presets_rv.adapter = presetsAdapter

        presetsAdapter.onPresetClick = { combo ->

            setBackgroundAndVectorColorsChanged()

            //update background card color and fab tint
            val comboBackgroundColor = ContextCompat.getColor(this, combo.first)
            setBackgroundColorForUI(comboBackgroundColor)

            //update vector card color and fab check drawable
            val comboVectorColor = ContextCompat.getColor(this, combo.second)
            setVectorColorForUI(comboVectorColor)

            //update vector frame colors
            setVectorFrameColors(tintBackground = true, showErrorDialog = false)
        }

        //setup vectors
        mVectorsRecyclerView = vectors_rv
        mVectorsRecyclerViewLayoutManager =
            GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)

        mVectorsRecyclerView.layoutManager = mVectorsRecyclerViewLayoutManager
        mVectorsRecyclerView.setHasFixedSize(true)
        mVectorsAdapter = VectorsAdapter(this)
        mVectorsRecyclerView.adapter = mVectorsAdapter

        mVectorsAdapter.onVectorClick = { vector ->

            if (mSelectedVector != vector) {
                try {
                    mVectorFrame.setImageResource(Utils.getVectorProps(vector!!).first)
                    mSelectedVector = vector
                } catch (e: Exception) {
                    e.printStackTrace()
                    mSelectedVector = Utils.getDefaultVectorForApi()
                    mVectorFrame.setImageResource(Utils.getVectorProps(mSelectedVector).first)
                }

                //update drawable tint
                setVectorFrameColors(tintBackground = false, showErrorDialog = false)

                tempPreferences.tempVector = mSelectedVector
                tempPreferences.isVectorChanged = true
            }
        }

        mCategoriesChip.text = Utils.getCategory(this, mSelectedCategory).first
        mVectorsRecyclerView.scrollToPosition(
            mVectorsAdapter.getVectorPosition(
                vectorifyPreferences.vector
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            window?.apply {
                Utils.handleLightSystemBars(this@VectorifyActivity, window, decorView, false)
                edgeToEdge {
                    decorView.fit { Edge.Top }
                    mBottomBar.fit { Edge.Bottom + Edge.Right }
                    mFab.fit { Edge.Right }
                    mFab.fitPadding { Edge.Bottom }
                }
            }
        }
    }

    private fun showOptionsPopups(
        context: Context,
        view: View
    ) {
        val popup = PopupMenu(context, view)
        popup.setOnMenuItemClickListener {

            when (it.itemId) {
                R.id.clear_recents -> Utils.clearRecentSetups(context)
                else -> restoreDefaultWallpaper()
            }

            return@setOnMenuItemClickListener true
        }
        popup.inflate(R.menu.menu_do_something)
        if (vectorifyPreferences.recentSetups.isNullOrEmpty()) popup.menu.removeItem(R.id.clear_recents)
        popup.gravity = Gravity.END
        popup.show()
    }

    //update vector frame
    private fun setVectorFrameColors(tintBackground: Boolean, showErrorDialog: Boolean) {
        if (tintBackground) mVectorFrame.setBackgroundColor(mSelectedBackgroundColor)
        val vector = Utils.tintDrawable(
            this,
            mSelectedVector,
            mSelectedBackgroundColor,
            mSelectedVectorColor,
            showErrorDialog
        )
        mVectorFrame.setImageDrawable(vector)
    }

    //update background card colors
    private fun setBackgroundColorForUI(color: Int) {
        mSelectedBackgroundColor = color
        tempPreferences.tempBackgroundColor = mSelectedBackgroundColor

        //update shit colors
        background_color.setCardBackgroundColor(color)
        val textColor = Utils.getSecondaryColor(color)
        background_color_head.setTextColor(textColor)
        background_color_subhead.setTextColor(textColor)
        background_color_subhead.text = getHexCode(color)
        mFab.backgroundTintList = ColorStateList.valueOf(color)

        //update vector frame colors
        setVectorFrameColors(tintBackground = true, showErrorDialog = false)

        //check if colors are the same so we enable stroke to make vector visible
        val fabDrawableColor = if (Utils.checkIfColorsEqual(
                mSelectedBackgroundColor,
                mSelectedVectorColor
            )
        ) textColor else tempPreferences.tempVectorColor
        mFab.drawable.setTint(fabDrawableColor)
    }

    //update vector card colors
    private fun setVectorColorForUI(color: Int) {
        mSelectedVectorColor = color
        tempPreferences.tempVectorColor = mSelectedVectorColor

        //update shit colors
        vector_color.setCardBackgroundColor(color)
        val textColor = Utils.getSecondaryColor(color)
        vector_color_head.setTextColor(textColor)
        vector_color_subhead.setTextColor(textColor)
        vector_color_subhead.text = getHexCode(color)

        //check if colors are the same so we enable stroke to make vector visible
        val fabDrawableColor =
            if (Utils.checkIfColorsEqual(
                    mSelectedBackgroundColor,
                    mSelectedVectorColor
                )
            ) textColor else color
        mFab.drawable.setTint(fabDrawableColor)

        setVectorFrameColors(tintBackground = false, showErrorDialog = true)
    }

    //set system accent as background color
    fun setSystemAccentForBackground(view: View) {
        tempPreferences.isBackgroundColorChanged = true
        val systemAccent = Utils.getSystemAccentColor(this)
        setBackgroundColorForUI(systemAccent)
    }

    //set system accent as vector color
    fun setSystemAccentForVector(view: View) {
        tempPreferences.isVectorColorChanged = true
        val systemAccent = Utils.getSystemAccentColor(this)
        setVectorColorForUI(systemAccent)
    }

    fun swapCardsColor(view: View) {
        ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 180f).apply {
            duration = 500
            start()
            doOnEnd {
                val tempBackgroundColorBackup = tempPreferences.tempBackgroundColor
                setBackgroundColorForUI(tempPreferences.tempVectorColor)
                setVectorColorForUI(tempBackgroundColorBackup)
                tempPreferences.isVectorColorChanged = true
                tempPreferences.isBackgroundColorChanged = true
            }
        }
    }

    private fun scrollToVector(vector: Int) {
        mVectorsAdapter.onVectorClick?.invoke(vector)
        mVectorsAdapter.swapSelectedDrawable(mSelectedVector)
        mVectorsRecyclerView.scrollToPosition(mVectorsAdapter.getVectorPosition(mSelectedVector))
    }

    //restore default wallpaper
    private fun restoreDefaultWallpaper() {

        setBackgroundColorForUI(Color.BLACK)
        setVectorColorForUI(Color.WHITE)

        setBackgroundAndVectorColorsChanged()

        updateSelectedCategory(0)

        scrollToVector(Utils.getDefaultVectorForApi())

        tempPreferences.tempScale = 0.35F
        tempPreferences.isScaleChanged = true

        tempPreferences.tempHorizontalOffset = 0F
        tempPreferences.isHorizontalOffsetChanged = true
        tempPreferences.tempVerticalOffset = 0F
        tempPreferences.isVerticalOffsetChanged = true
    }

    //start material dialog
    private fun startColorPicker(key: String, title: Int) {
        MaterialDialog(this).show {

            title(title)
            cornerRadius(res = R.dimen.md_corner_radius)
            colorChooser(
                colors = ColorPalette.Primary,
                subColors = ColorPalette.PrimarySub,
                allowCustomArgb = true,
                showAlphaSelector = false

            ) { _, color ->
                when (key) {
                    getString(R.string.background_color_key) -> {
                        //update the color only if it really changed
                        if (tempPreferences.tempBackgroundColor != color) {
                            tempPreferences.isBackgroundColorChanged = true
                            setBackgroundColorForUI(color)
                        }
                    }
                    else -> {
                        //update the color only if it really changed
                        if (tempPreferences.tempVectorColor != color) {
                            tempPreferences.isVectorColorChanged = true
                            setVectorColorForUI(color)
                        }
                    }
                }
            }
            positiveButton()
        }
    }

    private fun setBackgroundAndVectorColorsChanged() {
        tempPreferences.isBackgroundColorChanged = true
        tempPreferences.isVectorColorChanged = true
    }

    //method to start background color picker for background
    fun startBackgroundColorPicker(view: View) {
        startColorPicker(getString(R.string.background_color_key), R.string.title_background_dialog)
    }

    //method to start vector color picker for background
    fun startVectorColorPicker(view: View) {
        startColorPicker(getString(R.string.vectors_color_key), R.string.title_vector_dialog)
    }

    //method to start categories chooser
    fun startCategoryChooser(view: View) {
        MaterialDialog(this).show {
            cornerRadius(res = R.dimen.md_corner_radius)
            title(R.string.title_categories)
            listItems(R.array.categories) { _, index, _ ->
                updateSelectedCategory(index)
            }
        }
    }

    private fun updateSelectedCategory(index: Int) {
        if (mSelectedCategory != index) {

            val category = Utils.getCategory(this@VectorifyActivity, index)

            mVectorsRecyclerView.scrollToPosition(0)
            mVectorsAdapter.swapCategory(category.second)
            mCategoriesChip.text = category.first

            mSelectedCategory = index
            tempPreferences.tempCategory = mSelectedCategory
            tempPreferences.isCategoryChanged = true
        }
    }

    private fun startRecentsDialog() {
        mRecentSetupsDialog = MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {

            cornerRadius(res = R.dimen.md_corner_radius)

            title(res = R.string.title_recent_setups)

            val recentsAdapter = RecentsAdapter(this@VectorifyActivity)

            recentsAdapter.onRecentClick = { recent ->
                onRecentSelected(
                    recent.backgroundColor,
                    recent.vectorColor,
                    recent.resource,
                    recent.category,
                    recent.scale,
                    recent.horizontalOffset,
                    recent.verticalOffset
                )
                dismiss()
            }

            customListAdapter(recentsAdapter)
            getRecyclerView().layoutManager =
                GridLayoutManager(this@VectorifyActivity, 3, RecyclerView.VERTICAL, false)


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                window?.apply {
                    Utils.handleLightSystemBars(this@VectorifyActivity, window, decorView, true)
                    edgeToEdge {
                        getRecyclerView().fit { Edge.Bottom }
                        decorView.fit { Edge.Top }
                    }
                }

            }
        }
    }

    //returns formatted hex string
    @SuppressLint("DefaultLocale")
    private fun getHexCode(color: Int): String {
        return getString(R.string.hex, Integer.toHexString(color)).toUpperCase()
    }

    //method to open git page
    private fun openGitHubPage() {
        //intent to open git link
        val openGitHubPageIntent = Intent(Intent.ACTION_VIEW)
        openGitHubPageIntent.data = Uri.parse(getString(R.string.app_github_link))

        //check if a browser is present
        if (openGitHubPageIntent.resolveActivity(packageManager) != null) startActivity(
            openGitHubPageIntent
        ) else
            DynamicToast.makeError(
                this,
                getString(R.string.install_browser_message),
                Toast.LENGTH_LONG
            )
                .show()
    }

    //Generified function to measure layout params
    //https://antonioleiva.com/kotlin-ongloballayoutlistener/
    private inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })
    }
}
