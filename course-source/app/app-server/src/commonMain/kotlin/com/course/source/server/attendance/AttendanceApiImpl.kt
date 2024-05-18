package com.course.source.server.attendance

import com.course.source.app.attendance.*
import com.course.source.app.response.ResponseWrapper
import com.course.source.server.AppHttpClient
import com.g985892345.provider.api.annotation.ImplProvider
import io.github.seiko.ktorfit.annotation.generator.GenerateApi
import io.github.seiko.ktorfit.annotation.http.Field
import io.github.seiko.ktorfit.annotation.http.GET
import io.github.seiko.ktorfit.annotation.http.Query
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 19:52
 */
@ImplProvider
object AttendanceApiImpl : AttendanceApi by AttendanceApiProxy.create(AppHttpClient)

@GenerateApi
interface AttendanceApiProxy : AttendanceApi {

//  @POST("/attendance/publish")
//  @FormUrlEncoded
  override suspend fun publishAttendance(
    @Field("classPlanId")
    classPlanId: Int,
    @Field("code")
    code: String,
    @Field("duration")
    duration: Int,
    @Field("lateDuration")
    lateDuration: Int,
  ): ResponseWrapper<Unit> {
    val result = AppHttpClient.request {
      method = HttpMethod.parse("POST")
      url("/attendance/publish")
      val parameters = Parameters.build {
        append("classPlanId", classPlanId.toString())
        append("code", code)
        append("duration", duration.toString())
        append("lateDuration", lateDuration.toString())
      }
      setBody(FormDataContent(parameters))
    }
    return result.body()
  }

  @GET("/attendance/students")
  override suspend fun getAttendanceStudent(
    @Query("classPlanId")
    classPlanId: Int
  ): ResponseWrapper<List<AttendanceStudentList>>

//  @POST("/attendance/change")
//  @FormUrlEncoded
  override suspend fun changeAttendanceStatus(
    @Field("periodId")
    classPlanId: Int,
    @Field("code")
    code: String,
    @Field("stuNum")
    stuNum: String,
    @Field("status")
    status: AttendanceStatus,
  ): ResponseWrapper<Unit> {
    val result = AppHttpClient.request {
      method = HttpMethod.parse("POST")
      url("/attendance/change")
      val parameters = Parameters.build {
        append("classPlanId", classPlanId.toString())
        append("code", code)
        append("stuNum", stuNum)
        append("status", status.name)
      }
      setBody(FormDataContent(parameters))
    }
    return result.body()
  }

//  @POST("/attendance/code")
//  @FormUrlEncoded
  override suspend fun postAttendanceCode(
    @Field("periodId")
    classPlanId: Int,
    @Field("code")
    code: String,
  ): ResponseWrapper<AttendanceCodeStatus> {
    val result = AppHttpClient.request {
      method = HttpMethod.parse("POST")
      url("/attendance/code")
      val parameters = Parameters.build {
        append("classPlanId", classPlanId.toString())
        append("code", code)
      }
      setBody(FormDataContent(parameters))
    }
    return result.body()
  }

  @GET("/attendance/history")
  override suspend fun getAttendanceHistory(
    @Query("classNum")
    classNum: String,
    @Query("stuNum")
    stuNum: String,
  ): ResponseWrapper<List<AttendanceHistory>>

  @GET("/attendance/askForLeave")
  override suspend fun getAskForLeaveHistory(
    @Query("classNum")
    classNum: String,
    @Query("stuNum")
    stuNum: String,
  ): ResponseWrapper<List<AskForLeaveHistory>>

//  @POST("/attendance/askForLeave")
//  @FormUrlEncoded
  override suspend fun postAskForLeave(
    @Field("classPlanId")
    classPlanId: Int,
    @Field("reason")
    reason: String,
  ): ResponseWrapper<Unit> {
    val result = AppHttpClient.request {
      method = HttpMethod.parse("POST")
      url("/attendance/askForLeave")
      val parameters = Parameters.build {
        append("classPlanId", classPlanId.toString())
        append("reason", reason)
      }
      setBody(FormDataContent(parameters))
    }
    return result.body()
  }
}