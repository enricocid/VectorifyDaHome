package com.iven.vectorify.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iven.vectorify.R
import com.iven.vectorify.databinding.VectorItemBinding
import com.iven.vectorify.utils.Utils
import com.iven.vectorify.utils.VectorsCategories
import com.iven.vectorify.vectorifyPreferences

class VectorsAdapter(private val ctx: Context) :
    RecyclerView.Adapter<VectorsAdapter.VectorsHolder>() {

    var onVectorClick: ((Int) -> Unit)? = null
    var onVectorLongClick: ((Int) -> Unit)? = null

    private var mSelectedDrawable = R.drawable.android_logo_2019
    private var mSelectedCategory = VectorsCategories.TECH
    private var wallpaperToRestore = vectorifyPreferences.savedWallpaper

    init {
        if (Utils.isDeviceLand(ctx.resources)) {
            wallpaperToRestore = vectorifyPreferences.savedWallpaperLand
        }
        mSelectedCategory = Utils.getCategory(ctx, wallpaperToRestore.category).second
        mSelectedDrawable = wallpaperToRestore.resource
    }

    @SuppressLint("NotifyDataSetChanged")
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
        val binding = VectorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VectorsHolder(binding)
    }

    override fun getItemCount(): Int {
        return mSelectedCategory.size
    }

    override fun onBindViewHolder(holder: VectorsHolder, position: Int) {
        holder.bindItems(mSelectedCategory[holder.absoluteAdapterPosition])
    }

    inner class VectorsHolder(private val binding: VectorItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(drawable: Int) {

            binding.checkbox.visibility = if (mSelectedDrawable == drawable) View.VISIBLE else View.GONE

            binding.vectorButton.run {
                setImageResource(drawable)
                contentDescription = ctx.getString(R.string.content_vector, resources.getResourceEntryName(drawable)
                    .replace(
                        ctx.getString(R.string.underscore_delimiter),
                        ctx.getString(R.string.space_delimiter)
                    ))
                setOnClickListener {
                    if (mSelectedDrawable != drawable) {
                        notifyItemChanged(getVectorPosition(mSelectedDrawable))
                        mSelectedDrawable = drawable
                        binding.checkbox.visibility = View.VISIBLE
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
