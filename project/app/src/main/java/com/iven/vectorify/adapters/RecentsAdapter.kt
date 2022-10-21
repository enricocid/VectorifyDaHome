package com.iven.vectorify.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.iven.vectorify.R
import com.iven.vectorify.databinding.RecentItemBinding
import com.iven.vectorify.models.VectorifyWallpaper
import com.iven.vectorify.toContrastColor
import com.iven.vectorify.utils.Utils
import com.iven.vectorify.vectorifyPreferences


class RecentsAdapter(private val ctx: Context) : RecyclerView.Adapter<RecentsAdapter.RecentSetupsHolder>() {

    var onRecentClick: ((VectorifyWallpaper) -> Unit)? = null
    private var mRecentSetups = vectorifyPreferences.recentSetups

    init {
        if (Utils.isDeviceLand(ctx.resources)) {
            mRecentSetups = vectorifyPreferences.recentSetupsLand
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSetupsHolder {
        val binding = RecentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentSetupsHolder(binding)
    }

    override fun getItemCount(): Int {
        return mRecentSetups?.size!!
    }

    override fun onBindViewHolder(holder: RecentSetupsHolder, position: Int) {
        holder.bindItems(mRecentSetups?.get(position)!!)
    }

    inner class RecentSetupsHolder(private val binding: RecentItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(wallpaper: VectorifyWallpaper) {

            val drawable =
                    Utils.tintDrawable(
                        ctx,
                        wallpaper.resource,
                        wallpaper.vectorColor.toContrastColor(wallpaper.backgroundColor)
                    )

            binding.root.run {

                contentDescription = ctx.getString(R.string.content_recent, absoluteAdapterPosition)

                setOnClickListener {
                    onRecentClick?.invoke(wallpaper)
                }

                setOnLongClickListener {
                    MaterialAlertDialogBuilder(ctx)
                        .setTitle(R.string.title_recent_setups)
                        .setMessage(ctx.getString(
                            R.string.message_clear_single_recent_setup,
                            absoluteAdapterPosition.toString()
                        ))
                        .setPositiveButton(R.string.ok) { _, _ ->
                            //add an empty list to preferences
                            try {
                                val index = mRecentSetups?.indexOf(wallpaper)!!
                                if (mRecentSetups?.contains(wallpaper)!!) {
                                    mRecentSetups?.remove(wallpaper)
                                }
                                notifyItemRemoved(index)
                                vectorifyPreferences.recentSetups = mRecentSetups
                            } catch (e: Exception) {
                                e.printStackTrace()
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
                    x = (resources.getDimensionPixelOffset(R.dimen.recent_width) * wallpaper.horizontalOffset) / vectorifyPreferences.savedMetrics.width
                    y = (resources.getDimensionPixelOffset(R.dimen.recent_height) * wallpaper.verticalOffset) / vectorifyPreferences.savedMetrics.height
                }
            }
        }
    }
}
