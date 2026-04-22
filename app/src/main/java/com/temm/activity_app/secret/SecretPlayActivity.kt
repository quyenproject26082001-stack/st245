package com.temm.activity_app.secret

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.temm.R
import com.temm.activity_app.play.InstrumentSoundPlayer
import com.temm.core.extensions.hideNavigation
import com.temm.core.extensions.animateScaleEffect
import com.temm.core.helper.NoteIconManager
import com.temm.core.helper.SharePreferenceHelper
import com.temm.databinding.ActivitySecretPlayBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SecretPlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecretPlayBinding
    private val soundPlayer by lazy { InstrumentSoundPlayer(this) }
    private val sharePreference by lazy { SharePreferenceHelper(this) }
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var noteIconManager: NoteIconManager

    // Instrument được truyền từ SecretActivity qua intent ("guitar", "harp", ...)
    private var currentInstrumentId = "piano"
    // Skin tương ứng với vị trí item: item 0 → skin "1", item 1 → skin "2", ...
    private var currentSkinId = "1"

    // Chuỗi nốt đúng đọc từ txt, vd: [1, 3, 5, 2, 4]
    private var targetSequence: List<Int> = emptyList()

    // Lịch sử nốt người chơi đã nhấn (chỉ giữ N nốt cuối = độ dài target)
    private val inputSequence = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySecretPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Nhận instrument và skin từ SecretActivity
        currentInstrumentId = intent.getStringExtra("instrument") ?: "piano"
        currentSkinId = intent.getStringExtra("skin") ?: "1"

        // Load âm thanh từ assets/instrument/secret_melody/{instrument}/
        soundPlayer.load(currentInstrumentId, "secret_melody")

        noteIconManager = NoteIconManager(this, binding.noteOverlay)
        noteIconManager.setRandomSpawnPoints(5, -150f..150f, -10f..150f)
        loadCharacterImage("default")
        loadBackground()
        setupNoteLayout()

        // Load sequence mục tiêu + hiển thị gợi ý lên tvDescription
        setupSongGuide()

        setupNoteButtons()

        binding.btnBack.setOnClickListener { finish() }
    }

    // Xác định số nốt của instrument, ẩn/hiện layout phù hợp và wire buttons
    private fun setupNoteLayout() {
        val noteCount = assets.list("instrument/secret_melody/$currentInstrumentId")
            ?.count { it.endsWith(".mp3") } ?: 8
        if (noteCount <= 2) {
            binding.notes8.visibility = android.view.View.GONE
            binding.notes2.visibility = android.view.View.VISIBLE
        } else {
            binding.notes8.visibility = android.view.View.VISIBLE
            binding.notes2.visibility = android.view.View.GONE
        }
    }

    // Load background JSON đã chọn từ PlayActivity (dùng chung SharedPreferences)
    private fun loadBackground() {
        lifecycleScope.launch {
            val savedBg = sharePreference.getSelectedBackground()
            val bgPath = if (savedBg.isNotEmpty()) "bg/$savedBg.json" else "bg/1.json"
            withContext(Dispatchers.IO) {
                com.airbnb.lottie.LottieCompositionFactory.fromAssetSync(this@SecretPlayActivity, bgPath).value
            }?.let { binding.btnBackground.setComposition(it) }
                ?: binding.btnBackground.setAnimation(bgPath)
            binding.btnBackground.playAnimation()
        }
    }

    // Đọc file assets/secret_melody_song/{instrument}.txt
    // Hiển thị nội dung lên tvDescription và parse sequence nốt (các số 1–8)
    private fun setupSongGuide() {
        try {
            val text = assets.open("secret_melody_song/$currentInstrumentId.txt")
                .bufferedReader().readText().trim()
            binding.tvDescription.text = text

            // Parse chuỗi mục tiêu: L/R → 1/2, chữ số → từng ký tự riêng lẻ (vd: "332" → [3,3,2])
            targetSequence = if (text.contains(Regex("[LRlr]"))) {
                text.filter { it == 'L' || it == 'l' || it == 'R' || it == 'r' }
                    .map { if (it.uppercaseChar() == 'L') 1 else 2 }
            } else {
                text.filter { it.isDigit() }.map { it.digitToInt() }
            }
        } catch (_: Exception) {}
    }

    // Gán sự kiện cho 8 nút bấm: phát âm thanh + animation + kiểm tra sequence
    private fun setupNoteButtons() {
        // Nút 1–4: animation sang trái
        binding.btn1.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(1); showLeft(); onNotePressed(1) }
        binding.btn2.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(2); showLeft(); onNotePressed(2) }
        binding.btn3.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(3); showLeft(); onNotePressed(3) }
        binding.btn4.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(4); showLeft(); onNotePressed(4) }
        // Nút 5–8: animation sang phải
        binding.btn5.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(5); showRight(); onNotePressed(5) }
        binding.btn6.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(6); showRight(); onNotePressed(6) }
        binding.btn7.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(7); showRight(); onNotePressed(7) }
        binding.btn8.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(8); showRight(); onNotePressed(8) }
        // Nút 2-note (kick, table): L=1, R=2
        binding.btnLeft.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(1); showLeft(); onNotePressed(1) }
        binding.btnRight.setOnClickListener { it.animateScaleEffect(); noteIconManager.showIcon(0, binding.btnCharacter); soundPlayer.play(2); showRight(); onNotePressed(2) }
    }

    // Ghi nhận nốt vừa nhấn, chỉ giữ N nốt cuối (N = độ dài target)
    // Nếu khớp hoàn toàn với targetSequence → mở DancesActivity
    private fun onNotePressed(note: Int) {
        if (targetSequence.isEmpty()) return

        inputSequence.add(note)

        // Cắt bớt phần đầu nếu vượt quá độ dài target
        if (inputSequence.size > targetSequence.size) {
            inputSequence.removeAt(0)
        }

        if (inputSequence == targetSequence) {
            inputSequence.clear()
            openDancesScreen()
        }
    }

    // Chuyển sang màn DancesActivity để phát video tương ứng
    private fun openDancesScreen() {
        val intent = Intent(this, DancesActivity::class.java)
        intent.putExtra("instrument", currentInstrumentId)
        startActivity(intent)
    }

    private fun showLeft() = showAction("left")
    private fun showRight() = showAction("right")

    // default → action → default để mỗi lần bấm đều thấy animation rõ
    private fun showAction(type: String) {
        handler.removeCallbacksAndMessages(null)
        loadCharacterImage("default")
        handler.postDelayed({ loadCharacterImage(type) }, 80)
        handler.postDelayed({ loadCharacterImage("default") }, 330)
    }

    // Load ảnh character từ assets/skin/1/{instrument}/{type}.png
    // Fallback theo thứ tự: type → active → default
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

    override fun onPause() {
        super.onPause()
        soundPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        hideNavigation()
        soundPlayer.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        noteIconManager.hideAllIcons()
        soundPlayer.release()
    }
}
