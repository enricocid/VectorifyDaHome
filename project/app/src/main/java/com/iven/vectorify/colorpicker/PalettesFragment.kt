package com.iven.vectorify.colorpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.iven.vectorify.databinding.FragmentPresetsBinding
import com.iven.vectorify.utils.Utils


class PalettesFragment : Fragment() {

    private var _presetsBinding: FragmentPresetsBinding? = null
    var onColorSelection: ((Int) -> Unit)? = null

    private var mPalettesAdapter: PalettesAdapter? = null
    private var mTonesAdapter: TonesAdapter? = null

    // Unselect current tone
    fun resetPalette() {
        mPalettesAdapter?.resetSelectedPalette()
        mTonesAdapter?.resetColor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _presetsBinding = FragmentPresetsBinding.inflate(inflater, container, false)
        return _presetsBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mTonesAdapter = TonesAdapter()
        _presetsBinding?.run {
            selectedPalette.adapter = mTonesAdapter?.apply {
                onToneClicked = { color ->
                    onColorSelection?.invoke(color)
                }
            }
            selectedPalette.setHasFixedSize(true)

            mPalettesAdapter = PalettesAdapter(Utils.colorsMap.keys.toList())
            palettes.adapter = mPalettesAdapter?.apply {
                onPaletteClick = { pos ->
                    mTonesAdapter?.swapColors(pos)
                }
            }
            palettes.setHasFixedSize(true)

            selectedPalette.doOnPreDraw {
                palettes.updatePadding(left = it.left, right = it.left)
                (palettes.layoutManager as LinearLayoutManager)
                    .scrollToPositionWithOffset(0, 0)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = PalettesFragment()
    }
}
