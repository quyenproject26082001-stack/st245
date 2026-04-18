package com.temm.activity_app

import android.view.LayoutInflater
import com.temm.R
import com.temm.core.base.BaseActivity
import com.temm.core.extensions.gone
import com.temm.core.extensions.handleBackLeftToRight
import com.temm.core.extensions.policy
import com.temm.core.extensions.rateApp
import com.temm.core.extensions.select
import com.temm.core.extensions.shareApp
import com.temm.core.extensions.startIntentRightToLeft
import com.temm.core.extensions.visible
import com.temm.core.utils.key.IntentKey
import com.temm.core.utils.state.RateState
import com.temm.databinding.ActivitySettingsBinding
import com.temm.activity_app.language.LanguageActivity
import com.temm.core.extensions.setOnSingleClick
import com.temm.core.extensions.strings
import kotlin.jvm.java

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRate()
        initUnlockAll()
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
            btnUnlockAll.setOnSingleClick { toggleUnlockAll() }
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