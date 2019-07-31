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
import com.iven.vectorify.adapters.IconsAdapter
import com.iven.vectorify.ui.Utils
import kotlinx.android.synthetic.main.background_color_pref_card.*
import kotlinx.android.synthetic.main.icon_color_pref_card.*
import kotlinx.android.synthetic.main.iconify_activity.*
import kotlinx.android.synthetic.main.icons_card.*
import kotlinx.android.synthetic.main.presets_card.*

@Suppress("UNUSED_PARAMETER")
class VectorifyActivity : AppCompatActivity() {

    private var mTheme = R.style.AppTheme

    private var mBackgroundColor = 0
    private var mIconColor = 0
    private var mIcon = R.drawable.android

    private lateinit var mFab: FloatingActionButton
    private lateinit var mIconFrame: ImageView
    private lateinit var mBackgroundSystemAccentGrabber: Chip
    private lateinit var mIconSystemAccentGrabber: Chip

    private var sBackgroundColorChanged = false
    private var sIconColorChanged = false
    private var sBackgroundAccentSet = false
    private var sIconAccentSet = false

    private var sIconChanged = true

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

        setContentView(R.layout.iconify_activity)

        //get system accent grabbers
        mBackgroundSystemAccentGrabber = background_system_accent
        mIconSystemAccentGrabber = icon_system_accent
        mIconFrame = icon_frame

        //get the fab (don't move from this position)
        mFab = fab

        //apply live wallpaper on fab click!
        mFab.setOnClickListener {

            //do all the save shit here
            if (sBackgroundColorChanged) {
                mVectorifyPreferences.isBackgroundAccented = false
                mVectorifyPreferences.backgroundColor = mBackgroundColor
            }
            if (sIconColorChanged) {
                mVectorifyPreferences.isIconAccented = false
                mVectorifyPreferences.iconColor = mIconColor
            }
            if (sBackgroundAccentSet) {
                mVectorifyPreferences.isBackgroundAccented = true
                mVectorifyPreferences.backgroundColor = mBackgroundColor
            }
            if (sIconAccentSet) {
                mVectorifyPreferences.isIconAccented = true
                mVectorifyPreferences.iconColor = mIconColor
            }

            if (sIconChanged) mVectorifyPreferences.icon = mIcon

            Utils.openLiveWallpaperIntent(this)
        }

        //update background card color and text from preferences
        mBackgroundColor = mVectorifyPreferences.backgroundColor
        setBackgroundColorForUI(mBackgroundColor, false)

        //update icon card color and text from preferences
        mIconColor = mVectorifyPreferences.iconColor
        setIconColorForUI(mIconColor, false)

        mIcon = mVectorifyPreferences.icon

        //set the bottom bar menu
        val bottomBar = bar
        bottomBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.app_bar_info -> openGitHubPage()
                R.id.app_bar_theme -> setNewTheme()
                R.id.app_bar_restore -> setDefaultIconColors()
            }
            return@setOnMenuItemClickListener true
        }

        //setup presets
        colors_rv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val colorsAdapter = ColorsAdapter(this)
        colors_rv.adapter = colorsAdapter

        colorsAdapter.onColorClick = { combo ->

            setBackgroundAndIconColorsChanged()

            runOnUiThread {

                //update background card color and fab tint
                val comboBackgroundColor = ContextCompat.getColor(this, combo.first)
                setBackgroundColorForUI(comboBackgroundColor, false)

                //update icon card color and fab check drawable
                val comboIconColor = ContextCompat.getColor(this, combo.second)
                setIconColorForUI(comboIconColor, false)

                //update icon frame colors
                setIconFrameColors()
            }
        }

        //setup presets
        icons_rv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val iconsAdapter = IconsAdapter(this)
        icons_rv.adapter = iconsAdapter

        iconsAdapter.onIconClick = { icon ->

            if (mIcon != icon) {
                mIcon = icon!!
                runOnUiThread { mIconFrame.setImageResource(mIcon) }

                sIconChanged = true
            }
        }
        icons_rv.scrollToPosition(iconsAdapter.getIconPosition(mIcon))

        //set icon frame height to match icons rv + "icons" title total height
        icons_rv.afterMeasured {

            val iconFrameLayoutParams = mIconFrame.layoutParams as LinearLayout.LayoutParams
            iconFrameLayoutParams.height = height + icons_title.height
            mIconFrame.layoutParams = iconFrameLayoutParams

            setIconFrameColors()
        }
    }

    //update icon frame
    private fun setIconFrameColors() {
        mIconFrame.setImageResource(mIcon)
        mIconFrame.setBackgroundColor(mBackgroundColor)

        if (mBackgroundColor == mIconColor) {
            if (Utils.isColorDark(mIconColor)) mIconFrame.setColorFilter(Utils.lightenColor(mIconColor, 0.20F))
            else mIconFrame.setColorFilter(Utils.darkenColor(mIconColor, 0.20F))
        } else {
            mIconFrame.setColorFilter(mIconColor)
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

            //check if colors are the same so we enable stroke to make icon visible
            val fabDrawableColor = if (checkIfColorsEquals()) textColor else mIconColor
            mFab.drawable.setTint(fabDrawableColor)

            setIconFrameColors()
        }
    }

    //update icon card colors
    private fun setIconColorForUI(color: Int, isSystemAccentChanged: Boolean) {
        mIconColor = color

        //if system accent has changed update preferences on resume with the new accent
        if (isSystemAccentChanged) mVectorifyPreferences.iconColor = mIconColor

        //update shit colors
        runOnUiThread {
            icon_color.setCardBackgroundColor(color)
            val textColor = Utils.getSecondaryColor(color)
            icon_color_head.setTextColor(textColor)
            icon_color_subhead.setTextColor(textColor)
            icon_color_subhead.text = getHexCode(color)

            //check if colors are the same so we enable stroke to make icon visible
            val fabDrawableColor = if (checkIfColorsEquals()) textColor else color
            mFab.drawable.setTint(fabDrawableColor)

            setIconFrameColors()
        }
    }

    //set system accent as background color
    fun setSystemAccentForBackground(view: View) {
        sBackgroundAccentSet = true
        val systemAccent = Utils.getSystemAccentColor(this)
        setBackgroundColorForUI(systemAccent, false)
    }

    //set system accent as icon color
    fun setSystemAccentForIcon(view: View) {
        sIconAccentSet = true
        val systemAccent = Utils.getSystemAccentColor(this)
        setIconColorForUI(systemAccent, false)
    }

    //restore default background and icon colors
    private fun setDefaultIconColors() {

        setBackgroundColorForUI(Color.BLACK, false)
        setIconColorForUI(Color.WHITE, false)

        setBackgroundAndIconColorsChanged()
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
                        if (mIconColor != color) {
                            sIconColorChanged = true
                            sIconAccentSet = false
                            setIconColorForUI(color, false)
                        }
                    }
                }
            }
            positiveButton(android.R.string.ok)
        }
    }

    private fun setBackgroundAndIconColorsChanged() {
        sBackgroundAccentSet = false
        sIconAccentSet = false
        sBackgroundColorChanged = true
        sIconColorChanged = true
    }

    //method to start background color picker for background
    fun startBackgroundColorPicker(view: View) {
        startColorPicker(getString(R.string.background_color_key))
    }

    //method to start icon color picker for background
    fun startIconColorPicker(view: View) {
        startColorPicker(getString(R.string.icon_color_key))
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

    //check if background and icon colors are equals
    private fun checkIfColorsEquals(): Boolean {
        return mBackgroundColor == mIconColor
    }

    //returns formatted hex string
    private fun getHexCode(color: Int): String {
        return getString(R.string.hex, Integer.toHexString(color)).toUpperCase()
    }

    //method to check if accent theme has changed on resume
    private fun checkSystemAccent(): Boolean {

        val isBackgroundAccented = mVectorifyPreferences.isBackgroundAccented
        val isIconAccented = mVectorifyPreferences.isIconAccented

        return if (!isBackgroundAccented && !isIconAccented) {
            false
        } else {
            //get system accent color
            val systemAccentColor = Utils.getSystemAccentColor(this)

            //if changed, update it!
            if (systemAccentColor != mVectorifyPreferences.backgroundColor || systemAccentColor != mVectorifyPreferences.iconColor) {

                //update cards colors
                if (isBackgroundAccented) setBackgroundColorForUI(systemAccentColor, true)
                if (isIconAccented) setIconColorForUI(systemAccentColor, true)
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