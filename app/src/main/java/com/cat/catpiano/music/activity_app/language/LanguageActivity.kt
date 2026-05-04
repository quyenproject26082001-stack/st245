package com.cat.catpiano.music.activity_app.language

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.lvt.ads.util.Admob
import androidx.lifecycle.lifecycleScope
import com.cat.catpiano.music.R
import com.cat.catpiano.music.core.base.BaseActivity
import com.cat.catpiano.music.core.extensions.handleBackLeftToRight
import com.cat.catpiano.music.core.extensions.select
import com.cat.catpiano.music.core.extensions.showToast
import com.cat.catpiano.music.core.extensions.startIntentRightToLeft
import com.cat.catpiano.music.core.extensions.startIntentWithClearTop
import com.cat.catpiano.music.core.extensions.visible
import com.cat.catpiano.music.core.utils.key.IntentKey
import com.cat.catpiano.music.databinding.ActivityLanguageBinding
import com.cat.catpiano.music.activity_app.main.MainActivity
import com.cat.catpiano.music.activity_app.intro.IntroActivity
import com.cat.catpiano.music.core.extensions.setOnSingleClick
import com.cat.catpiano.music.core.extensions.strings
import com.cat.catpiano.music.activity_app.language.LanguageViewModel
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    private val viewModel: LanguageViewModel by viewModels()

    private val languageAdapter by lazy { LanguageAdapter(this) }

    override fun setViewBinding(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        val intentValue = intent.getStringExtra(IntentKey.INTENT_KEY)
        val currentLang = sharePreference.getPreLanguage()
        viewModel.setFirstLanguage(intentValue == null)
        viewModel.loadLanguages(currentLang)
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            viewModel.isFirstLanguage.collect { isFirst ->
                if (isFirst) {
                    binding.actionBar.tvStart.visible()
                } else {
                    binding.actionBar.btnActionBarLeft.visible()
                    binding.actionBar.tvCenter.visible()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.languageList.collect { list ->
                languageAdapter.submitList(list)
            }
        }
        lifecycleScope.launch {
            viewModel.codeLang.collect { code ->
                if (code.isNotEmpty()) {
                    binding.actionBar.btnActionBarRight.visible()
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.setOnSingleClick { handleBackLeftToRight() }
            actionBar.btnActionBarRight.setOnSingleClick { handleDone() }
        }
        handleRcv()
    }

    override fun initText() {
        binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {

        if (viewModel.isFirstLanguage.value) {
            val p = (8 * resources.displayMetrics.density).toInt() // 6dp
            binding.actionBar.btnActionBarRight.setPadding(p, p, p, p)
            binding.actionBar.btnActionBarRight.setImageResource(R.drawable.ic_done_first_setting)
        } else {
            binding.actionBar.btnActionBarRight.setImageResource(R.drawable.ic_done)
        }

        binding.actionBar.apply {

            btnActionBarLeft.setImageResource(R.drawable.ic_back)
            val text = R.string.language
            tvCenter.text = strings(text)
            tvStart.text = strings(text)
            tvCenter.visible()
        }
    }

    private fun initRcv() {
        binding.rcv.apply {
            adapter = languageAdapter
            itemAnimator = null
        }
    }

    private fun handleRcv() {
        binding.apply {
            languageAdapter.onItemClick = { code ->
                binding.actionBar.btnActionBarRight.visible()
                viewModel.selectLanguage(code)
            }
        }
    }

    private fun handleDone() {
        val code = viewModel.codeLang.value
        if (code.isEmpty()) {
            showToast(R.string.not_select_lang)
            return
        }
        sharePreference.setPreLanguage(code)

        if (viewModel.isFirstLanguage.value) {
            sharePreference.setIsFirstLang(false)
            startIntentRightToLeft(IntroActivity::class.java)
        } else {
            sharePreference.setLanguageChanged(true)
            handleBackLeftToRight()
        }
    }

    override fun initAds() {
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_language), binding.nativeAds, R.layout.ads_native_big_btn_top)
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (!viewModel.isFirstLanguage.value) {
            handleBackLeftToRight()
        } else {
            exitProcess(0)
        }
    }


}