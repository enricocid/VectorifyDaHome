package com.iven.vectorify.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.iven.vectorify.R
import com.iven.vectorify.preferences.Recent
import com.iven.vectorify.utils.Utils
import com.iven.vectorify.vectorifyPreferences

class RecentsAdapter(
    private val context: Context
) :
    RecyclerView.Adapter<RecentsAdapter.RecentSetupsHolder>() {

    var onRecentClick: ((Recent) -> Unit)? = null
    private var mRecentSetups = vectorifyPreferences.recentSetups

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSetupsHolder {
        return RecentSetupsHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recent_option,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mRecentSetups?.size!!
    }

    override fun onBindViewHolder(holder: RecentSetupsHolder, position: Int) {

        holder.bindItems(mRecentSetups?.get(position)!!)
    }

    inner class RecentSetupsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(recent: Recent) {

            val recentButton = itemView.findViewById<ImageView>(R.id.recent_setups_vector)

            recentButton.setBackgroundColor(recent.backgroundColor)

            val drawable =
                Utils.tintDrawable(
                    context,
                    recent.resource,
                    recent.backgroundColor,
                    recent.vectorColor,
                    false
                )

            recentButton.setImageDrawable(drawable)

            recentButton.setOnClickListener {
                onRecentClick?.invoke(recent)
            }

            recentButton.setOnLongClickListener {

                MaterialDialog(context).show {

                    cornerRadius(res = R.dimen.md_corner_radius)
                    title(R.string.title_recent_setups)
                    message(
                        text = context.getString(
                            R.string.message_clear_single_recent_setup,
                            adapterPosition.toString()
                        )
                    )
                    positiveButton {
                        //add an empty list to preferences
                        try {
                            if (mRecentSetups?.contains(recent)!!) mRecentSetups?.remove(recent)
                            notifyDataSetChanged()
                            vectorifyPreferences.recentSetups = mRecentSetups
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    negativeButton { dismiss() }
                }

                return@setOnLongClickListener true
            }
        }
    }
}
