package com.gxd.template.dal.network

import com.gxd.template.dal.network.transform.ResponseConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitObj {
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.wanandroid.com/")
            .addConverterFactory(ResponseConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient().newBuilder()
            .protocols(listOf(Protocol.HTTP_1_1, Protocol.HTTP_2))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}