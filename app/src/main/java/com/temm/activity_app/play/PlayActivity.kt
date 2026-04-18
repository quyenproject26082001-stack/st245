package com.temm.activity_app.play

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.temm.R
import com.temm.core.extensions.animateScaleEffect
import com.temm.core.helper.NoteIconManager
import com.temm.activity_app.background.BackgroundAdapter
import com.temm.activity_app.character.CharacterAdapter
import com.temm.activity_app.instrument.InstrumentAdapter
import com.temm.core.base.BaseActivity
import com.temm.core.helper.SharePreferenceHelper
import com.temm.data.model.custom.BackgroundItemModel
import com.temm.data.model.custom.CharacterModel
import com.temm.data.model.custom.InstrumentModel
import com.temm.databinding.ActivityPlayBinding
import com.temm.dialog.YesNoDialog

class PlayActivity : BaseActivity<ActivityPlayBinding>() {

    private val soundPlayer by lazy { InstrumentSoundPlayer(this) }

    private var currentSkinId = "1"         // tracks which skin folder to load images from
    private var currentInstrumentId =
        "piano" // tracks which instrument subfolder to load images from
    private val handler = Handler(Looper.getMainLooper())

    private val characterAdapter = CharacterAdapter()
    private val backgroundAdapter = BackgroundAdapter()
    private val instrumentAdapter = InstrumentAdapter()

    private lateinit var noteIconManager: NoteIconManager
    private data class Song(val name: String, val notes: String)
    private var songs: List<Song> = emptyList()
    private var currentSongIndex = 0

    override fun setViewBinding() = ActivityPlayBinding.inflate(layoutInflater)

    override fun initView() {
        soundPlayer.load("piano", 8)
        binding.notes8.visibility = View.VISIBLE
        binding.notes2.visibility = View.GONE
        showDefault()                         // show default character image on startup

        // Lottie 6.x throws by default when parsing fails — override to prevent crash
        binding.btnBackground.setFailureListener { /* ignore parse errors */ }
        binding.btnBackground.setAnimation("bg/1.json")
        binding.btnBackground.playAnimation()

        noteIconManager = NoteIconManager(this, binding.noteOverlay)
        noteIconManager.setRandomSpawnPoints(5,-150f..150f, -10f..150f) // pre-generate 5 random spawn points within a 300x300 area
        setupCharacterPanel()
        setupBackgroundPanel()
        setupInstrumentPanel()
        setupSongGuide()
    }

    override fun initActionBar() {}

    override fun viewListener() {
        setupNoteButtons()
        setupBottomBar()
        binding.btnBack.setOnClickListener {
            if (!closePanelIfOpen()) {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        onBackPressedDispatcher.addCallback(this, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!closePanelIfOpen()) finish()
            }
        })

    }


    private fun closePanelIfOpen(): Boolean {
        return when (View.VISIBLE) {
            binding.panelCharacter.visibility -> {
                binding.panelCharacter.visibility = View.GONE; true
            }

            binding.panelBackground.visibility -> {
                binding.panelBackground.visibility = View.GONE; true
            }

            binding.panelInstrument.visibility -> {
                binding.panelInstrument.visibility = View.GONE; true
            }

            else -> false
        }
    }

    // Wire each note button to its note index + character animation
    private fun setupNoteButtons() {
        // Buttons 1–4: play sound + flash left.png then return to default
        binding.btn1.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(1); showLeft() }
        binding.btn2.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(2); showLeft() }
        binding.btn3.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(3); showLeft() }
        binding.btn4.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(4); showLeft() }

        // Buttons 5–8: play sound + flash right.png then return to default
        binding.btn5.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(5); showRight() }
        binding.btn6.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(6); showRight() }
        binding.btn7.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(7); showRight() }
        binding.btn8.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(8); showRight() }

        // 2-note buttons
        binding.btnLeft.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(1); showLeft() }
        binding.btnRight.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(2); showRight() }
    }

    private fun showLeft() = showAction("left")
    private fun showRight() = showAction("right")

    private fun showDefault() {
        loadCharacterImage("default")
    }

    // default → action → default so every tap is visually distinct even when clicked rapidly
    private fun showAction(type: String) {
        handler.removeCallbacksAndMessages(null)
        showDefault()
        handler.postDelayed({ loadCharacterImage(type) }, 80)
        handler.postDelayed({ showDefault() }, 330)
    }

    // Load the given image type (default / left / right) from the current skin + instrument folder
    // Using InputStream directly from assets — more reliable than Glide for local asset files
    private fun loadCharacterImage(type: String) {
        try {
            val stream = assets.open("skin/$currentSkinId/$currentInstrumentId/$type.png")
            binding.btnCharacter.setImageDrawable(Drawable.createFromStream(stream, null))
            stream.close()
        } catch (e: Exception) {
            // Some instruments only have default.png — fall back silently
        }
    }

    // Bottom bar buttons toggle their respective panels
    private fun setupBottomBar() {
        binding.btnBackgroundFragment.setOnClickListener {
            val isVisible = binding.panelBackground.visibility == View.VISIBLE
            binding.panelCharacter.visibility = View.GONE
            binding.panelBackground.visibility = if (isVisible) View.GONE else View.VISIBLE
        }

        binding.btnCharacterFgagment.setOnClickListener {
            val isVisible = binding.panelCharacter.visibility == View.VISIBLE
            binding.panelBackground.visibility = View.GONE
            binding.panelCharacter.visibility = if (isVisible) View.GONE else View.VISIBLE
        }

        binding.btnInstrumentFragment.setOnClickListener {
            val isVisible = binding.panelInstrument.visibility == View.VISIBLE
            binding.panelCharacter.visibility = View.GONE
            binding.panelBackground.visibility = View.GONE
            binding.panelInstrument.visibility = if (isVisible) View.GONE else View.VISIBLE
        }
    }

    private fun setupCharacterPanel() {
        binding.rcvCharacterPanel.adapter = characterAdapter
        characterAdapter.submitList(loadCharacters())
        characterAdapter.onItemClick = { clickedItem ->
            when {
                !clickedItem.isUnlocked -> showUnlockCharacterDialog(clickedItem)
                !clickedItem.isSelected -> {
                    val pref = SharePreferenceHelper(this)
                    pref.setSelectedCharacter(clickedItem.id)
                    characterAdapter.submitList(characterAdapter.currentList.map {
                        it.copy(
                            isSelected = it.id == clickedItem.id
                        )
                    })
                    currentSkinId = clickedItem.id
                    instrumentAdapter.submitList(loadInstruments())
                    showDefault()
                    binding.panelCharacter.visibility = View.GONE
                }

                else -> binding.panelCharacter.visibility = View.GONE
            }
        }
    }

    private fun loadCharacters(): List<CharacterModel> {
        val pref = SharePreferenceHelper(this)
        val unlockedIds = pref.getUnlockedCharacters()
        var selectedId = pref.getSelectedCharacter()
        val folders = assets.list("skin") ?: return emptyList()
        val validFolders =
            folders.filter { assets.list("skin/$it")?.contains("avatar.png") == true }
        if (validFolders.isNotEmpty()) {
            val firstId = validFolders.first()
            if (unlockedIds.isEmpty()) {
                unlockedIds.add(firstId); pref.setUnlockedCharacters(unlockedIds)
            }
            if (selectedId.isEmpty()) {
                selectedId = firstId; pref.setSelectedCharacter(selectedId)
            }
        }
        return validFolders.map { folder ->
            CharacterModel(
                id = folder, avatarPath = "skin/$folder/avatar.png",
                isUnlocked = unlockedIds.contains(folder), isSelected = folder == selectedId
            )
        }
    }

    private fun showUnlockCharacterDialog(item: CharacterModel) {
        val dialog = YesNoDialog(this, R.string.unlock, R.string.watch_video_to_unlock_this_item)
        dialog.onYesClick = {
            val pref = SharePreferenceHelper(this)
            val unlocked = pref.getUnlockedCharacters()
            unlocked.add(item.id)
            pref.setUnlockedCharacters(unlocked)
            characterAdapter.submitList(characterAdapter.currentList.map {
                if (it.id == item.id) it.copy(isUnlocked = true) else it
            })
            dialog.dismiss()
        }
        dialog.onNoClick = { dialog.dismiss() }
        dialog.onDismissClick = { dialog.dismiss() }
        dialog.show()
    }

    private fun setupBackgroundPanel() {
        binding.rcvBackgroundPanel.adapter = backgroundAdapter
        backgroundAdapter.submitList(loadBackgrounds())
        backgroundAdapter.onItemClick = { clickedItem ->
            when {
                !clickedItem.isUnlocked -> showUnlockBackgroundDialog(clickedItem)
                !clickedItem.isSelected -> {
                    val pref = SharePreferenceHelper(this)
                    pref.setSelectedBackground(clickedItem.id)
                    backgroundAdapter.submitList(backgroundAdapter.currentList.map {
                        it.copy(
                            isSelected = it.id == clickedItem.id
                        )
                    })
                    binding.btnBackground.setFailureListener { }
                    binding.btnBackground.setAnimation(clickedItem.jsonPath)
                    binding.btnBackground.playAnimation()
                    binding.panelBackground.visibility = View.GONE
                }

                else -> binding.panelBackground.visibility = View.GONE
            }
        }
    }

    private fun loadBackgrounds(): List<BackgroundItemModel> {
        val pref = SharePreferenceHelper(this)
        val unlockedIds = pref.getUnlockedBackgrounds()
        var selectedId = pref.getSelectedBackground()
        val files = assets.list("bg") ?: return emptyList()
        val validFiles = files.filter { it.endsWith(".json") }
        if (validFiles.isNotEmpty()) {
            val firstId = validFiles.first().removeSuffix(".json")
            if (unlockedIds.isEmpty()) {
                unlockedIds.add(firstId); pref.setUnlockedBackgrounds(unlockedIds)
            }
            if (selectedId.isEmpty()) {
                selectedId = firstId; pref.setSelectedBackground(selectedId)
            }
        }
        return validFiles.map { file ->
            val id = file.removeSuffix(".json")
            BackgroundItemModel(
                id = id, jsonPath = "bg/$file",
                isUnlocked = unlockedIds.contains(id), isSelected = id == selectedId
            )
        }
    }

    private fun showUnlockBackgroundDialog(item: BackgroundItemModel) {
        val dialog = YesNoDialog(this, R.string.unlock, R.string.watch_video_to_unlock_this_item)
        dialog.onYesClick = {
            val pref = SharePreferenceHelper(this)
            val unlocked = pref.getUnlockedBackgrounds()
            unlocked.add(item.id)
            pref.setUnlockedBackgrounds(unlocked)
            backgroundAdapter.submitList(backgroundAdapter.currentList.map {
                if (it.id == item.id) it.copy(isUnlocked = true) else it
            })
            dialog.dismiss()
        }
        dialog.onNoClick = { dialog.dismiss() }
        dialog.onDismissClick = { dialog.dismiss() }
        dialog.show()
    }

    private fun setupInstrumentPanel() {
        binding.rcvInstrumentPanel.adapter = instrumentAdapter
        instrumentAdapter.submitList(loadInstruments())
        instrumentAdapter.onItemClick = { clickedItem ->
            when {
                !clickedItem.isUnlocked -> showUnlockInstrumentDialog(clickedItem)
                !clickedItem.isSelected -> {
                    val pref = SharePreferenceHelper(this)
                    pref.setSelectedInstrument(clickedItem.id)
                    instrumentAdapter.submitList(instrumentAdapter.currentList.map {
                        it.copy(isSelected = it.id == clickedItem.id)
                    })
                    currentInstrumentId = clickedItem.id
                    soundPlayer.load(clickedItem.id, clickedItem.noteCount)
                    showDefault()
                    if (clickedItem.noteCount == 8) {
                        binding.notes8.visibility = View.VISIBLE
                        binding.notes2.visibility = View.GONE
                    } else {
                        binding.notes2.visibility = View.VISIBLE
                        binding.notes8.visibility = View.GONE
                    }
                    binding.panelInstrument.visibility = View.GONE
                }
                else -> binding.panelInstrument.visibility = View.GONE
            }
        }
    }

    private fun loadInstruments(): List<InstrumentModel> {
        val pref = SharePreferenceHelper(this)
        val unlockedIds = pref.getUnlockedInstruments()
        var selectedId = pref.getSelectedInstrument()
        val folders = assets.list("instrument/let_play") ?: return emptyList()
        val validFolders = folders.filter {
            assets.list("instrument/let_play/$it")?.contains("nav.png") == true &&
            assets.list("skin/$currentSkinId/$it") != null
        }
        if (validFolders.isNotEmpty()) {
            val firstId = validFolders.first()
            if (unlockedIds.isEmpty()) {
                unlockedIds.add(firstId); pref.setUnlockedInstruments(unlockedIds)
            }
            if (selectedId.isEmpty() || !validFolders.contains(selectedId)) {
                selectedId = firstId; pref.setSelectedInstrument(selectedId)
            }
        }
        if (currentInstrumentId !in validFolders) {
            currentInstrumentId = validFolders.firstOrNull() ?: currentInstrumentId
        }
        return validFolders.map { folder ->
            val files = assets.list("instrument/let_play/$folder") ?: emptyArray()
            val noteCount = files.count { it.endsWith(".mp3") }
            InstrumentModel(
                id = folder,
                navPath = "instrument/let_play/$folder/nav.png",
                noteCount = noteCount,
                isUnlocked = unlockedIds.contains(folder),
                isSelected = folder == selectedId
            )
        }
    }

    private fun showUnlockInstrumentDialog(item: InstrumentModel) {
        val dialog = YesNoDialog(this, R.string.unlock, R.string.watch_video_to_unlock_this_item)
        dialog.onYesClick = {
            val pref = SharePreferenceHelper(this)
            val unlocked = pref.getUnlockedInstruments()
            unlocked.add(item.id)
            pref.setUnlockedInstruments(unlocked)
            instrumentAdapter.submitList(instrumentAdapter.currentList.map {
                if (it.id == item.id) it.copy(isUnlocked = true) else it
            })
            dialog.dismiss()
        }
        dialog.onNoClick = { dialog.dismiss() }
        dialog.onDismissClick = { dialog.dismiss() }
        dialog.show()
    }

    private fun setupSongGuide() {
        songs = assets.list("song")
            ?.filter { it.endsWith(".txt") }
            ?.map { fileName ->
                val name = fileName.removeSuffix(".txt")
                val notes = assets.open("song/$fileName").bufferedReader().readText().trim()
                Song(name, notes)
            } ?: emptyList()

        if (songs.isEmpty()) return

        fun showSong(index: Int) {
            binding.songName.text = songs[index].name
            binding.tvDescription.text = songs[index].notes
            binding.btnPrevious.isEnabled = index > 0
            binding.btnPrevious.alpha = if (index > 0) 1f else 0.3f
            binding.btnNext.isEnabled = index < songs.size - 1
            binding.btnNext.alpha = if (index < songs.size - 1) 1f else 0.3f
        }

        showSong(currentSongIndex)

        binding.btnPrevious.setOnClickListener {
            currentSongIndex--
            showSong(currentSongIndex)
        }
        binding.btnNext.setOnClickListener {
            currentSongIndex++
            showSong(currentSongIndex)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        noteIconManager.hideAllIcons()
        soundPlayer.release()
    }
}
