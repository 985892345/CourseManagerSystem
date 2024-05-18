package com.course.source.server.notification

import com.course.source.app.notification.Notification
import com.course.source.app.notification.NotificationApi
import com.course.source.app.response.ResponseWrapper
import com.course.source.server.AppHttpClient
import com.g985892345.provider.api.annotation.ImplProvider
import io.github.seiko.ktorfit.annotation.generator.GenerateApi
import io.github.seiko.ktorfit.annotation.http.Field
import io.github.seiko.ktorfit.annotation.http.GET
import io.github.seiko.ktorfit.annotation.http.POST
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 20:00
 */
@ImplProvider
object NotificationApiImpl : NotificationApi by NotificationApiProxy.create(AppHttpClient)

@GenerateApi
interface NotificationApiProxy : NotificationApi {

  @GET("/notification/get")
  override suspend fun getNotifications(): ResponseWrapper<List<Notification>>

  @POST("/notification/hasNew")
  override suspend fun hasNewNotification(): ResponseWrapper<Boolean>

//  @POST("/notification/decision")
//  @FormUrlEncoded
  override suspend fun decision(
    @Field("notificationId")
    notificationId: Int,
    @Field("isAgree")
    isAgree: Boolean,
  ): ResponseWrapper<Unit> {
    val result = AppHttpClient.request {
      method = HttpMethod.parse("POST")
      url("/notification/decision")
      val parameters = Parameters.build {
        append("notificationId", notificationId.toString())
        append("isAgree", isAgree.toString())
      }
      setBody(FormDataContent(parameters))
    }
    return result.body()
  }
}