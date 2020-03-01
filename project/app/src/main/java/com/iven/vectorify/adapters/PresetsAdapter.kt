package com.iven.vectorify.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.iven.vectorify.R
import com.iven.vectorify.toColouredToast
import com.iven.vectorify.toErrorToast

class PresetsAdapter(private val context: Context) :
    RecyclerView.Adapter<PresetsAdapter.ColorsHolder>() {

    var onPresetClick: ((Pair<Int, Int>) -> Unit)? = null

    //first = background color, second = vector color
    private val mPresets = listOf(
        //from https://www.canva.com/learn/100-color-combinations/
        Pair(R.color.midnight_blue, R.color.ink),
        Pair(R.color.dark_navy, R.color.blue_berry),
        Pair(R.color.shadow, R.color.mist),
        Pair(R.color.crevice, R.color.desert),
        Pair(R.color.deep_aqua, R.color.wave),
        Pair(R.color.blue_black, R.color.rain),
        Pair(R.color.blue_pine, R.color.reflection),
        Pair(R.color.ink, R.color.light_blue_berry),
        Pair(R.color.navy, R.color.android_blue),
        Pair(R.color.navy, R.color.android_green),
        Pair(R.color.greece, R.color.plaster),

        Pair(R.color.cocoa, R.color.chocolate),
        Pair(R.color.slate, R.color.ceramic),

        Pair(R.color.chocolate, R.color.toffee),
        Pair(R.color.chocolate, R.color.frosting),
        Pair(R.color.egg_plant, R.color.lemon_lime),
        Pair(R.color.blue_berry, R.color.daffodil),
        Pair(R.color.cloud, R.color.moss),
        Pair(R.color.blue, R.color.sun),
        Pair(R.color.sky, R.color.sunflower),
        Pair(R.color.android_green, R.color.navy),
        Pair(R.color.android_blue, R.color.navy),
        Pair(R.color.sea, R.color.sandstone),
        Pair(R.color.stem, R.color.poppy),
        Pair(R.color.turquoise, R.color.pink_tulip),
        Pair(R.color.branch, R.color.berry),
        Pair(R.color.glacier, R.color.ice),
        Pair(R.color.ice, R.color.overcast),
        Pair(R.color.ceramic, R.color.latte),
        Pair(R.color.plaster, R.color.greece)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorsHolder {
        return ColorsHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.preset_option,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mPresets.size
    }

    override fun onBindViewHolder(holder: ColorsHolder, position: Int) {
        holder.bindItems(mPresets[holder.adapterPosition])
    }

    inner class ColorsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("DefaultLocale")
        fun bindItems(combo: Pair<Int, Int>) {

            val colorItem = itemView as MaterialCardView

            val selectedVectorColor = ContextCompat.getColor(context, combo.second)
            colorItem.setCardBackgroundColor(selectedVectorColor)

            val selectedBackgroundColor = ContextCompat.getColor(context, combo.first)
            colorItem.strokeColor = selectedBackgroundColor

            itemView.apply {
                setOnClickListener {
                    onPresetClick?.invoke(combo)
                }

                setOnLongClickListener {

                    try {
                        val backgroundColorName =
                            context.resources.getResourceEntryName(combo.first)
                                .replace(
                                    context.getString(R.string.underscore_delimiter),
                                    context.getString(R.string.space_delimiter)
                                )
                                .capitalize()

                        val vectorColorName = context.resources.getResourceEntryName(combo.second)
                            .replace(
                                context.getString(R.string.underscore_delimiter),
                                context.getString(R.string.space_delimiter)
                            )
                            .capitalize()

                        context.getString(
                            R.string.selected_preset,
                            backgroundColorName,
                            vectorColorName
                        ).toColouredToast(
                            context,
                            null,
                            selectedBackgroundColor,
                            selectedVectorColor
                        )

                    } catch (e: Exception) {
                        e.printStackTrace()
                        context.getString(
                            R.string.error_get_resource
                        ).toErrorToast(context)
                    }
                    return@setOnLongClickListener false
                }
            }
        }
    }
}
