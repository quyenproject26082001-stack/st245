package com.temm.activity_app.instrument

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.temm.data.model.custom.InstrumentModel
import com.temm.databinding.FragmentInstrumentBinding

// BottomSheetDialogFragment: a Fragment that slides up from the bottom of the screen
class InstrumentFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentInstrumentBinding
    private val adapter = InstrumentAdapter()

    // PlayActivity sets this to receive the selected instrument
    var onInstrumentSelected: ((InstrumentModel) -> Unit)? = null

    // Y position of the bottom navigation buttons — dialog window stops here
    var windowHeight: Int = 0

    // Step 1: inflate the layout fragment_instrument.xml
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentInstrumentBinding.inflate(inflater, container, false)
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

        binding.rcvInstrument.adapter = adapter
        adapter.submitList(loadInstrumentsFromAssets())

        adapter.onItemClick = { clickedItem ->
            // Update selection state in the list
            val newList = adapter.currentList.map { it.copy(isSelected = it.id == clickedItem.id) }
            adapter.submitList(newList)

            // Send the selected instrument back to PlayActivity, then close
            onInstrumentSelected?.invoke(clickedItem)
            dismiss()
        }
    }

    // Scans assets/instrument/let_play/, counts .mp3 files to determine noteCount (2 or 8)
    private fun loadInstrumentsFromAssets(): List<InstrumentModel> {
        val folders = requireContext().assets.list("instrument/let_play") ?: return emptyList()
        return folders
            .filter { requireContext().assets.list("instrument/let_play/$it")?.contains("nav.png") == true }
            .map { folder ->
                val files = requireContext().assets.list("instrument/let_play/$folder") ?: emptyArray()
                val noteCount = files.count { it.endsWith(".mp3") }
                InstrumentModel(id = folder, navPath = "instrument/let_play/$folder/nav.png", noteCount = noteCount)
            }
    }

    companion object {
        const val TAG = "InstrumentFragment" // used when calling .show()
    }
}
