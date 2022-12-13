package com.gxd.template.ui.main

import android.os.Bundle
import android.view.View
import com.gxd.template.databinding.ActivityMainBinding
import com.gxd.template.base.viewmodel.ViewModelActivity

class MainActivity : ViewModelActivity<ActivityMainBinding, MainViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.bannerList.observe(this) { bannerList ->
            binding.mainTv.text = bannerList.toString()
        }
    }

    fun onBtnClick(view: View) = viewModel.requestBannerList()
}