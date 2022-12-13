package com.gxd.template.dal.network.transform

import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Converter
import java.io.IOException

class ResponseConverter constructor(
    private val transformer: IResponseTransformer,
    private val candidateConverter: Converter<ResponseBody, *>
) : Converter<ResponseBody, Any?> {

    @Throws(IOException::class)
    override fun convert(value: ResponseBody): Any? = transformer.transform(value.byteStream())
        .readBytes()
        .toResponseBody(value.contentType())
        .let(candidateConverter::convert)
}