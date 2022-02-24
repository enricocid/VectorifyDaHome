package com.iven.vectorify.ui


import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.iven.vectorify.R
import com.iven.vectorify.adapters.RecentsAdapter
import com.iven.vectorify.databinding.ModalRvBinding
import com.iven.vectorify.models.VectorifyWallpaper
import com.iven.vectorify.utils.Utils
import com.iven.vectorify.vectorifyPreferences
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.windowInsetTypesOf


class RecentsSheet: BottomSheetDialogFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var _modalRvBinding: ModalRvBinding? = null

    var onRecentClick: ((VectorifyWallpaper) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _modalRvBinding = ModalRvBinding.inflate(inflater, container, false)
        return _modalRvBinding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceManager.getDefaultSharedPreferences(requireActivity())
            .unregisterOnSharedPreferenceChangeListener(this)
        _modalRvBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _modalRvBinding?.run {

            title.text = getString(R.string.title_recent_setups)
            deleteRecentsBtn.visibility = View.VISIBLE
            deleteRecentsBtn.setOnClickListener {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(R.string.title_recent_setups)
                    .setMessage(R.string.message_clear_recent_setups)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        if (Utils.isDeviceLand(resources)) {
                            vectorifyPreferences.recentSetupsLand = mutableListOf()
                        } else {
                            vectorifyPreferences.recentSetups = mutableListOf()
                        }
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            val recentsAdapter = RecentsAdapter(requireActivity())
            modalRv.itemAnimator = null
            modalRv.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            modalRv.adapter = recentsAdapter
            recentsAdapter.onRecentClick = { recent ->
                onRecentClick?.invoke(recent)
            }
        }

        PreferenceManager.getDefaultSharedPreferences(requireActivity())
            .registerOnSharedPreferenceChangeListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            dialog?.window?.navigationBarColor = ContextCompat.getColor(requireActivity(),
                R.color.activity_background_color)
            Insetter.builder()
                .padding(windowInsetTypesOf(navigationBars = true))
                .margin(windowInsetTypesOf(statusBars = true))
                .applyToView(view)
        }
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, key: String?) {
        when {
            Utils.isDeviceLand(resources) -> if (vectorifyPreferences.recentSetupsLand.isNullOrEmpty()) {
                dismiss()
            }
            else -> if (vectorifyPreferences.recentSetups.isNullOrEmpty()) {
                dismiss()
            }
        }
    }

    companion object {

        const val TAG_MODAL = "MODAL_BOTTOM_SHEET"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment [RecentsSheet].
         */
        @JvmStatic
        fun newInstance() = RecentsSheet()
    }
}
