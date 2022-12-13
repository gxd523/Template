package com.gxd.template.dal.network.transform

import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.gxd.template.data.BaseResponse
import com.gxd.template.dal.network.GsonObj
import okio.IOException
import java.io.InputStream

class ResponseTransformer : IResponseTransformer {
    @Throws(IOException::class)
    override fun transform(original: InputStream): InputStream {
        val response = GsonObj.gson.fromJson<BaseResponse<JsonElement>>(
            original.reader(), object : TypeToken<BaseResponse<JsonElement>>() {}.type
        )
        if (response.errorCode != 0 && response.errorCode != 200) throw IOException("errCode = ${response.errorCode}, errMsg = ${response.errorMsg}")

        return response.data.toString().byteInputStream()
    }
}