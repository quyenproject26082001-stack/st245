package com.cat.catpiano.music.activity_app.secret

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.cat.catpiano.music.R
import com.cat.catpiano.music.core.extensions.hideNavigation
import com.cat.catpiano.music.core.helper.LanguageHelper
import com.cat.catpiano.music.databinding.ActivitySecretBinding
import android.widget.Toast
import com.cat.catpiano.music.dialog.YesNoDialog
import com.google.android.gms.ads.rewarded.RewardItem
import com.lvt.ads.callback.RewardCallback
import com.lvt.ads.util.Admob
import kotlin.collections.listOf

class SecretActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.wrapContext(newBase))
    }

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
                var check = false
                dialog.onYesClick = {
                    Admob.getInstance().loadAndShowRewardAds(this@SecretActivity, getString(R.string.reward_unlockSecret),
                        object : RewardCallback() {
                            override fun onAdFailedToLoad() {
                                super.onAdFailedToLoad()
                                Toast.makeText(this@SecretActivity, R.string.ad_loading_failed, Toast.LENGTH_SHORT).show()
                            }
                            override fun onEarnedReward(rewardItem: RewardItem?) {
                                super.onEarnedReward(rewardItem)
                                check = true
                            }
                            override fun onAdClosed() {
                                super.onAdClosed()
                                if (check) {
                                    check = false
                                    secret.isLocked = false
                                    prefs.edit().putBoolean("key_${secret.Instrument}", false).apply()
                                    adapter.notifyItemChanged(position)
                                    dialog.dismiss()
                                } else {
                                    Toast.makeText(this@SecretActivity, R.string.ad_loading_failed, Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                }
                dialog.onNoClick = { dialog.dismiss() }
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

        initNativeCollab()
    }

    override fun onResume() {
        super.onResume()
        hideNavigation()
    }



    override fun onRestart() {
        super.onRestart()
        initNativeCollab()
    }
    fun initNativeCollab() {
        Admob.getInstance().loadNativeCollapNotBanner(this,getString(R.string.native_cl_Secret),binding.flNativeCollab)
    }
}
