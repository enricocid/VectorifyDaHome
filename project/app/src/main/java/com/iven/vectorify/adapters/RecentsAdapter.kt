package com.iven.vectorify.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.iven.vectorify.R
import com.iven.vectorify.RecentSetupsFragment
import com.iven.vectorify.mVectorifyPreferences
import com.iven.vectorify.utils.Utils

class RecentSetupsAdapter(
    private val context: Context,
    private val delimiter: String,
    private val recentSetupsFragment: RecentSetupsFragment
) :
    RecyclerView.Adapter<RecentSetupsAdapter.RecentSetupsHolder>() {

    var onRecentClick: ((List<Int>) -> Unit)? = null
    private var mRecentSetups: MutableList<String> = mVectorifyPreferences.recentSetups.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSetupsHolder {
        return RecentSetupsHolder(LayoutInflater.from(parent.context).inflate(R.layout.recent_option, parent, false))
    }

    override fun getItemCount(): Int {
        return mRecentSetups.size
    }

    override fun onBindViewHolder(holder: RecentSetupsHolder, position: Int) {
        holder.bindItems(mRecentSetups[holder.adapterPosition])
    }

    inner class RecentSetupsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(setup: String) {

            val recentButton = itemView.findViewById<ImageView>(R.id.recent_setups_vector)
            val arr = setup.split(delimiter)

            val backgroundColor = Integer.parseInt(arr[0])
            recentButton.setBackgroundColor(backgroundColor)

            val vector = Integer.parseInt(arr[1])

            val vectorColor = Integer.parseInt(arr[2])

            val vectorDrawable = Utils.tintVectorDrawable(context, vector, backgroundColor, vectorColor, false)

            recentButton.setImageDrawable(vectorDrawable)

            recentButton.setOnClickListener {
                onRecentClick?.invoke(listOf(backgroundColor, vector, vectorColor))
            }

            recentButton.setOnLongClickListener {

                if (recentSetupsFragment.context != null) {
                    val context = recentSetupsFragment.context!!
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
                                mRecentSetups.remove(setup)
                                notifyDataSetChanged()
                                mVectorifyPreferences.recentSetups = mRecentSetups.toMutableSet()
                                if (mRecentSetups.isEmpty()) recentSetupsFragment.dismiss()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        negativeButton { dismiss() }
                    }
                }
                return@setOnLongClickListener true
            }
        }
    }
}
