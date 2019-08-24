package com.iven.vectorify

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
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
    private lateinit var mVectorsRecyclerView: RecyclerView
    private lateinit var mVectorsRecyclerViewLayoutManager: LinearLayoutManager
    private lateinit var mVectorsAdapter: VectorsAdapter

    private var mBackgroundColor = Color.BLACK
    private var mVectorColor = Color.WHITE
    private var mVector = R.drawable.android

    //interface to let recent  setups UI to let VectorifyActivity to update its shit
    private val recentSetupsInterface = object : RecentSetupsFragment.RecentSetupsInterface {
        override fun onRecentSelected(backgroundColor: Int, vector: Int, vectorColor: Int) {

            setBackgroundColorForUI(backgroundColor)
            setVectorColorForUI(vectorColor)

            setBackgroundAndVectorColorsChanged()

            scrollToVector(vector)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //set theme
        mTheme = mVectorifyPreferences.theme

        setTheme(mTheme)

        setContentView(R.layout.vectorify_activity)

        //get wallpaper shit
        mBackgroundColor = mVectorifyPreferences.backgroundColor
        mVectorColor = mVectorifyPreferences.vectorColor
        mVector = mVectorifyPreferences.vector

        //init temp preferences
        mTempPreferences.tempBackgroundColor = mBackgroundColor
        mTempPreferences.tempVectorColor = mVectorColor
        mTempPreferences.tempVector = mVector
        mTempPreferences.tempScale = mVectorifyPreferences.scale
        mTempPreferences.tempHorizontalOffset = mVectorifyPreferences.horizontalOffset
        mTempPreferences.tempVerticalOffset = mVectorifyPreferences.verticalOffset

        mVectorFrame = vector_frame

        //get the fab (don't move from this position)
        mFab = fab

        //apply live wallpaper on fab click!
        mFab.setOnClickListener {
            //start preview activity
            val intent = Intent(this, PreviewActivity::class.java)
            startActivity(intent)
        }

        //update background card color and text from preferences
        setBackgroundColorForUI(mBackgroundColor)

        //update vector card color and text from preferences
        setVectorColorForUI(mVectorColor)

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
                bottomSheetDialogFragment.show(supportFragmentManager, bottomSheetDialogFragment.tag)
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
        val presetsAdapter = PresetsAdapter(this)
        presets_rv.adapter = presetsAdapter

        presetsAdapter.onColorClick = { combo ->

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
        val gridSize = mDeviceMetrics.second / resources.getDimensionPixelSize(R.dimen.vector_size)
        mVectorsRecyclerViewLayoutManager = GridLayoutManager(this, gridSize / 2)
        mVectorsRecyclerView.layoutManager = mVectorsRecyclerViewLayoutManager
        mVectorsAdapter = VectorsAdapter(this)
        mVectorsRecyclerView.adapter = mVectorsAdapter

        mVectorsAdapter.onVectorClick = { vector ->

            if (mVector != vector) {
                runOnUiThread {
                    try {
                        mVectorFrame.setImageResource(Utils.getVectorProps(vector!!).first)
                        mVector = vector
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mVector = Utils.getDefaultVectorForApi()
                        mVectorFrame.setImageResource(Utils.getVectorProps(mVector).first)
                    }
                }

                //update drawable tint
                setVectorFrameColors(tintBackground = false, showErrorDialog = false)

                mTempPreferences.tempVector = mVector
                mTempPreferences.isVectorChanged = true
            }
        }

        mVectorsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                runOnUiThread {
                    categories.text =
                        Utils.getCategoryForPosition(resources, mVectorsRecyclerViewLayoutManager, mVectorsAdapter)
                }
            }
        })

        mVectorsRecyclerView.scrollToPosition(mVectorsAdapter.getVectorPosition(mVectorifyPreferences.vector))
    }

    //update vector frame
    private fun setVectorFrameColors(tintBackground: Boolean, showErrorDialog: Boolean) {
        if (tintBackground) mVectorFrame.setBackgroundColor(mBackgroundColor)
        val vectorDrawable = Utils.tintVectorDrawable(this, mVector, mBackgroundColor, mVectorColor, showErrorDialog)
        mVectorFrame.setImageDrawable(vectorDrawable)
    }

    //update background card colors
    private fun setBackgroundColorForUI(color: Int) {
        mBackgroundColor = color
        mTempPreferences.tempBackgroundColor = mBackgroundColor

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
                    mBackgroundColor,
                    mVectorColor
                )
            ) textColor else mTempPreferences.tempVectorColor
            mFab.drawable.setTint(fabDrawableColor)
        }
    }

    //update vector card colors
    private fun setVectorColorForUI(color: Int) {
        mVectorColor = color
        mTempPreferences.tempVectorColor = mVectorColor

        //update shit colors
        runOnUiThread {
            vector_color.setCardBackgroundColor(color)
            val textColor = Utils.getSecondaryColor(color)
            vector_color_head.setTextColor(textColor)
            vector_color_subhead.setTextColor(textColor)
            vector_color_subhead.text = getHexCode(color)

            //check if colors are the same so we enable stroke to make vector visible
            val fabDrawableColor = if (Utils.checkIfColorsEqual(mBackgroundColor, mVectorColor)) textColor else color
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
        mVectorsAdapter.swapSelectedDrawable(mVector)
        mVectorsRecyclerView.scrollToPosition(mVectorsAdapter.getVectorPosition(mVector))
    }

    //restore default wallpaper
    private fun restoreDefaultWallpaper() {

        setBackgroundColorForUI(Color.BLACK)
        setVectorColorForUI(Color.WHITE)

        setBackgroundAndVectorColorsChanged()

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
                val vector = Utils.getCategoryStartPosition(index)
                val vectorPosition = mVectorsAdapter.getVectorPosition(vector)

                //if the first item position of the selected category is > of the actual position
                //first scroll to the bottom, then go to the item position
                if (mVectorsRecyclerViewLayoutManager.findFirstVisibleItemPosition() < vectorPosition) {

                    mVectorsRecyclerView.afterMeasured {

                        mVectorsAdapter.itemCount.takeIf { it > 0 }?.let {

                            val timeMillis =
                                executeAndMeasureTimeMillis { mVectorsRecyclerView.scrollToPosition(it - 1) }

                            Handler().postDelayed(
                                { mVectorsRecyclerView.scrollToPosition(vectorPosition) },
                                timeMillis.second
                            )
                        }
                    }
                } else {
                    mVectorsRecyclerView.scrollToPosition(vectorPosition)
                }
            }
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
    private fun getHexCode(color: Int): String {
        return getString(R.string.hex, Integer.toHexString(color)).toUpperCase()
    }

    //method to open git page
    private fun openGitHubPage() {
        //intent to open git link
        val openGitHubPageIntent = Intent(Intent.ACTION_VIEW)
        openGitHubPageIntent.data = Uri.parse(getString(R.string.app_github_link))

        //check if a browser is present
        if (openGitHubPageIntent.resolveActivity(packageManager) != null) startActivity(openGitHubPageIntent) else
            DynamicToast.makeError(this, getString(R.string.install_browser_message), Toast.LENGTH_LONG)
                .show()
    }

    //Generified function to measure layout params
    //https://antonioleiva.com/kotlin-ongloballayoutlistener/
    private inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })
    }

    //measure the time a function takes to run
    private inline fun <R> executeAndMeasureTimeMillis(block: () -> R): Pair<R, Long> {
        val start = System.currentTimeMillis()
        val result = block()
        return result to (System.currentTimeMillis() - start)
    }
}
