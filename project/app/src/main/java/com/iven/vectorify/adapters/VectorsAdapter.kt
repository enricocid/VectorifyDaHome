package com.iven.vectorify.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.iven.vectorify.R
import com.iven.vectorify.utils.Utils
import com.iven.vectorify.utils.VectorsCategories
import com.iven.vectorify.vectorifyPreferences

class VectorsAdapter(private val context: Context) :
        RecyclerView.Adapter<VectorsAdapter.VectorsHolder>() {

    var onVectorClick: ((Int) -> Unit)? = null
    var onVectorLongClick: ((Int) -> Unit)? = null

    private var mSelectedDrawable = vectorifyPreferences.vectorifyWallpaperBackup.resource
    private var mSelectedCategory = VectorsCategories.TECH

    init {
        vectorifyPreferences.liveVectorifyWallpaper?.let { recent ->
            mSelectedCategory = Utils.getCategory(context, recent.category).second
            mSelectedDrawable = recent.resource
        }
    }

    fun swapCategory(selectedCategory: List<Int>) {
        mSelectedCategory = selectedCategory
        notifyDataSetChanged()
    }

    fun swapSelectedDrawable(newSelectedDrawable: Int) {
        notifyItemChanged(getVectorPosition(mSelectedDrawable))
        mSelectedDrawable = newSelectedDrawable
        notifyItemChanged(getVectorPosition(mSelectedDrawable))
    }

    fun getVectorPosition(drawable: Int): Int {
        return try {
            mSelectedCategory.indexOf(drawable)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VectorsHolder {
        return VectorsHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.vector_option,
                        parent,
                        false
                )
        )
    }

    override fun getItemCount(): Int {
        return mSelectedCategory.size
    }

    override fun onBindViewHolder(holder: VectorsHolder, position: Int) {
        holder.bindItems(mSelectedCategory[holder.adapterPosition])
    }

    inner class VectorsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(drawable: Int) {

            val checkbox = itemView.findViewById<ImageView>(R.id.checkbox).apply {
                visibility = if (mSelectedDrawable == drawable) View.VISIBLE else View.GONE
            }

            itemView.findViewById<ImageButton>(R.id.vector_button).apply {
                setImageResource(drawable)
                setOnClickListener {
                    if (mSelectedDrawable != drawable) {
                        notifyItemChanged(getVectorPosition(mSelectedDrawable))
                        mSelectedDrawable = drawable
                        checkbox.visibility = View.VISIBLE
                        onVectorClick?.invoke(drawable)
                    }
                }
                setOnLongClickListener {
                    onVectorLongClick?.invoke(drawable)
                    return@setOnLongClickListener false
                }
            }
        }
    }
}
