package id.my.anandalukman.otpchatappfirebase.api

import id.my.anandalukman.otpchatappfirebase.asset.NotificationBean
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant.CONTENT_TYPE
import id.my.anandalukman.otpchatappfirebase.constant.ChatConstant.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {

    @Headers("Authorization: key=$SERVER_KEY","Content-type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification:NotificationBean
    ): Response<ResponseBody>
}