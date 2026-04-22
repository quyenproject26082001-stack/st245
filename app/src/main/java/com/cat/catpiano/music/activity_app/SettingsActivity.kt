package com.cat.catpiano.music.activity_app

import android.view.LayoutInflater
import com.cat.catpiano.music.R
import com.cat.catpiano.music.core.base.BaseActivity
import com.cat.catpiano.music.core.extensions.gone
import com.cat.catpiano.music.core.extensions.handleBackLeftToRight
import com.cat.catpiano.music.core.extensions.policy
import com.cat.catpiano.music.core.extensions.rateApp
import com.cat.catpiano.music.core.extensions.select
import com.cat.catpiano.music.core.extensions.shareApp
import com.cat.catpiano.music.core.extensions.startIntentRightToLeft
import com.cat.catpiano.music.core.extensions.visible
import com.cat.catpiano.music.core.utils.key.IntentKey
import com.cat.catpiano.music.core.utils.state.RateState
import com.cat.catpiano.music.databinding.ActivitySettingsBinding
import com.cat.catpiano.music.activity_app.language.LanguageActivity
import com.cat.catpiano.music.core.extensions.setOnSingleClick
import com.cat.catpiano.music.core.extensions.strings
import kotlin.jvm.java

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRate()
        // TODO: re-enable before next release
        // initUnlockAll()
        binding.btnUnlockAll.gone()
    }

    override fun onResume() {
        super.onResume()
        if (sharePreference.isLanguageChanged()) {
            handleBackLeftToRight()
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.setOnSingleClick { handleBackLeftToRight() }
            btnLang.setOnSingleClick { startIntentRightToLeft(LanguageActivity::class.java, IntentKey.INTENT_KEY) }
            btnShare.setOnSingleClick(1500) { shareApp() }
            btnRate.setOnSingleClick {
                rateApp(sharePreference) { state ->
                    if (state != RateState.CANCEL) {
                        binding.btnRate.gone()
                    }
                }
            }
            btnPolicy.setOnSingleClick(1500) { policy() }
            // btnUnlockAll.setOnSingleClick { toggleUnlockAll() }
        }
    }

    override fun initText() {
        binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            tvCenter.text = strings(R.string.settings)
            tvCenter.visible()
            tvCenter.setText(R.string.settings)
            btnActionBarLeft.setImageResource(R.drawable.ic_back)
            btnActionBarLeft.visible()
        }
    }

    private fun initRate() {
        if (sharePreference.getIsRate(this)) {
            binding.btnRate.gone()
        } else {
            binding.btnRate.visible()
        }
    }

    private fun initUnlockAll() {
        updateUnlockAllToggle(sharePreference.getUnlockAll())
    }

    private fun toggleUnlockAll() {
        val newState = !sharePreference.getUnlockAll()
        sharePreference.setUnlockAll(newState)
        updateUnlockAllToggle(newState)
    }

    private fun updateUnlockAllToggle(enabled: Boolean) {
        binding.imgUnlockAllToggle.setImageResource(
            if (enabled) R.drawable.ic_sw_on else R.drawable.ic_sw_off
        )
    }
}