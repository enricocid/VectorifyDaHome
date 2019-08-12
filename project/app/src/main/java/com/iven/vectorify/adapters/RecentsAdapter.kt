package com.iven.vectorify.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.iven.vectorify.R
import com.iven.vectorify.mVectorifyPreferences
import com.iven.vectorify.ui.Utils

class RecentSetupsAdapter(private val delimiter: String) :
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

        fun bindItems(set: String) {

            val recentButton = itemView.findViewById<ImageView>(R.id.recent_setups_vector)
            val arr = set.split(delimiter)

            val backgroundColor = Integer.parseInt(arr[0])
            recentButton.setBackgroundColor(backgroundColor)

            val vector = Integer.parseInt(arr[1])
            recentButton.setImageResource(vector)

            val drawable = recentButton.drawable.mutate()
            val vectorColor = Integer.parseInt(arr[2])

            val vectorColorForRecentItem = if (Utils.checkIfColorsEqual(backgroundColor, vectorColor)) {
                if (Utils.isColorDark(vectorColor)) Utils.lightenColor(vectorColor, 0.20F)
                else Utils.darkenColor(vectorColor, 0.20F)
            } else {
                vectorColor
            }

            drawable.setTint(vectorColorForRecentItem)

            recentButton.setOnClickListener {
                onRecentClick?.invoke(listOf(backgroundColor, vector, vectorColor))
            }
        }
    }
}
