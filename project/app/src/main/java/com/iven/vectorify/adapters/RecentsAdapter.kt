package com.iven.vectorify.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.iven.vectorify.R
import com.iven.vectorify.VectorifyPreferences
import com.iven.vectorify.databinding.RecentItemBinding
import com.iven.vectorify.models.VectorifyWallpaper
import com.iven.vectorify.toContrastColor
import com.iven.vectorify.utils.Utils


class RecentsAdapter(private val recentSetups: MutableList<VectorifyWallpaper>?): RecyclerView.Adapter<RecentsAdapter.RecentSetupsHolder>() {

    var onRecentClick: ((VectorifyWallpaper) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSetupsHolder {
        val binding = RecentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentSetupsHolder(binding)
    }

    override fun getItemCount(): Int {
        return recentSetups?.size!!
    }

    override fun onBindViewHolder(holder: RecentSetupsHolder, position: Int) {
        holder.bindItems(recentSetups?.get(position)!!)
    }

    inner class RecentSetupsHolder(private val binding: RecentItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bindItems(wallpaper: VectorifyWallpaper) {

            val prefs = VectorifyPreferences.getPrefsInstance()

            binding.root.run {

                val drawable =
                    Utils.tintDrawable(
                        context,
                        wallpaper.resource,
                        wallpaper.vectorColor.toContrastColor(wallpaper.backgroundColor)
                    )

                contentDescription = context.getString(R.string.content_recent, absoluteAdapterPosition)

                setOnClickListener {
                    onRecentClick?.invoke(wallpaper)
                }

                setOnLongClickListener {
                    MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.title_recent_setups)
                        .setMessage(context.getString(
                            R.string.message_clear_single_recent_setup,
                            absoluteAdapterPosition.toString()
                        ))
                        .setPositiveButton(R.string.ok) { _, _ ->
                            recentSetups?.run {
                                val index = indexOf(wallpaper)
                                if (recentSetups.remove(wallpaper)) {
                                    notifyItemRemoved(index)
                                    prefs.recentSetups = recentSetups
                                }
                            }
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                    return@setOnLongClickListener true
                }

                setCardBackgroundColor(wallpaper.backgroundColor)

                binding.recentSetupsVector.run {

                    setImageDrawable(drawable)

                    scaleY = wallpaper.scale
                    scaleX = wallpaper.scale

                    // properly calculate image view gravity to match set wallpaper
                    x = (resources.getDimensionPixelOffset(R.dimen.recent_width) * wallpaper.horizontalOffset) / prefs.savedMetrics.width
                    y = (resources.getDimensionPixelOffset(R.dimen.recent_height) * wallpaper.verticalOffset) / prefs.savedMetrics.height
                }
            }
        }
    }
}
