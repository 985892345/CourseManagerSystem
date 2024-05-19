package com.course.source.server.schedule

import com.course.source.app.response.ResponseWrapper
import com.course.source.app.schedule.LocalScheduleBody
import com.course.source.app.schedule.ScheduleApi
import com.course.source.app.schedule.ScheduleBean
import com.course.source.server.AppHttpClient
import com.g985892345.provider.api.annotation.ImplProvider
import io.github.seiko.ktorfit.annotation.generator.GenerateApi
import io.github.seiko.ktorfit.annotation.http.*

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

  @POST("/schedule/get")
  override suspend fun getSchedule(
    @Body
    body: LocalScheduleBody,
  ): ResponseWrapper<List<ScheduleBean>>

  @POST("/schedule/add")
  override suspend fun addSchedule(
    @Body
    bean: ScheduleBean,
  ): ResponseWrapper<Int>

  @POST("/schedule/update")
  override suspend fun updateSchedule(
    @Body
    bean: ScheduleBean,
  ): ResponseWrapper<Unit>

  @POST("/schedule/remove")
  @FormUrlEncoded
  override suspend fun removeSchedule(
    @Field("id")
    id: Int,
  ): ResponseWrapper<Unit>
}