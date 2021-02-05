package com.iven.vectorify.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.card.MaterialCardView
import com.iven.vectorify.R
import com.iven.vectorify.models.VectorifyWallpaper
import com.iven.vectorify.toContrastColor
import com.iven.vectorify.utils.Utils
import com.iven.vectorify.vectorifyPreferences

class RecentsAdapter(
        private val ctx: Context
) :
        RecyclerView.Adapter<RecentsAdapter.RecentSetupsHolder>() {

    var onRecentClick: ((VectorifyWallpaper) -> Unit)? = null
    private var mRecentSetups = vectorifyPreferences.vectorifyWallpaperSetups

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSetupsHolder {

        return RecentSetupsHolder(LayoutInflater.from(parent.context).inflate(
                R.layout.recent_option,
                parent,
                false)
        )
    }

    override fun getItemCount(): Int {
        return mRecentSetups?.size!!
    }

    override fun onBindViewHolder(holder: RecentSetupsHolder, position: Int) {
        holder.bindItems(mRecentSetups?.get(position)!!)
    }

    inner class RecentSetupsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(vectorifyWallpaper: VectorifyWallpaper) {

            val drawable =
                    Utils.tintDrawable(
                            ctx,
                            vectorifyWallpaper.resource,
                            vectorifyWallpaper.vectorColor.toContrastColor(vectorifyWallpaper.backgroundColor)
                    )

            itemView.run {

                contentDescription = ctx.getString(R.string.content_recent, adapterPosition)

                setOnClickListener {
                    onRecentClick?.invoke(vectorifyWallpaper)
                }
                setOnLongClickListener {

                    MaterialDialog(ctx).show {

                        title(R.string.title_recent_setups)
                        message(
                                text = ctx.getString(
                                        R.string.message_clear_single_recent_setup,
                                        adapterPosition.toString()
                                )
                        )
                        positiveButton {
                            //add an empty list to preferences
                            try {
                                if (mRecentSetups?.contains(vectorifyWallpaper)!!) {
                                    mRecentSetups?.remove(
                                            vectorifyWallpaper
                                    )
                                }
                                notifyDataSetChanged()
                                vectorifyPreferences.vectorifyWallpaperSetups = mRecentSetups
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        negativeButton { dismiss() }
                    }

                    return@setOnLongClickListener true
                }

                (this as MaterialCardView).setCardBackgroundColor(vectorifyWallpaper.backgroundColor)

                findViewById<ImageView>(R.id.recent_setups_vector).apply {

                    setImageDrawable(drawable)

                    scaleY = vectorifyWallpaper.scale
                    scaleX = vectorifyWallpaper.scale

                    // properly calculate image view gravity to match set wallpaper
                    x = (resources.getDimensionPixelOffset(R.dimen.recent_width) * vectorifyWallpaper.horizontalOffset) / vectorifyPreferences.vectorifyMetrics.width
                    y = (resources.getDimensionPixelOffset(R.dimen.recent_height) * vectorifyWallpaper.verticalOffset) / vectorifyPreferences.vectorifyMetrics.height
                }
            }
        }
    }
}
