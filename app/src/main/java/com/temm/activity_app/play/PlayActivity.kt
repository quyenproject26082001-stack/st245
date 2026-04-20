package com.temm.activity_app.play

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.text.StaticLayout
import android.text.TextPaint
import android.view.MotionEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.temm.R
import com.temm.core.extensions.animateScaleEffect
import com.temm.core.helper.NoteIconManager
import com.temm.activity_app.background.BackgroundAdapter
import com.temm.activity_app.character.CharacterAdapter
import com.temm.activity_app.instrument.InstrumentAdapter
import com.temm.core.base.BaseActivity
import com.temm.data.model.custom.BackgroundItemModel
import com.temm.data.model.custom.CharacterModel
import com.temm.data.model.custom.InstrumentModel
import com.temm.databinding.ActivityPlayBinding
import com.temm.dialog.YesNoDialog
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayActivity : BaseActivity<ActivityPlayBinding>() {

    private val soundPlayer by lazy { InstrumentSoundPlayer(this) }

    private var currentSkinId = "1"         // tracks which skin folder to load images from
    private var currentInstrumentId =
        "piano" // tracks which instrument subfolder to load images from
    private val handler = Handler(Looper.getMainLooper())

    private val characterAdapter = CharacterAdapter()
    private val backgroundAdapter = BackgroundAdapter()
    private val instrumentAdapter = InstrumentAdapter()
    private val lottieCache = HashMap<String, LottieComposition>()

    private lateinit var noteIconManager: NoteIconManager
    private data class Song(val name: String, val notes: String)
    private var songs: List<Song> = emptyList()
    private var currentSongIndex = 0

    override fun setViewBinding() = ActivityPlayBinding.inflate(layoutInflater)

    override fun initView() {
        val savedCharacter = sharePreference.getSelectedCharacter()
        if (savedCharacter.isNotEmpty()) currentSkinId = savedCharacter
        val savedInstrument = sharePreference.getSelectedInstrument().lowercase()
        if (savedInstrument.isNotEmpty()) currentInstrumentId = savedInstrument

        soundPlayer.load(currentInstrumentId)
        binding.notes8.visibility = View.VISIBLE
        binding.notes2.visibility = View.GONE
        showDefault()

        binding.btnBackground.setFailureListener {}
        noteIconManager = NoteIconManager(this, binding.noteOverlay)
        noteIconManager.setRandomSpawnPoints(5, -150f..150f, -10f..150f)

        setupCharacterPanel()
        setupBackgroundPanel()
        setupInstrumentPanel()
        setupSongGuide()

        lifecycleScope.launch {
            // Load and display the selected background first so it appears immediately
            val savedBg = sharePreference.getSelectedBackground()
            val selectedBgPath = if (savedBg.isNotEmpty()) "bg/$savedBg.json" else "bg/1.json"
            withContext(Dispatchers.IO) {
                LottieCompositionFactory.fromAssetSync(this@PlayActivity, selectedBgPath).value
                    ?.let { lottieCache[selectedBgPath] = it }
            }
            lottieCache[selectedBgPath]?.let { binding.btnBackground.setComposition(it) }
                ?: binding.btnBackground.setAnimation(selectedBgPath)
            binding.btnBackground.playAnimation()

            // Load all three in parallel — total wait = slowest, not sum
            val charactersAsync = async(Dispatchers.IO) { loadCharacters() }
            val backgroundsAsync = async(Dispatchers.IO) { loadBackgrounds() }
            val instrumentsAsync = async(Dispatchers.IO) { loadInstruments() }

            characterAdapter.submitList(charactersAsync.await())
            val backgrounds = backgroundsAsync.await()
            backgroundAdapter.submitList(backgrounds)
            val instruments = instrumentsAsync.await()
            instrumentAdapter.submitList(instruments)
            instruments.find { it.isSelected }?.let { updateNoteLayout(it.noteCount) }

            // Preload remaining Lottie files in background (selected already cached above)
            withContext(Dispatchers.IO) {
                backgrounds.filter { it.jsonPath != selectedBgPath }.forEach { bg ->
                    LottieCompositionFactory.fromAssetSync(this@PlayActivity, bg.jsonPath).value
                        ?.let { lottieCache[bg.jsonPath] = it }
                }
            }
        }
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


    private fun openPanel(panel: View) {
        binding.panelCharacter.visibility = View.GONE
        binding.panelBackground.visibility = View.GONE
        binding.panelInstrument.visibility = View.GONE
        panel.visibility = View.VISIBLE
        binding.panelScrim.visibility = View.VISIBLE
    }

    private fun closePanelIfOpen(): Boolean {
        return when (View.VISIBLE) {
            binding.panelCharacter.visibility,
            binding.panelBackground.visibility,
            binding.panelInstrument.visibility -> {
                binding.panelCharacter.visibility = View.GONE
                binding.panelBackground.visibility = View.GONE
                binding.panelInstrument.visibility = View.GONE
                binding.panelScrim.visibility = View.GONE
                true
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
        val candidates = if (type == "default") listOf("default") else listOf(type, "active", "default")
        for (name in candidates) {
            try {
                val stream = assets.open("skin/$currentSkinId/$currentInstrumentId/$name.png")
                binding.btnCharacter.setImageDrawable(Drawable.createFromStream(stream, null))
                stream.close()
                return
            } catch (_: Exception) {}
        }
    }

    // Bottom bar buttons toggle their respective panels
    private fun setupBottomBar() {
        binding.panelScrim.setOnClickListener { closePanelIfOpen() }

        binding.btnBackgroundFragment.setOnClickListener {
            if (binding.panelBackground.visibility == View.VISIBLE) closePanelIfOpen()
            else openPanel(binding.panelBackground)
        }

        binding.btnCharacterFgagment.setOnClickListener {
            if (binding.panelCharacter.visibility == View.VISIBLE) closePanelIfOpen()
            else openPanel(binding.panelCharacter)
        }

        binding.btnInstrumentFragment.setOnClickListener {
            if (binding.panelInstrument.visibility == View.VISIBLE) closePanelIfOpen()
            else openPanel(binding.panelInstrument)
        }
    }

    private fun setupCharacterPanel() {
        binding.rcvCharacterPanel.adapter = characterAdapter
        characterAdapter.onItemClick = { clickedItem ->
            when {
                !clickedItem.isUnlocked -> showUnlockCharacterDialog(clickedItem)
                !clickedItem.isSelected -> {
                    sharePreference.setSelectedCharacter(clickedItem.id)
                    characterAdapter.submitList(characterAdapter.currentList.map {
                        it.copy(isSelected = it.id == clickedItem.id)
                    })
                    currentSkinId = clickedItem.id
                    lifecycleScope.launch {
                        val instruments = withContext(Dispatchers.IO) { loadInstruments() }
                        instrumentAdapter.submitList(instruments)
                        instruments.find { it.isSelected }?.let {
                            soundPlayer.load(it.id)
                            updateNoteLayout(it.noteCount)
                        }
                    }
                    showDefault()
                    closePanelIfOpen()
                }
                else -> closePanelIfOpen()
            }
        }
    }

    private fun loadCharacters(): List<CharacterModel> {
        val unlockedIds = sharePreference.getUnlockedCharacters()
        var selectedId = sharePreference.getSelectedCharacter()
        val folders = assets.list("skin") ?: return emptyList()
        val validFolders = folders.filter { assets.list("skin/$it")?.contains("avatar.png") == true }
        if (validFolders.isNotEmpty()) {
            val firstId = validFolders.first()
            if (unlockedIds.isEmpty()) {
                unlockedIds.add(firstId); sharePreference.setUnlockedCharacters(unlockedIds)
            }
            if (selectedId.isEmpty()) {
                selectedId = firstId; sharePreference.setSelectedCharacter(selectedId)
            }
        }
        val unlockAll = sharePreference.getUnlockAll()
        return validFolders.map { folder ->
            val avatarPath = "skin/$folder/avatar.png"
            val drawable = try { assets.open(avatarPath).use { Drawable.createFromStream(it, null) } } catch (e: Exception) { null }
            CharacterModel(
                id = folder, avatarPath = avatarPath,
                isUnlocked = unlockAll || unlockedIds.contains(folder),
                isSelected = folder == selectedId,
                drawable = drawable
            )
        }
    }

    private fun showUnlockCharacterDialog(item: CharacterModel) {
        val dialog = YesNoDialog(this, R.string.unlock, R.string.watch_video_to_unlock_this_item)
        dialog.onYesClick = {
            val unlocked = sharePreference.getUnlockedCharacters()
            unlocked.add(item.id)
            sharePreference.setUnlockedCharacters(unlocked)
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
        backgroundAdapter.onItemClick = { clickedItem ->
            when {
                !clickedItem.isUnlocked -> showUnlockBackgroundDialog(clickedItem)
                !clickedItem.isSelected -> {
                    sharePreference.setSelectedBackground(clickedItem.id)
                    backgroundAdapter.submitList(backgroundAdapter.currentList.map {
                        it.copy(isSelected = it.id == clickedItem.id)
                    })
                    lottieCache[clickedItem.jsonPath]?.let { binding.btnBackground.setComposition(it) }
                        ?: binding.btnBackground.setAnimation(clickedItem.jsonPath)
                    binding.btnBackground.playAnimation()
                    closePanelIfOpen()
                }
                else -> closePanelIfOpen()
            }
        }
    }

    private fun loadBackgrounds(): List<BackgroundItemModel> {
        val unlockedIds = sharePreference.getUnlockedBackgrounds()
        var selectedId = sharePreference.getSelectedBackground()
        val files = assets.list("bg") ?: return emptyList()
        val validFiles = files.filter { it.endsWith(".json") }
        if (validFiles.isNotEmpty()) {
            val firstId = validFiles.first().removeSuffix(".json")
            if (unlockedIds.isEmpty()) {
                unlockedIds.add(firstId); sharePreference.setUnlockedBackgrounds(unlockedIds)
            }
            if (selectedId.isEmpty()) {
                selectedId = firstId; sharePreference.setSelectedBackground(selectedId)
            }
        }
        val unlockAll = sharePreference.getUnlockAll()
        return validFiles.map { file ->
            val id = file.removeSuffix(".json")
            val resId = resources.getIdentifier("bg_$id", "drawable", packageName)
            BackgroundItemModel(
                id = id, jsonPath = "bg/$file",
                isUnlocked = unlockAll || unlockedIds.contains(id),
                isSelected = id == selectedId,
                previewResId = resId
            )
        }
    }

    private fun showUnlockBackgroundDialog(item: BackgroundItemModel) {
        val dialog = YesNoDialog(this, R.string.unlock, R.string.watch_video_to_unlock_this_item)
        dialog.onYesClick = {
            val unlocked = sharePreference.getUnlockedBackgrounds()
            unlocked.add(item.id)
            sharePreference.setUnlockedBackgrounds(unlocked)
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
        instrumentAdapter.onItemClick = { clickedItem ->
            when {
                !clickedItem.isUnlocked -> showUnlockInstrumentDialog(clickedItem)
                !clickedItem.isSelected -> {
                    sharePreference.setSelectedInstrument(clickedItem.id)
                    instrumentAdapter.submitList(instrumentAdapter.currentList.map {
                        it.copy(isSelected = it.id == clickedItem.id)
                    })
                    currentInstrumentId = clickedItem.id
                    soundPlayer.load(clickedItem.id)
                    showDefault()
                    updateNoteLayout(clickedItem.noteCount)
                    closePanelIfOpen()
                }
                else -> closePanelIfOpen()
            }
        }
    }

    private fun updateNoteLayout(noteCount: Int) {
        if (noteCount == 8) {
            binding.notes8.visibility = View.VISIBLE
            binding.notes2.visibility = View.GONE
        } else {
            binding.notes2.visibility = View.VISIBLE
            binding.notes8.visibility = View.GONE
        }
    }

    private fun loadInstruments(): List<InstrumentModel> {
        val unlockedIds = sharePreference.getUnlockedInstruments()
        var selectedId = sharePreference.getSelectedInstrument()
        val folders = assets.list("instrument/let_play") ?: return emptyList()
        val validFolders = folders.filter {
            assets.list("instrument/let_play/$it")?.contains("nav.png") == true
        }
        val availableFolders = validFolders.filter {
            assets.list("skin/$currentSkinId/$it") != null
        }
        if (availableFolders.isNotEmpty()) {
            val defaultId = if (availableFolders.contains("piano")) "piano" else availableFolders.first()
            if (unlockedIds.isEmpty()) {
                unlockedIds.add(defaultId); sharePreference.setUnlockedInstruments(unlockedIds)
            }
            if (selectedId.isEmpty() || !availableFolders.contains(selectedId)) {
                selectedId = defaultId; sharePreference.setSelectedInstrument(selectedId)
            }
        }
        if (currentInstrumentId !in availableFolders) {
            currentInstrumentId = availableFolders.firstOrNull() ?: currentInstrumentId
        }
        val unlockAll = sharePreference.getUnlockAll()
        return availableFolders.map { folder ->
            val navPath = "instrument/let_play/$folder/nav.png"
            val files = assets.list("instrument/let_play/$folder") ?: emptyArray()
            val noteCount = files.count { it.endsWith(".mp3") }
            val drawable = try { assets.open(navPath).use { Drawable.createFromStream(it, null) } } catch (e: Exception) { null }
            InstrumentModel(
                id = folder, navPath = navPath, noteCount = noteCount,
                isUnlocked = unlockAll || unlockedIds.contains(folder),
                isSelected = folder == selectedId,
                drawable = drawable
            )
        }
    }

    private fun showUnlockInstrumentDialog(item: InstrumentModel) {
        val dialog = YesNoDialog(this, R.string.unlock, R.string.watch_video_to_unlock_this_item)
        dialog.onYesClick = {
            val unlocked = sharePreference.getUnlockedInstruments()
            unlocked.add(item.id)
            sharePreference.setUnlockedInstruments(unlocked)
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
        // Step 1: Read all .txt files from assets/song/ and store them as Song objects
        songs = assets.list("song")
            ?.filter { it.endsWith(".txt") }
            ?.map { fileName ->
                val name = fileName.removeSuffix(".txt")
                val notes = assets.open("song/$fileName").bufferedReader().readText().trim()
                Song(name, notes)
            } ?: emptyList()

        if (songs.isEmpty()) return

        // Step 2: Show the current song on screen
        showSong(currentSongIndex)

        // Step 3: Make btnNext / btnPrevious cycle through songs while held
        setupRepeatButton(binding.btnPrevious, step = -1)
        setupRepeatButton(binding.btnNext, step = +1)
    }

    // Shows the song at the given index: updates song name label and note description
    private fun showSong(index: Int) {
        val song = songs[index]
        binding.songName.text = song.name
        binding.tvDescription.text = song.notes

        // tvDescription.width is 0 before the view is drawn for the first time.
        // post { } waits until the view finishes drawing, then we can read the real size.
        binding.tvDescription.post {
            adjustDescriptionTextSize(song)
        }
    }

    // Shrinks the text to 12sp if the notes take 3 or more lines; otherwise keeps 16sp.
    // We always measure at 16sp so the result is consistent regardless of current size.
    private fun adjustDescriptionTextSize(song: Song) {
        val notes = song.notes

        // Convert 16sp → pixels so StaticLayout can measure correctly
        val textSizeInPixels = 16f * resources.displayMetrics.scaledDensity

        // Copy the TextView's paint and force it to 16sp for measurement
        val paint = TextPaint(binding.tvDescription.paint)
        paint.textSize = textSizeInPixels

        // The usable width is the view width minus left/right padding
        val usableWidth = (binding.tvDescription.width
            - binding.tvDescription.paddingLeft
            - binding.tvDescription.paddingRight)
            .coerceAtLeast(1)

        // StaticLayout pre-measures how many lines the text would take at 16sp
        val lineCount = StaticLayout.Builder
            .obtain(notes, 0, notes.length, paint, usableWidth)
            .setLineSpacing(0f, 1f)
            .build()
            .lineCount

        val isLongText = lineCount >= 3

        if (isLongText) {
            binding.tvDescription.textSize = 12f
            binding.tvDescription.setLineSpacing(0f, 0.85f)
        } else {
            binding.tvDescription.textSize = 16f
            binding.tvDescription.setLineSpacing(0f, 1f)
        }
    }

    // Makes a button navigate songs while held down (repeats every 200ms after a 400ms delay)
    private fun setupRepeatButton(button: View, step: Int) {
        val repeatRunnable = object : Runnable {
            override fun run() {
                // Wrap around: going past the last song loops back to the first, and vice versa
                currentSongIndex = (currentSongIndex + step + songs.size) % songs.size
                showSong(currentSongIndex)
                handler.postDelayed(this, 200L)
            }
        }

        button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Navigate immediately on first press, then start repeating
                    currentSongIndex = (currentSongIndex + step + songs.size) % songs.size
                    showSong(currentSongIndex)
                    handler.postDelayed(repeatRunnable, 400L)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Stop repeating when the finger is lifted or the touch is cancelled
                    handler.removeCallbacks(repeatRunnable)
                }
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        noteIconManager.hideAllIcons()
        soundPlayer.release()
    }
}
