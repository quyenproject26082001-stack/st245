package com.temm.activity_app.background

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.temm.R
import com.temm.core.helper.SharePreferenceHelper
import com.temm.data.model.custom.BackgroundItemModel
import com.temm.databinding.FragmentBackgroundBinding
import com.temm.dialog.YesNoDialog

// BottomSheetDialogFragment: a Fragment that slides up from the bottom of the screen
class BackgroundFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBackgroundBinding
    private val adapter = BackgroundAdapter()

    // Y position of the bottom navigation buttons — dialog window stops here
    var windowHeight: Int = 0

    // PlayActivity sets this to receive the selected background
    var onBackgroundSelected: ((BackgroundItemModel) -> Unit)? = null

    // Step 1: inflate the layout fragment_background.xml
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBackgroundBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Resize the dialog window to stop at the top of the bottom navigation buttons
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, windowHeight)
            setGravity(Gravity.BOTTOM)
        }
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    // Step 2: View is ready — set up adapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rcvBackground.adapter = adapter
        adapter.submitList(loadBackgroundsFromAssets()) // push the list into the adapter

        adapter.onItemClick = { clickedItem ->
            when {
                // State: unlock — item is locked, show dialog to watch video
                !clickedItem.isUnlocked -> showUnlockDialog(clickedItem)

                // State: use — item is unlocked but not active, select it
                !clickedItem.isSelected -> {
                    val pref = SharePreferenceHelper(requireContext())
                    pref.setSelectedBackground(clickedItem.id)

                    val newList = adapter.currentList.map { it.copy(isSelected = it.id == clickedItem.id) }
                    adapter.submitList(newList)

                    onBackgroundSelected?.invoke(clickedItem)
                    dismiss()
                }

                // State: used — already the active item, just close
                else -> dismiss()
            }
        }
    }

    // Shows YesNoDialog; btnBottomRight (Yes) unlocks the item
    private fun showUnlockDialog(item: BackgroundItemModel) {
        val dialog = YesNoDialog(
            requireActivity(),
            R.string.unlock,
            R.string.watch_video_to_unlock_this_item
        )
        dialog.onYesClick = {
            val pref = SharePreferenceHelper(requireContext())
            val unlocked = pref.getUnlockedBackgrounds()
            unlocked.add(item.id)
            pref.setUnlockedBackgrounds(unlocked)

            // Refresh list so this item now shows "Use" state
            val newList = adapter.currentList.map {
                if (it.id == item.id) it.copy(isUnlocked = true) else it
            }
            adapter.submitList(newList)
            dialog.dismiss()
        }
        dialog.onNoClick = { dialog.dismiss() }
        dialog.onDismissClick = { dialog.dismiss() }
        dialog.show()
    }

    // Scans assets/bg/; applies unlock/selected state from SharedPreferences
    private fun loadBackgroundsFromAssets(): List<BackgroundItemModel> {
        val pref = SharePreferenceHelper(requireContext())
        val unlockedIds = pref.getUnlockedBackgrounds()
        var selectedId = pref.getSelectedBackground()

        val files = requireContext().assets.list("bg") ?: return emptyList()
        val validFiles = files.filter { it.endsWith(".json") }

        // First item is always unlocked and selected by default on first launch
        if (validFiles.isNotEmpty()) {
            val firstId = validFiles.first().removeSuffix(".json")
            if (unlockedIds.isEmpty()) {
                unlockedIds.add(firstId)
                pref.setUnlockedBackgrounds(unlockedIds)
            }
            if (selectedId.isEmpty()) {
                selectedId = firstId
                pref.setSelectedBackground(selectedId)
            }
        }

        return validFiles.map { file ->
            val id = file.removeSuffix(".json") // "1.json" → "1"
            BackgroundItemModel(
                id = id,
                jsonPath = "bg/$file",
                isUnlocked = unlockedIds.contains(id),
                isSelected = id == selectedId
            )
        }
    }

    companion object {
        const val TAG = "BackgroundFragment" // used when calling .show()
    }
}
