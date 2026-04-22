package com.cat.catpiano.music.activity_app.main

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.cat.catpiano.music.R
import com.cat.catpiano.music.core.base.BaseActivity
import com.cat.catpiano.music.core.extensions.rateApp
import com.cat.catpiano.music.core.extensions.select
import com.cat.catpiano.music.core.extensions.startIntentRightToLeft
import com.cat.catpiano.music.core.extensions.visible
import com.cat.catpiano.music.core.helper.MediaHelper
import com.cat.catpiano.music.core.utils.key.ValueKey
import com.cat.catpiano.music.core.utils.state.RateState
import com.cat.catpiano.music.databinding.ActivityHomeBinding
import com.cat.catpiano.music.activity_app.SettingsActivity
import com.cat.catpiano.music.activity_app.play.PlayActivity
import com.cat.catpiano.music.activity_app.secret.SecretActivity

import com.cat.catpiano.music.core.extensions.gone
import com.cat.catpiano.music.core.extensions.invisible
import com.cat.catpiano.music.core.extensions.setGradientTextHeightColor
import com.cat.catpiano.music.core.extensions.setOnSingleClick
import com.cat.catpiano.music.core.extensions.strings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.exitProcess

class MainActivity : BaseActivity<ActivityHomeBinding>() {

    override fun setViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        deleteTempFolder()
        setupTextStroke()

    }

    private fun setupTextStroke() {
        // Enable marquee effect for long text
        binding.tv1.isSelected = true
        binding.tv2.isSelected = true
    }

    override fun viewListener() {
        binding.apply {
            btnActionBarRight1.setOnSingleClick { startIntentRightToLeft(SettingsActivity::class.java) }

            btnPlayHome.setOnSingleClick {
                startIntentRightToLeft(PlayActivity::class.java)
            }

            btnSecret.setOnSingleClick { startIntentRightToLeft(SecretActivity::class.java) }



        }
    }

    override fun initActionBar() {
        binding.apply {
            btnActionBarRight1.setBackgroundResource(R.drawable.ic_settings)
            tvRightText.setText(R.string.settings)
            tvRightText.select()
            btnActionBarRight1.visible()
            btnActionBarRightText.visible()
        }
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (!sharePreference.getIsRate(this) && sharePreference.getCountBack() % 2 != 0) {
            rateApp(sharePreference) { state ->
                when (state) {
                    RateState.LESS3 -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            delay(1000)
                            finishAffinity()
                        }
                    }

                    RateState.GREATER3 -> {
                        // Thoát app sau khi rate
                        lifecycleScope.launch(Dispatchers.Main) {
                            delay(1000)
                            finishAffinity()
                        }
                    }
                    RateState.CANCEL -> {
                        lifecycleScope.launch {
                            sharePreference.setCountBack(sharePreference.getCountBack() + 1)
                            withContext(Dispatchers.Main) {
                                delay(1000)
                                finishAffinity()
                            }
                        }
                    }
                }
            }
        } else {
            sharePreference.setCountBack(sharePreference.getCountBack() + 1)
            finishAffinity()
        }
    }

    private fun deleteTempFolder() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dataTemp = MediaHelper.getImageInternal(this@MainActivity, ValueKey.DOWNLOAD_ALBUM_BACKGROUND)
            if (dataTemp.isNotEmpty()) {
                dataTemp.forEach {
                    val file = File(it)
                    file.delete()
                }
            }
        }
    }

    private fun updateText() {
        binding.apply {

            tv1.setText(R.string.play)
            tv2.setText(R.string.my_record)

            // Re-enable marquee after text update
            tv1.isSelected = true
            tv2.isSelected = true
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (sharePreference.isLanguageChanged()) {
            sharePreference.setLanguageChanged(false)
            recreate()
        } else {
            updateText()
        }
    }
}