package com.temm.activity_app.secret

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
                val dialog = YesNoDialog(this@SecretActivity, R.string.unlock_item_secret)
                dialog.onYesClick = {
                    secret.isLocked = false
                    // Lưu trạng thái mở khóa theo vị trí
                    prefs.edit().putBoolean("key_$position", secret.isLocked).apply()
                    adapter.notifyItemChanged(position)
                }
                dialog.show()
            } else {
                // Item đã mở khóa → chuyển sang màn chơi, truyền tên instrument
                val intent = Intent(this, SecretPlayActivity::class.java)
                intent.putExtra("instrument", secret.Instrument)
                startActivity(intent)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySecretBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Khởi tạo danh sách instrument, đọc trạng thái khóa từ SharedPreferences
        listSecret = listOf(
            Secret(R.drawable.img_secret1, getString(R.string.unlock_item_secret), prefs.getBoolean("key_guitar", false), "guitar"),
            Secret(R.drawable.img_secret2, getString(R.string.unlock_item_secret), prefs.getBoolean("key_harp", false), "harp"),
            Secret(R.drawable.img_secret3, getString(R.string.unlock_item_secret), prefs.getBoolean("key_kick", false), "kick"),
            Secret(R.drawable.img_secret4, getString(R.string.unlock_item_secret), prefs.getBoolean("key_pad", false), "pad"),
            Secret(R.drawable.img_secret5, getString(R.string.unlock_item_secret), prefs.getBoolean("key_table", false), "table"),
            Secret(R.drawable.img_secret6, getString(R.string.unlock_item_secret), prefs.getBoolean("key_ukulele", false), "ukulele"),
        )
        recyclerView()
    }

    override fun onResume() {
        super.onResume()
        hideNavigation()
    }
}
