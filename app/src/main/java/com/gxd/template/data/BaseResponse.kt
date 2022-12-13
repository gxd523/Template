package com.gxd.template.data

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    val data: T,
    @SerializedName(value = "errorCode", alternate = ["code"])
    val errorCode: Int? = null,
    @SerializedName(value = "errorMsg", alternate = ["msg"])
    val errorMsg: String? = null
)