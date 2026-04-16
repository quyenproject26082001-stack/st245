package com.temm.activity_app.character

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
import com.temm.data.model.custom.CharacterModel
import com.temm.databinding.FragmentCharaceterBinding
import com.temm.dialog.YesNoDialog

// BottomSheetDialogFragment: a Fragment that slides up from the bottom of the screen
class CharacterFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentCharaceterBinding
    private val adapter = CharacterAdapter()

    // Y position of the bottom navigation buttons — dialog window stops here
    var windowHeight: Int = 0

    // PlayActivity sets this to receive the selected character skin id
    var onCharacterSelected: ((CharacterModel) -> Unit)? = null

    // Step 1: inflate the layout fragment_characeter.xml
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCharaceterBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Resize the dialog window to stop at the top of the bottom navigation buttons
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, windowHeight)
            setGravity(Gravity.BOTTOM) // anchor window at top so it ends exactly at the nav buttons
        }
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    // Step 2: View is ready — set up adapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rcvCharacter.adapter = adapter
        adapter.submitList(loadSkinsFromAssets()) // push the list into the adapter

        adapter.onItemClick = { clickedItem ->
            when {
                // State: unlock — item is locked, show dialog to watch video
                !clickedItem.isUnlocked -> showUnlockDialog(clickedItem)

                // State: use — item is unlocked but not active, select it
                !clickedItem.isSelected -> {
                    val pref = SharePreferenceHelper(requireContext())
                    pref.setSelectedCharacter(clickedItem.id)

                    val newList = adapter.currentList.map { it.copy(isSelected = it.id == clickedItem.id) }
                    adapter.submitList(newList)

                    onCharacterSelected?.invoke(clickedItem)
                    dismiss()
                }

                // State: used — already the active item, just close
                else -> dismiss()
            }
        }
    }

    // Shows YesNoDialog; btnBottomRight (Yes) unlocks the item
    private fun showUnlockDialog(item: CharacterModel) {
        val dialog = YesNoDialog(
            requireActivity(),
            R.string.unlock,
            R.string.watch_video_to_unlock_this_item
        )
        dialog.onYesClick = {
            val pref = SharePreferenceHelper(requireContext())
            val unlocked = pref.getUnlockedCharacters()
            unlocked.add(item.id)
            pref.setUnlockedCharacters(unlocked)

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

    // Scans assets/skin/ folder; applies unlock/selected state from SharedPreferences
    private fun loadSkinsFromAssets(): List<CharacterModel> {
        val pref = SharePreferenceHelper(requireContext())
        val unlockedIds = pref.getUnlockedCharacters()
        var selectedId = pref.getSelectedCharacter()

        val folders = requireContext().assets.list("skin") ?: return emptyList()
        val validFolders = folders.filter {
            requireContext().assets.list("skin/$it")?.contains("avatar.png") == true
        }

        // First item is always unlocked and selected by default on first launch
        if (validFolders.isNotEmpty()) {
            val firstId = validFolders.first()
            if (unlockedIds.isEmpty()) {
                unlockedIds.add(firstId)
                pref.setUnlockedCharacters(unlockedIds)
            }
            if (selectedId.isEmpty()) {
                selectedId = firstId
                pref.setSelectedCharacter(selectedId)
            }
        }

        return validFolders.map { folder ->
            CharacterModel(
                id = folder,
                avatarPath = "skin/$folder/avatar.png",
                isUnlocked = unlockedIds.contains(folder),
                isSelected = folder == selectedId
            )
        }
    }

    companion object {
        const val TAG = "CharacterFragment" // used when calling .show()
    }
}
