package com.iven.vectorify.colorpicker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.iven.vectorify.R
import com.iven.vectorify.databinding.ToneItemBinding
import com.iven.vectorify.toSurfaceColor
import com.iven.vectorify.utils.Utils

class TonesAdapter: RecyclerView.Adapter<TonesAdapter.TonesHolder>() {

    private var mSelectedPalette = 0
    private var mColors = Utils.colorsMap.getValue(0)
    private var selectedColor = RecyclerView.NO_POSITION
    var onToneClicked: ((Int) -> Unit)? = null

    fun resetColor() {
        selectedColor = RecyclerView.NO_POSITION
    }

    @SuppressLint("NotifyDataSetChanged")
    fun swapColors(selection: Int) {
        selectedColor = -1
        mSelectedPalette = selection
        mColors = Utils.colorsMap.getValue(mSelectedPalette)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TonesHolder {
        val binding = ToneItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TonesHolder(binding)
    }

    override fun getItemCount() = mColors.size

    override fun onBindViewHolder(holder: TonesHolder, position: Int) {
        holder.bindItems(mColors[holder.absoluteAdapterPosition])
    }

    inner class TonesHolder(private val binding: ToneItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bindItems(colorInt: Int) {

            with(binding.root) {

                val translatedName = resources.getStringArray(R.array.accent_names)[mSelectedPalette]
                val tone = resources.getResourceEntryName(colorInt).filter { it.isDigit() }
                val fullName = context.getString(R.string.selected_tone, translatedName, tone)
                contentDescription = fullName

                val color = ContextCompat.getColor(context, colorInt)
                setCardBackgroundColor(color)

                if (absoluteAdapterPosition != selectedColor) {
                    binding.check.visibility = View.GONE
                } else {
                    binding.check.visibility = View.VISIBLE
                    binding.check.drawable.mutate().setTint(
                        ColorUtils.setAlphaComponent(color.toSurfaceColor(), 125)
                    )
                }

                setOnClickListener {
                    if (absoluteAdapterPosition != selectedColor) {
                        notifyItemChanged(selectedColor)
                        selectedColor = absoluteAdapterPosition
                        notifyItemChanged(absoluteAdapterPosition)
                        onToneClicked?.invoke(color)
                    }
                }

                setOnLongClickListener {
                    Toast.makeText(context, fullName, Toast.LENGTH_SHORT).show()
                    return@setOnLongClickListener true
                }
            }
        }
    }
}
