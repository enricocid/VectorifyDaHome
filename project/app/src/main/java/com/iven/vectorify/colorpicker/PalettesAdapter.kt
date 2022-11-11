package com.iven.vectorify.colorpicker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.iven.vectorify.R
import com.iven.vectorify.databinding.PaletteItemBinding
import com.iven.vectorify.utils.Utils

class PalettesAdapter(private val colors: List<Int>): RecyclerView.Adapter<PalettesAdapter.PalettesHolder>() {

    var selectedAccent = 0
    var onPaletteClick: ((Int) -> Unit)? = null

    fun resetSelectedPalette() {
        onPaletteClick?.invoke(colors[selectedAccent])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PalettesHolder {
        val binding = PaletteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PalettesHolder(binding)
    }

    override fun getItemCount() = colors.size

    override fun onBindViewHolder(holder: PalettesHolder, position: Int) {
        holder.bindItems(colors[holder.absoluteAdapterPosition])
    }

    inner class PalettesHolder(private val binding: PaletteItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bindItems(position: Int) {

            with(binding) {

                val context = root.context
                val resources = root.resources

                val name = resources.getStringArray(R.array.accent_names)[absoluteAdapterPosition]
                root.contentDescription = name

                colorName.text = name

                if (absoluteAdapterPosition != selectedAccent) {
                    val colorDisabled = Utils.resolveWidgetsColorNormal(context)
                    root.strokeColor = colorDisabled
                    val drawable = ContextCompat.getDrawable(context, R.drawable.ic_checked)
                    drawable?.mutate()?.setTint(colorDisabled)
                    colorName.setTextColor(colorDisabled)
                    colorName.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
                } else {
                    val palette = resources.getIntArray(R.array.colors)[position]
                    root.strokeColor = palette
                    val drawable = ContextCompat.getDrawable(context, R.drawable.ic_checked)
                    drawable?.mutate()?.setTint(palette)
                    colorName.setTextColor(palette)
                    colorName.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
                }

                root.setOnClickListener {
                    if (absoluteAdapterPosition != selectedAccent) {
                        notifyItemChanged(selectedAccent)
                        selectedAccent = absoluteAdapterPosition
                        notifyItemChanged(absoluteAdapterPosition)
                        onPaletteClick?.invoke(colors[selectedAccent])
                    }
                }

                root.setOnLongClickListener {
                    Toast.makeText(context, name, Toast.LENGTH_SHORT).show()
                    return@setOnLongClickListener true
                }
            }
        }
    }
}
