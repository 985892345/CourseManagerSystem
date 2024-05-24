package com.course.source.server.schedule

import com.course.source.app.response.ResponseWrapper
import com.course.source.app.schedule.LocalScheduleBody
import com.course.source.app.schedule.ScheduleApi
import com.course.source.app.schedule.ScheduleBean
import com.course.source.server.AppHttpClient
import com.g985892345.provider.api.annotation.ImplProvider
import io.github.seiko.ktorfit.annotation.generator.GenerateApi
import io.github.seiko.ktorfit.annotation.http.Body
import io.github.seiko.ktorfit.annotation.http.Field
import io.github.seiko.ktorfit.annotation.http.FormUrlEncoded
import io.github.seiko.ktorfit.annotation.http.POST
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 20:02
 */
@ImplProvider
object ScheduleApiImpl : ScheduleApi by ScheduleApiProxy.create(AppHttpClient)

@GenerateApi
interface ScheduleApiProxy : ScheduleApi {

  override suspend fun getSchedule(
    body: LocalScheduleBody,
  ): ResponseWrapper<List<ScheduleBean>> {
    return getSchedule(Json.encodeToString(body))
  }

  @POST("/schedule/get")
  suspend fun getSchedule(
    @Body
    body: String,
  ): ResponseWrapper<List<ScheduleBean>>

  override suspend fun addSchedule(
    bean: ScheduleBean,
  ): ResponseWrapper<Int> {
    return addSchedule(Json.encodeToString(bean))
  }

  @POST("/schedule/add")
  suspend fun addSchedule(
    @Body
    bean: String,
  ): ResponseWrapper<Int>

  override suspend fun updateSchedule(
    bean: ScheduleBean,
  ): ResponseWrapper<Unit> {
    return updateSchedule(Json.encodeToString(bean))
  }

  @POST("/schedule/update")
  suspend fun updateSchedule(
    @Body
    bean: String,
  ): ResponseWrapper<Unit>

  @POST("/schedule/remove")
  @FormUrlEncoded
  override suspend fun removeSchedule(
    @Field("id")
    id: Int,
  ): ResponseWrapper<Unit>
}