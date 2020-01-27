package com.iven.vectorify.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.iven.vectorify.R
import com.iven.vectorify.tempPreferences
import com.iven.vectorify.utils.Utils
import com.iven.vectorify.utils.VectorsCategories
import com.iven.vectorify.vectorifyPreferences
import com.pranavpandey.android.dynamic.toasts.DynamicToast

class VectorsAdapter(private val context: Context) :
    RecyclerView.Adapter<VectorsAdapter.VectorsHolder>() {

    var onVectorClick: ((Int?) -> Unit)? = null

    private var mSelectedDrawable = R.drawable.android
    private var mSelectedCategory = VectorsCategories.TECH

    init {
        mSelectedCategory = Utils.getCategory(context, vectorifyPreferences.category).second
        mSelectedDrawable = vectorifyPreferences.vector
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

        @SuppressLint("DefaultLocale")
        fun bindItems(drawable: Int) {

            val vectorButton = itemView.findViewById<ImageButton>(R.id.vector_button)
            val checkbox = itemView.findViewById<ImageView>(R.id.checkbox)

            vectorButton.setImageResource(drawable)

            if (mSelectedDrawable == drawable) checkbox.visibility = View.VISIBLE
            else
                checkbox.visibility = View.GONE

            vectorButton.setOnClickListener {
                if (mSelectedDrawable != drawable) {
                    notifyItemChanged(getVectorPosition(mSelectedDrawable))
                    mSelectedDrawable = drawable
                    checkbox.visibility = View.VISIBLE
                    onVectorClick?.invoke(drawable)
                }
            }
            vectorButton.setOnLongClickListener {
                try {
                    val iconName = context.resources.getResourceEntryName(drawable)
                        .replace(
                            context.getString(R.string.underscore_delimiter),
                            context.getString(R.string.space_delimiter)
                        )
                        .capitalize()

                    DynamicToast.make(
                        context,
                        iconName,
                        AppCompatResources.getDrawable(context, drawable),
                        tempPreferences.tempVectorColor,
                        tempPreferences.tempBackgroundColor
                    )
                        .show()

                } catch (e: Exception) {
                    e.printStackTrace()
                    DynamicToast.makeError(
                        context,
                        context.getString(R.string.error_get_resource),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                return@setOnLongClickListener false
            }
        }
    }
}
