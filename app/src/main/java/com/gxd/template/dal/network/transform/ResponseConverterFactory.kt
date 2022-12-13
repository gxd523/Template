package com.gxd.template.dal.network.transform

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class ResponseConverterFactory : Converter.Factory() {
    private val transformer by lazy { ResponseTransformer() }

    override fun responseBodyConverter(
        type: Type, annotations: Array<out Annotation>, retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        annotations.find { it is DisableResponseTransformer }?.let { return null }
        val candidateConverter = retrofit.nextResponseBodyConverter<Any>(this, type, annotations)
        return ResponseConverter(transformer, candidateConverter)
    }
}