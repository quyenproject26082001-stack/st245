package com.cat.catpiano.music.activity_app.intro

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.lvt.ads.util.Admob
import com.cat.catpiano.music.R
import com.cat.catpiano.music.core.base.BaseActivity
import com.cat.catpiano.music.core.utils.DataLocal
import com.cat.catpiano.music.databinding.ActivityIntroBinding
import com.cat.catpiano.music.activity_app.main.MainActivity
import com.cat.catpiano.music.core.extensions.setOnSingleClick
import kotlin.system.exitProcess

class IntroActivity : BaseActivity<ActivityIntroBinding>() {
    private val introAdapter by lazy { IntroAdapter(this) }

    override fun setViewBinding(): ActivityIntroBinding {
        return ActivityIntroBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initVpg()
    }

    override fun viewListener() {
        binding.btnNext.setOnSingleClick { handleNext() }

        binding.vpgTutorial.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val params = binding.btnNext.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams

                if (position == 1) {
                    binding.nativeAds.visibility = View.GONE
                } else {
                    binding.nativeAds.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun initActionBar() {}

    private fun initVpg() {
        binding.apply {
            binding.vpgTutorial.adapter = introAdapter
            binding.dotsIndicator.attachTo(binding.vpgTutorial)
            introAdapter.submitList(DataLocal.itemIntroList)
        }
    }

    private fun handleNext() {
        binding.apply {
            val nextItem = binding.vpgTutorial.currentItem + 1
            if (nextItem < DataLocal.itemIntroList.size) {
                vpgTutorial.setCurrentItem(nextItem, true)
            } else {
                startActivity(Intent(this@IntroActivity, MainActivity::class.java))
                finishAffinity()
            }
        }
    }

    override fun initAds() {
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_intro), binding.nativeAds, R.layout.ads_native_medium_btn_bottom_2)
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        finishAffinity()
        exitProcess(0)
    }
}