package com.iven.vectorify.colorpicker

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import com.google.android.material.slider.Slider.OnSliderTouchListener
import com.iven.vectorify.databinding.FragmentPickerBinding
import com.iven.vectorify.toSliderValue


/**
 * A simple [Fragment] subclass.
 * Use the [PickerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PickerFragment : Fragment() {

    private var _pickerBinding: FragmentPickerBinding? = null
    private val mSliders = mutableMapOf<Slider?, TextView?>()

    private var mSelectedColor = Color.WHITE

    var onPickerUpdated: ((Float, Float, Float) -> Unit)? = null
    var onResetPalette: (() -> Unit)? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // restore picker color
        arguments?.getInt(TAG_PICKED_COLOR)?.let { color ->
            mSelectedColor = color
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _pickerBinding = FragmentPickerBinding.inflate(inflater, container, false)
        return _pickerBinding?.root
    }

    fun onUpdateColor(color: Int) {
        if (mSelectedColor != color) {
            mSelectedColor = color
            updateSliders()
        }
    }

    private fun updateSliders() {
        _pickerBinding?.run {
            sliderR.value = Color.red(mSelectedColor).toFloat()
            sliderG.value = Color.green(mSelectedColor).toFloat()
            sliderB.value = Color.blue(mSelectedColor).toFloat()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _pickerBinding?.run {

            mSliders[sliderR] = valueR
            mSliders[sliderG] = valueG
            mSliders[sliderB] = valueB

            // restore picker positions
            updateSliders()

            val iterator = mSliders.iterator().withIndex()

            while (iterator.hasNext()) {
                val item = iterator.next()
                item.value.key?.let { slider ->
                    val textView = mSliders[slider]
                    textView?.text = slider.value.toSliderValue()
                    slider.addOnChangeListener { _, value, fromUser ->
                        textView?.text = value.toSliderValue()
                        if (fromUser) {
                            onPickerUpdated?.invoke(sliderR.value, sliderG.value, sliderB.value)
                        }
                    }
                    slider.addOnSliderTouchListener(object: OnSliderTouchListener {
                        override fun onStartTrackingTouch(slider: Slider) {
                        }
                        override fun onStopTrackingTouch(slider: Slider) {
                            // Be sure to unselect current tone in palettes fragment
                            onResetPalette?.invoke()
                        }
                    })
                }
            }
        }
    }

    companion object {

        const val TAG_PICKED_COLOR = "PICKED_COLOR"

        @JvmStatic
        fun newInstance(selectedColor: Int) = PickerFragment().apply {
            arguments = bundleOf(TAG_PICKED_COLOR to selectedColor)
        }
    }
}
