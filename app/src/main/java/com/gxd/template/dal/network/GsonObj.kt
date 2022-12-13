package com.gxd.template.dal.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonObj {
    val gson: Gson by lazy { GsonBuilder().disableHtmlEscaping().create() }
}