package com.cat.catpiano.music.activity_app.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import com.lvt.ads.callback.InterCallback
import com.lvt.ads.util.Admob
import com.cat.catpiano.music.R
import com.cat.catpiano.music.core.base.BaseActivity
import com.cat.catpiano.music.core.extensions.hideNavigation
import com.cat.catpiano.music.core.extensions.initNetworkMonitor
import com.cat.catpiano.music.databinding.ActivitySplashBinding
import com.cat.catpiano.music.activity_app.intro.IntroActivity
import com.cat.catpiano.music.activity_app.language.LanguageActivity

class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    private var intentActivity: Intent? = null
    private var interCallBack: InterCallback? = null
    private var isAdsLoaded = false

    override fun setViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        Admob.getInstance().setOpenShowAllAds(true)
        if (!isTaskRoot &&
            intent.hasCategory(Intent.CATEGORY_LAUNCHER) &&
            intent.action != null &&
            intent.action.equals(Intent.ACTION_MAIN)) {
            finish(); return
        }

        intentActivity = if (sharePreference.getIsFirstLang()) {
            Intent(this, LanguageActivity::class.java)
        } else {
            Intent(this, IntroActivity::class.java)
        }

        initNetworkMonitor()
        Admob.getInstance().setTimeLimitShowAds(3000)
        Admob.getInstance().setTimeCountdownNativeCollab(20000)

        interCallBack = object : InterCallback() {
            override fun onNextAction() {
                super.onNextAction()
                startActivity(intentActivity)
                finishAffinity()
            }
        }
    }

    override fun viewListener() {}
    override fun initActionBar() {}

    override fun onResume() {
        super.onResume()
        hideNavigation(isBlack = false)
        if (!isAdsLoaded) {
            isAdsLoaded = true
            Admob.getInstance().loadSplashInterAds(
                this,
                getString(R.string.inter_splash),
                30000,
                2000,
                interCallBack
            )
        } else {
            Admob.getInstance().onCheckShowSplashWhenFail(this, interCallBack, 1000)
        }
    }

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {}
}
