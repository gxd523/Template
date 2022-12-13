package com.gxd.template.respository

import com.gxd.template.data.BannerData
import com.gxd.template.dal.network.WanAndroidApi

class WanRepository(private val apiService: WanAndroidApi) {
    suspend fun requestBannerList(): List<BannerData> {
        return apiService.requestBannerList("json")
    }
}