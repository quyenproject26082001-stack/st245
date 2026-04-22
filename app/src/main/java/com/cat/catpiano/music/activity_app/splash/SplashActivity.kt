package com.cat.catpiano.music.activity_app.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import com.cat.catpiano.music.core.base.BaseActivity
import com.cat.catpiano.music.core.extensions.initNetworkMonitor
import com.cat.catpiano.music.databinding.ActivitySplashBinding
import com.cat.catpiano.music.activity_app.intro.IntroActivity
import com.cat.catpiano.music.activity_app.language.LanguageActivity

class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    private val splashDuration = 3000L

    override fun setViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        if (!isTaskRoot &&
            intent.hasCategory(Intent.CATEGORY_LAUNCHER) &&
            intent.action != null &&
            intent.action.equals(Intent.ACTION_MAIN)) {
            finish(); return
        }

        val nextActivity = if (sharePreference.getIsFirstLang()) {
            Intent(this, LanguageActivity::class.java)
        } else {
            Intent(this, IntroActivity::class.java)
        }
        initNetworkMonitor()

        binding.root.postDelayed({
            if (!isFinishing) {
                startActivity(nextActivity)
                finishAffinity()
            }
        }, splashDuration)
    }

    override fun viewListener() {}
    override fun initActionBar() {}

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {}
}
