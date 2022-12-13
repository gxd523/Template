package com.gxd.template.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gxd.template.data.BannerData
import com.gxd.template.dal.network.RetrofitObj
import com.gxd.template.dal.network.WanAndroidApi
import com.gxd.template.respository.WanRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val wanRepository by lazy {
        WanRepository(RetrofitObj.retrofit.create(WanAndroidApi::class.java))
    }

    private val _bannerList by lazy { MutableLiveData<List<BannerData>>() }
    val bannerList: LiveData<List<BannerData>> = _bannerList

    fun requestBannerList() {
        viewModelScope.launch {
            val bannerList = wanRepository.requestBannerList()
            _bannerList.value = bannerList
        }
    }
}