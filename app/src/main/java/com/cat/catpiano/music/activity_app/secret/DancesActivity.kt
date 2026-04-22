package com.cat.catpiano.music.activity_app.secret

import android.media.MediaPlayer
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.cat.catpiano.music.core.extensions.hideNavigation
import com.cat.catpiano.music.databinding.ActivityDancesBinding

class DancesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDancesBinding
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDancesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nhận tên instrument để xác định video cần phát (vd: "guitar", "harp")
        val instrument = intent.getStringExtra("instrument") ?: return

        binding.btnBack.setOnClickListener { finish() }

        // SurfaceView sẵn sàng → gắn MediaPlayer và phát video
        binding.surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                playVideo(instrument, holder)
            }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        })
    }

    // Phát video từ assets/video/{instrument}.mp4 trực tiếp qua FileDescriptor (không cần copy ra cache)
    private fun playVideo(instrument: String, holder: SurfaceHolder) {
        try {
            val afd = assets.openFd("video/$instrument.mp4")
            val mp = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                setSurface(holder.surface)
                isLooping = true
                prepare()
                start()
            }
            afd.close()
            mediaPlayer = mp
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        hideNavigation()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
