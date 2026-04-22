package com.cat.catpiano.music.activity_app.play

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class InstrumentSoundPlayer(private val context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(8) // allow up to 10 simultaneous sounds (multi-touch friendly)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // Maps note index (1–8) to the SoundPool sound ID
    private val soundMap = mutableMapOf<Int, Int>()
    // Tracks active stream IDs so we can stop them explicitly
    private val activeStreams = mutableListOf<Int>()

    // Load all sounds for the given instrument from assets
    // e.g. instrument/let_play/piano/1.mp3 ... 8.mp3
    fun load(instrumentId: String, subfolder: String = "let_play") {
        soundMap.values.forEach { soundPool.unload(it) }
        soundMap.clear()

        val folder = "instrument/$subfolder/$instrumentId"
        val count = context.assets.list(folder)?.count { it.endsWith(".mp3") } ?: 0
        for (note in 1..count) {
            val afd = context.assets.openFd("$folder/$note.mp3")
            soundMap[note] = soundPool.load(afd, 1)
        }
    }

    // Play the sound for the given note index (1–8)
    fun play(note: Int) {
        soundMap[note]?.let { soundId ->
            val streamId = soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
            if (streamId != 0) activeStreams.add(streamId)
        }
    }

    fun pause() { soundPool.autoPause() }

    fun resume() { soundPool.autoResume() }

    // Stop all currently playing streams — use when leaving the screen permanently
    fun stopAll() {
        activeStreams.forEach { soundPool.stop(it) }
        activeStreams.clear()
    }

    // Call this in onDestroy to free memory
    fun release() {
        soundPool.release()
        soundMap.clear()
    }
}
