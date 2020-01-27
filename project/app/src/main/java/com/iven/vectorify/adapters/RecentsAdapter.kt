package com.iven.vectorify.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.iven.vectorify.R
import com.iven.vectorify.utils.Utils
import com.iven.vectorify.vectorifyPreferences

class RecentsAdapter(
    private val context: Context
) :
    RecyclerView.Adapter<RecentsAdapter.RecentSetupsHolder>() {

    var onRecentClick: ((List<Int>) -> Unit)? = null
    private var mRecentSetups: MutableList<String> =
        vectorifyPreferences.recentSetups.toMutableList()

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
        return mRecentSetups.size
    }

    override fun onBindViewHolder(holder: RecentSetupsHolder, position: Int) {

        val rawSetupString = mRecentSetups[holder.adapterPosition]
        val arr = rawSetupString.split(context.getString(R.string.delimiter))

        try {
            val selectedBackgroundColor = Integer.parseInt(arr[0])
            val selectedVector = Integer.parseInt(arr[1])
            val selectedVectorColor = Integer.parseInt(arr[2])
            val selectedVectorCategory = Integer.parseInt(arr[3])

            holder.bindItems(
                rawSetupString, listOf(
                    selectedBackgroundColor,
                    selectedVector,
                    selectedVectorColor,
                    selectedVectorCategory
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class RecentSetupsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(rawSetupString: String, recentSetup: List<Int>) {

            val recentButton = itemView.findViewById<ImageView>(R.id.recent_setups_vector)

            recentButton.setBackgroundColor(recentSetup[0])

            val drawable =
                Utils.tintDrawable(context, recentSetup[1], recentSetup[0], recentSetup[2], false)

            recentButton.setImageDrawable(drawable)

            recentButton.setOnClickListener {
                onRecentClick?.invoke(recentSetup)
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
                            mRecentSetups.remove(rawSetupString)
                            notifyDataSetChanged()
                            vectorifyPreferences.recentSetups = mRecentSetups.toMutableSet()
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
