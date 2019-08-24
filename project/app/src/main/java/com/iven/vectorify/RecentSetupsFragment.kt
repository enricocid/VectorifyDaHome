package com.iven.vectorify

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iven.vectorify.adapters.RecentSetupsAdapter
import kotlinx.android.synthetic.main.recent_setups_fragment.*

class RecentSetupsFragment : BottomSheetDialogFragment() {

    private lateinit var mRecentSetupsInterface: RecentSetupsInterface

    fun setRecentSetupsInterface(recentSetupsInterface: RecentSetupsInterface) {
        mRecentSetupsInterface = recentSetupsInterface
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recent_setups_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(Color.TRANSPARENT)

        delete.setOnClickListener {
            clearRecentSetups()
        }

        if (context != null) {

            //setup recent setups
            recents_rv.layoutManager = GridLayoutManager(context, 4)
            recents_rv.setHasFixedSize(true)

            val recentSetupsAdapter =
                RecentSetupsAdapter(context!!, getString(R.string.delimiter), this@RecentSetupsFragment)
            recents_rv.adapter = recentSetupsAdapter

            recentSetupsAdapter.onRecentClick = { recent ->
                mRecentSetupsInterface.onRecentSelected(recent[0], recent[1], recent[2], recent[3])
                dismiss()
            }
        }
    }

    //clear recent setups
    private fun clearRecentSetups() {
        if (context != null) {
            MaterialDialog(context!!).show {

                cornerRadius(res = R.dimen.md_corner_radius)
                title(R.string.title_recent_setups)
                message(R.string.message_clear_recent_setups)
                positiveButton {
                    //add an empty list to preferences
                    mVectorifyPreferences.recentSetups = mutableSetOf()
                    this@RecentSetupsFragment.dismiss()
                }
                negativeButton { dismiss() }
            }
        }
    }

    interface RecentSetupsInterface {
        fun onRecentSelected(
            selectedBackgroundColor: Int,
            selectedVector: Int,
            selectedVectorColor: Int,
            selectedCategory: Int
        )
    }
}
