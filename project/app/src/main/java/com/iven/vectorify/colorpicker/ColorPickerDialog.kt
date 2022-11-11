package com.iven.vectorify.colorpicker

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.iven.vectorify.*
import com.iven.vectorify.databinding.ColorPickerBinding

class ColorPickerDialog: BottomSheetDialogFragment() {

    private var _colorPickerBinding: ColorPickerBinding? = null

    private var mPickerFragment: PickerFragment? = null
    private var mPalettesFragment: PalettesFragment? = null

    private var mSheetTitle = ""
    private var mBackupColor = -1
    private var mSelectedColor = -1

    var onPositive: ((Int) -> Unit)? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString(TAG_DIALOG_TITLE)?.let { title ->
            mSheetTitle = title
        }
        arguments?.getInt(TAG_COLOR)?.let { color ->
            mBackupColor = color
            mSelectedColor = color
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _colorPickerBinding = ColorPickerBinding.inflate(inflater, container, false)
        return _colorPickerBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.applyFullHeightDialog(requireActivity())
        dialog?.disableShapeAnimation()

        val pagerAdapter = ScreenSlidePagerAdapter(requireActivity())
        val icon = listOf(R.drawable.ic_colorize, R.drawable.ic_palette)

        _colorPickerBinding?.run {

            viewPager2.let { vp ->
                vp.offscreenPageLimit = 2
                vp.adapter = pagerAdapter
                vp.reduceDragSensitivity()
                TabLayoutMediator(tabLayout, vp) { tab, position ->
                    tab.setIcon(icon[position])
                }.attach()
            }

            title.text = mSheetTitle

            updateColors(mSelectedColor)

            btnPositive.setOnClickListener {
                onPositive?.invoke(mSelectedColor)
                dismissAllowingStateLoss()
            }
            btnNegative.setOnClickListener { dismissAllowingStateLoss() }
            btnReset.run {
                setOnClickListener {
                    if (mBackupColor != mSelectedColor) {
                        mSelectedColor = mBackupColor
                        updateColors(mSelectedColor)
                        mPalettesFragment?.resetPalette()
                        visibility = View.GONE
                    }
                }
                val contrastColor = mBackupColor.toSurfaceColor()
                backgroundTintList = ColorStateList.valueOf(mBackupColor)
                setTextColor(contrastColor)
                iconTint = ColorStateList.valueOf(contrastColor)
                visibility = View.GONE
            }
        }
    }

    private fun handleOnNavigationItemSelected(itemId: Int) = when (itemId) {
        0 -> mPickerFragment ?: initFragmentAt(itemId)
        else -> mPalettesFragment ?: initFragmentAt(itemId)
    }

    private fun updateColors(color: Int) {

        _colorPickerBinding?.run {

            // Set background colors
            colorPreview.setCardBackgroundColor(color)

            // Set widgets colors
            val contrastColor = color.toSurfaceColor()
            hexValue.setTextColor(contrastColor)
            title.setTextColor(contrastColor)
            tabLayout.setSelectedTabIndicatorColor(contrastColor)
            tabLayout.tabIconTint = ColorStateList.valueOf(contrastColor)
            tabLayout.tabRippleColor = ColorStateList.valueOf(
                ColorUtils.setAlphaComponent(contrastColor, 25)
            )

            // Update hex value
            hexValue.text = color.toHex()

            if (btnReset.isGone) btnReset.visibility = View.VISIBLE

            mPickerFragment?.onUpdateColor(color)
        }
    }

    private fun initFragmentAt(position: Int): Fragment {
        when (position) {
            0 -> mPickerFragment = PickerFragment.newInstance(mSelectedColor).apply {
                onPickerUpdated = { r, g, b ->
                    val color = Color.rgb(r.toInt(), g.toInt(), b.toInt())
                    if (mSelectedColor != color) {
                        updateColors(color)
                        mSelectedColor = color
                    }
                }
                // Be sure to unselect current tone in palettes fragment
                onResetPalette = {
                    mPalettesFragment?.resetPalette()
                }
            }
            else -> mPalettesFragment = PalettesFragment.newInstance().apply {
                onColorSelection = { color ->
                    if (mSelectedColor != color) {
                        updateColors(color)
                        mSelectedColor = color
                    }
                }
            }
        }
        return handleOnNavigationItemSelected(position)
    }

    // ViewPager2 adapter class
    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {
        override fun getItemCount() = 2
        override fun createFragment(position: Int): Fragment = handleOnNavigationItemSelected(position)
    }

    companion object {
        const val TAG_PICKER = "COLOR_PICKER"
        const val TAG_DIALOG_TITLE = "COLOR_PICKER_TITLE"
        const val TAG_COLOR = "COLOR"

        @JvmStatic
        fun newInstance(title: String, selectedColor: Int) = ColorPickerDialog().apply {
            arguments = bundleOf(
                TAG_DIALOG_TITLE to title,
                TAG_COLOR to selectedColor
            )
        }
    }
}
