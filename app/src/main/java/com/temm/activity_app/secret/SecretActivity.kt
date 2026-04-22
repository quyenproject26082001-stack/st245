package com.temm.activity_app.secret

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.temm.R
import com.temm.core.extensions.hideNavigation
import com.temm.databinding.ActivitySecretBinding
import com.temm.dialog.YesNoDialog
import kotlin.collections.listOf

class SecretActivity : AppCompatActivity() {

    // Lưu trạng thái khóa/mở của từng item vào SharedPreferences
    private val prefs by lazy {
        getSharedPreferences("secret_prefs", Context.MODE_PRIVATE)
    }

    private lateinit var binding: ActivitySecretBinding
    private lateinit var adapter: SecretAdapter
    private lateinit var listSecret: List<Secret>

    private fun recyclerView() {
        adapter = SecretAdapter()
        adapter.submitList(listSecret)
        binding.rcvSecret.adapter = adapter

        adapter.onItemClick = { secret, position ->
            if (secret.isLocked) {
                // Item đang khóa → hỏi người dùng có muốn mở khóa không
                val dialog = YesNoDialog(this@SecretActivity, R.string.watch_video_to_unlock_this_item)
                dialog.onYesClick = {
                    secret.isLocked = false
                    prefs.edit().putBoolean("key_${secret.Instrument}", false).apply()
                    adapter.notifyItemChanged(position)
                    dialog.dismiss()   // ← thiếu cái này

                }
                dialog.onNoClick = {
                    dialog.dismiss()
                }
                dialog.show()
            } else {
                // Item đã mở khóa → chuyển sang màn chơi, truyền instrument + skin theo vị trí
                val intent = Intent(this, SecretPlayActivity::class.java)
                intent.putExtra("instrument", secret.Instrument)
                intent.putExtra("skin", (position + 1).toString())
                startActivity(intent)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecretBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo danh sách instrument, đọc trạng thái khóa từ SharedPreferences
        listSecret = listOf(
            Secret(R.drawable.img_secret1, getString(R.string.unlock_item_secret), prefs.getBoolean("key_guitar", false), "guitar"),
            Secret(R.drawable.img_secret2, getString(R.string.unlock_item_secret), prefs.getBoolean("key_harp", true), "harp"),
            Secret(R.drawable.img_secret3, getString(R.string.unlock_item_secret), prefs.getBoolean("key_kick", true), "kick"),
            Secret(R.drawable.img_secret4, getString(R.string.unlock_item_secret), prefs.getBoolean("key_pad", true), "pad"),
            Secret(R.drawable.img_secret5, getString(R.string.unlock_item_secret), prefs.getBoolean("key_table", true), "table"),
            Secret(R.drawable.img_secret6, getString(R.string.unlock_item_secret), prefs.getBoolean("key_ukulele", true), "ukulele"),
        )
        recyclerView()

        binding.btnBack.setOnClickListener { finish() }

    }

    override fun onResume() {
        super.onResume()
        hideNavigation()
    }
}
