package com.iven.vectorify

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.ColorPalette
import com.afollestad.materialdialogs.color.colorChooser
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.iven.vectorify.adapters.ColorsAdapter
import com.iven.vectorify.adapters.VectorsAdapter
import com.iven.vectorify.ui.Utils
import kotlinx.android.synthetic.main.background_color_pref_card.*
import kotlinx.android.synthetic.main.presets_card.*
import kotlinx.android.synthetic.main.vector_color_pref_card.*
import kotlinx.android.synthetic.main.vectorify_activity.*
import kotlinx.android.synthetic.main.vectors_card.*

@Suppress("UNUSED_PARAMETER")
class VectorifyActivity : AppCompatActivity() {

    private var mTheme = R.style.AppTheme

    private var mBackgroundColor = 0
    private var mVectorColor = 0
    private var mVector = R.drawable.android

    private lateinit var mFab: FloatingActionButton
    private lateinit var mVectorFrame: ImageView
    private lateinit var mBackgroundSystemAccentGrabber: Chip
    private lateinit var mVectorSystemAccentGrabber: Chip

    private var sBackgroundColorChanged = false
    private var sVectorColorChanged = false
    private var sBackgroundAccentSet = false
    private var sVectorAccentSet = false

    private var sVectorChanged = true

    override fun onResume() {
        super.onResume()
        //check if accent theme has changed
        checkSystemAccent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //set theme
        mTheme = mVectorifyPreferences.theme
        setTheme(mTheme)

        setContentView(R.layout.vectorify_activity)

        //get system accent grabbers
        mBackgroundSystemAccentGrabber = background_system_accent
        mVectorSystemAccentGrabber = vector_system_accent
        mVectorFrame = vector_frame

        //get the fab (don't move from this position)
        mFab = fab

        //apply live wallpaper on fab click!
        mFab.setOnClickListener {
            handleWallpaperChanges()
        }

        //update background card color and text from preferences
        mBackgroundColor = mVectorifyPreferences.backgroundColor
        setBackgroundColorForUI(mBackgroundColor, false)

        //update vector card color and text from preferences
        mVectorColor = mVectorifyPreferences.vectorColor
        setVectorColorForUI(mVectorColor, false)

        mVector = mVectorifyPreferences.vector

        //set the bottom bar menu
        val bottomBar = bar
        bottomBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.app_bar_info -> openGitHubPage()
                R.id.app_bar_theme -> setNewTheme()
                R.id.app_bar_restore -> setDefaultVectorColors()
            }
            return@setOnMenuItemClickListener true
        }

        //setup presets
        colors_rv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val colorsAdapter = ColorsAdapter(this)
        colors_rv.adapter = colorsAdapter

        colorsAdapter.onColorClick = { combo ->

            setBackgroundAndVectorColorsChanged()

            runOnUiThread {

                //update background card color and fab tint
                val comboBackgroundColor = ContextCompat.getColor(this, combo.first)
                setBackgroundColorForUI(comboBackgroundColor, false)

                //update vector card color and fab check drawable
                val comboVectorColor = ContextCompat.getColor(this, combo.second)
                setVectorColorForUI(comboVectorColor, false)

                //update vector frame colors
                setVectorFrameColors()
            }
        }

        //setup presets
        vectors_rv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val vectorsAdapter = VectorsAdapter(this)
        vectors_rv.adapter = vectorsAdapter

        vectorsAdapter.onVectorClick = { vector ->

            if (mVector != vector) {
                mVector = vector!!
                runOnUiThread { mVectorFrame.setImageResource(mVector) }

                sVectorChanged = true
            }
        }

        vectors_rv.scrollToPosition(vectorsAdapter.getVectorPosition(mVector))

        //set vector frame height
        vectors_rv.afterMeasured {

            val vectorFrameLayoutParams = mVectorFrame.layoutParams as LinearLayout.LayoutParams
            vectorFrameLayoutParams.height = (height / 0.75F).toInt()
            mVectorFrame.layoutParams = vectorFrameLayoutParams

            setVectorFrameColors()
        }
    }

    private fun handleWallpaperChanges() {
        //do all the save shit here
        if (sBackgroundColorChanged) {
            mVectorifyPreferences.isBackgroundAccented = false
            mVectorifyPreferences.backgroundColor = mBackgroundColor
        }
        if (sVectorColorChanged) {
            mVectorifyPreferences.isVectorAccented = false
            mVectorifyPreferences.vectorColor = mVectorColor
        }
        if (sBackgroundAccentSet) {
            mVectorifyPreferences.isBackgroundAccented = true
            mVectorifyPreferences.backgroundColor = mBackgroundColor
        }
        if (sVectorAccentSet) {
            mVectorifyPreferences.isVectorAccented = true
            mVectorifyPreferences.vectorColor = mVectorColor
        }

        if (sVectorChanged) mVectorifyPreferences.vector = mVector

        val intent = Intent(this, SetWallpaperActivity::class.java)
        startActivity(intent)
    }

    //update vector frame
    private fun setVectorFrameColors() {
        mVectorFrame.setImageResource(mVector)
        mVectorFrame.setBackgroundColor(mBackgroundColor)
        if (mBackgroundColor == mVectorColor) {
            if (Utils.isColorDark(mVectorColor)) mVectorFrame.setColorFilter(Utils.lightenColor(mVectorColor, 0.20F))
            else mVectorFrame.setColorFilter(Utils.darkenColor(mVectorColor, 0.20F))
        } else {
            mVectorFrame.setColorFilter(mVectorColor)
        }
    }

    //update background card colors
    private fun setBackgroundColorForUI(color: Int, isSystemAccentChanged: Boolean) {
        mBackgroundColor = color

        //if system accent has changed update preferences on resume with the new accent
        if (isSystemAccentChanged) mVectorifyPreferences.backgroundColor = mBackgroundColor

        //update shit colors
        runOnUiThread {
            background_color.setCardBackgroundColor(color)
            val textColor = Utils.getSecondaryColor(color)
            background_color_head.setTextColor(textColor)
            background_color_subhead.setTextColor(textColor)
            background_color_subhead.text = getHexCode(color)
            mFab.backgroundTintList = ColorStateList.valueOf(color)

            //check if colors are the same so we enable stroke to make vector visible
            val fabDrawableColor = if (checkIfColorsEquals()) textColor else mVectorColor
            mFab.drawable.setTint(fabDrawableColor)

            setVectorFrameColors()
        }
    }

    //update vector card colors
    private fun setVectorColorForUI(color: Int, isSystemAccentChanged: Boolean) {
        mVectorColor = color

        //if system accent has changed update preferences on resume with the new accent
        if (isSystemAccentChanged) mVectorifyPreferences.vectorColor = mVectorColor

        //update shit colors
        runOnUiThread {
            vector_color.setCardBackgroundColor(color)
            val textColor = Utils.getSecondaryColor(color)
            vector_color_head.setTextColor(textColor)
            vector_color_subhead.setTextColor(textColor)
            vector_color_subhead.text = getHexCode(color)

            //check if colors are the same so we enable stroke to make vector visible
            val fabDrawableColor = if (checkIfColorsEquals()) textColor else color
            mFab.drawable.setTint(fabDrawableColor)

            setVectorFrameColors()
        }
    }

    //set system accent as background color
    fun setSystemAccentForBackground(view: View) {
        sBackgroundAccentSet = true
        val systemAccent = Utils.getSystemAccentColor(this)
        setBackgroundColorForUI(systemAccent, false)
    }

    //set system accent as vector color
    fun setSystemAccentForVector(view: View) {
        sVectorAccentSet = true
        val systemAccent = Utils.getSystemAccentColor(this)
        setVectorColorForUI(systemAccent, false)
    }

    //restore default background and vector colors
    private fun setDefaultVectorColors() {

        setBackgroundColorForUI(Color.BLACK, false)
        setVectorColorForUI(Color.WHITE, false)

        setBackgroundAndVectorColorsChanged()
    }

    //start material dialog
    private fun startColorPicker(key: String) {
        MaterialDialog(this).show {

            colorChooser(
                colors = ColorPalette.Primary,
                subColors = ColorPalette.PrimarySub,
                allowCustomArgb = true,
                showAlphaSelector = false

            ) { _, color ->
                when (key) {
                    getString(R.string.background_color_key) -> {
                        //update the color only if it really changed
                        if (mBackgroundColor != color) {
                            sBackgroundColorChanged = true
                            sBackgroundAccentSet = false
                            setBackgroundColorForUI(color, false)
                        }
                    }
                    else -> {
                        //update the color only if it really changed
                        if (mVectorColor != color) {
                            sVectorColorChanged = true
                            sVectorAccentSet = false
                            setVectorColorForUI(color, false)
                        }
                    }
                }
            }
            positiveButton(android.R.string.ok)
        }
    }

    private fun setBackgroundAndVectorColorsChanged() {
        sBackgroundAccentSet = false
        sVectorAccentSet = false
        sBackgroundColorChanged = true
        sVectorColorChanged = true
    }

    //method to start background color picker for background
    fun startBackgroundColorPicker(view: View) {
        startColorPicker(getString(R.string.background_color_key))
    }

    //method to start vector color picker for background
    fun startVectorColorPicker(view: View) {
        startColorPicker(getString(R.string.vectors_color_key))
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

    //check if background and vector colors are equals
    private fun checkIfColorsEquals(): Boolean {
        return mBackgroundColor == mVectorColor
    }

    //returns formatted hex string
    private fun getHexCode(color: Int): String {
        return getString(R.string.hex, Integer.toHexString(color)).toUpperCase()
    }

    //method to check if accent theme has changed on resume
    private fun checkSystemAccent(): Boolean {

        val isBackgroundAccented = mVectorifyPreferences.isBackgroundAccented
        val isVectorAccented = mVectorifyPreferences.isVectorAccented

        return if (!isBackgroundAccented && !isVectorAccented) {
            false
        } else {
            //get system accent color
            val systemAccentColor = Utils.getSystemAccentColor(this)

            //if changed, update it!
            if (systemAccentColor != mVectorifyPreferences.backgroundColor || systemAccentColor != mVectorifyPreferences.vectorColor) {

                //update cards colors
                if (isBackgroundAccented) setBackgroundColorForUI(systemAccentColor, true)
                if (isVectorAccented) setVectorColorForUI(systemAccentColor, true)
            }
            return true
        }
    }

    //method to open git page
    private fun openGitHubPage() {
        //intent to open git link
        val openGitHubPageIntent = Intent(Intent.ACTION_VIEW)
        openGitHubPageIntent.data = Uri.parse(getString(R.string.app_git_link))

        //check if a browser is present
        if (openGitHubPageIntent.resolveActivity(packageManager) != null) startActivity(openGitHubPageIntent) else
            Toast.makeText(this, getString(R.string.install_browser_message), Toast.LENGTH_SHORT).show()
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
}