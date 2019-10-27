package com.iven.vectorify

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.ColorPalette
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.list.listItems
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.iven.vectorify.adapters.PresetsAdapter
import com.iven.vectorify.adapters.VectorsAdapter
import com.iven.vectorify.utils.Utils
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import kotlinx.android.synthetic.main.background_color_pref_card.*
import kotlinx.android.synthetic.main.cards_container.*
import kotlinx.android.synthetic.main.presets_card.*
import kotlinx.android.synthetic.main.vector_color_pref_card.*
import kotlinx.android.synthetic.main.vectorify_activity.*
import kotlinx.android.synthetic.main.vectors_card.*

@Suppress("UNUSED_PARAMETER")
class VectorifyActivity : AppCompatActivity() {

    private var mTheme = R.style.AppTheme

    private lateinit var mFab: FloatingActionButton
    private lateinit var mVectorFrame: ImageView
    private lateinit var mCategoriesChip: Chip
    private lateinit var mVectorsRecyclerView: RecyclerView
    private lateinit var mVectorsRecyclerViewLayoutManager: LinearLayoutManager
    private lateinit var mVectorsAdapter: VectorsAdapter

    private var mSelectedBackgroundColor = Color.BLACK
    private var mSelectedVectorColor = Color.WHITE
    private var mSelectedVector = R.drawable.android
    private var mSelectedCategory = 0

    //interface to let recent  setups UI to let VectorifyActivity to update its shit
    private val recentSetupsInterface = object : RecentSetupsFragment.RecentSetupsInterface {
        override fun onRecentSelected(
            selectedBackgroundColor: Int,
            selectedVector: Int,
            selectedVectorColor: Int,
            selectedCategory: Int
        ) {

            setBackgroundColorForUI(selectedBackgroundColor)
            setVectorColorForUI(selectedVectorColor)

            setBackgroundAndVectorColorsChanged()

            updateSelectedCategory(selectedCategory)

            scrollToVector(selectedVector)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //set theme
        mTheme = mVectorifyPreferences.theme

        setTheme(mTheme)

        setContentView(R.layout.vectorify_activity)

        //get wallpaper shit
        mSelectedBackgroundColor = mVectorifyPreferences.backgroundColor
        mSelectedVectorColor = mVectorifyPreferences.vectorColor
        mSelectedVector = mVectorifyPreferences.vector
        mSelectedCategory = mVectorifyPreferences.category

        //init temp preferences
        mTempPreferences.tempBackgroundColor = mSelectedBackgroundColor
        mTempPreferences.tempVectorColor = mSelectedVectorColor
        mTempPreferences.tempVector = mSelectedVector
        mTempPreferences.tempCategory = mSelectedCategory
        mTempPreferences.tempScale = mVectorifyPreferences.scale
        mTempPreferences.tempHorizontalOffset = mVectorifyPreferences.horizontalOffset
        mTempPreferences.tempVerticalOffset = mVectorifyPreferences.verticalOffset

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
        val bottomBar = bar
        bottomBar.replaceMenu(R.menu.bottom_menu)
        bottomBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.app_bar_info -> openGitHubPage()
                R.id.app_bar_theme -> setNewTheme()
                R.id.app_bar_restore -> restoreDefaultWallpaper()
            }
            return@setOnMenuItemClickListener true
        }

        bottomBar.setNavigationOnClickListener {
            if (mVectorifyPreferences.recentSetups.isNotEmpty()) {
                val bottomSheetDialogFragment = RecentSetupsFragment()
                bottomSheetDialogFragment.setRecentSetupsInterface(recentSetupsInterface)
                bottomSheetDialogFragment.show(
                    supportFragmentManager,
                    bottomSheetDialogFragment.tag
                )
            } else {
                DynamicToast.makeWarning(this, getString(R.string.message_no_recent_setups))
                    .show()
            }
        }

        bottomBar.afterMeasured {
            val version = version
            val lp = version.layoutParams as CoordinatorLayout.LayoutParams
            lp.setMargins(0, 0, 0, height)
            version.layoutParams = lp

            cards_container.setPadding(0, 0, 0, height + version.height)
        }

        //setup presets
        presets_rv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        presets_rv.setHasFixedSize(true)
        val presetsAdapter = PresetsAdapter(this)
        presets_rv.adapter = presetsAdapter

        presetsAdapter.onPresetClick = { combo ->

            setBackgroundAndVectorColorsChanged()

            runOnUiThread {

                //update background card color and fab tint
                val comboBackgroundColor = ContextCompat.getColor(this, combo.first)
                setBackgroundColorForUI(comboBackgroundColor)

                //update vector card color and fab check drawable
                val comboVectorColor = ContextCompat.getColor(this, combo.second)
                setVectorColorForUI(comboVectorColor)

                //update vector frame colors
                setVectorFrameColors(tintBackground = true, showErrorDialog = false)
            }
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
                runOnUiThread {
                    try {
                        mVectorFrame.setImageResource(Utils.getVectorProps(vector!!).first)
                        mSelectedVector = vector
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mSelectedVector = Utils.getDefaultVectorForApi()
                        mVectorFrame.setImageResource(Utils.getVectorProps(mSelectedVector).first)
                    }
                }

                //update drawable tint
                setVectorFrameColors(tintBackground = false, showErrorDialog = false)

                mTempPreferences.tempVector = mSelectedVector
                mTempPreferences.isVectorChanged = true
            }
        }

        runOnUiThread {
            mCategoriesChip.text = Utils.getCategory(this, mSelectedCategory).first
            mVectorsRecyclerView.scrollToPosition(
                mVectorsAdapter.getVectorPosition(
                    mVectorifyPreferences.vector
                )
            )
        }
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
        mTempPreferences.tempBackgroundColor = mSelectedBackgroundColor

        //update shit colors
        runOnUiThread {
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
            ) textColor else mTempPreferences.tempVectorColor
            mFab.drawable.setTint(fabDrawableColor)
        }
    }

    //update vector card colors
    private fun setVectorColorForUI(color: Int) {
        mSelectedVectorColor = color
        mTempPreferences.tempVectorColor = mSelectedVectorColor

        //update shit colors
        runOnUiThread {
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
    }

    //set system accent as background color
    fun setSystemAccentForBackground(view: View) {
        mTempPreferences.isBackgroundColorChanged = true
        val systemAccent = Utils.getSystemAccentColor(this)
        setBackgroundColorForUI(systemAccent)
    }

    //set system accent as vector color
    fun setSystemAccentForVector(view: View) {
        mTempPreferences.isVectorColorChanged = true
        val systemAccent = Utils.getSystemAccentColor(this)
        setVectorColorForUI(systemAccent)
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

        mTempPreferences.tempScale = 0.35F
        mTempPreferences.isScaleChanged = true

        mTempPreferences.tempHorizontalOffset = 0F
        mTempPreferences.isHorizontalOffsetChanged = true
        mTempPreferences.tempVerticalOffset = 0F
        mTempPreferences.isVerticalOffsetChanged = true
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
                        if (mTempPreferences.tempBackgroundColor != color) {
                            mTempPreferences.isBackgroundColorChanged = true
                            setBackgroundColorForUI(color)
                        }
                    }
                    else -> {
                        //update the color only if it really changed
                        if (mTempPreferences.tempVectorColor != color) {
                            mTempPreferences.isVectorColorChanged = true
                            setVectorColorForUI(color)
                        }
                    }
                }
            }
            positiveButton()
        }
    }

    private fun setBackgroundAndVectorColorsChanged() {
        mTempPreferences.isBackgroundColorChanged = true
        mTempPreferences.isVectorColorChanged = true
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

            runOnUiThread {
                mVectorsRecyclerView.scrollToPosition(0)
                mVectorsAdapter.swapCategory(category.second)
                mCategoriesChip.text = category.first
            }

            mSelectedCategory = index
            mTempPreferences.tempCategory = mSelectedCategory
            mTempPreferences.isCategoryChanged = true
        }
    }

    //update theme
    private fun setNewTheme() {
        val newTheme = if (mTheme == R.style.AppTheme) R.style.AppTheme_Dark else R.style.AppTheme
        mVectorifyPreferences.theme = newTheme

        //smoothly set app theme
        val intent = Intent(this, VectorifyActivity::class.java)
        startActivity(intent)
        finish()
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
