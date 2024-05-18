package com.course.source.server.course

import com.course.source.app.course.ClassMember
import com.course.source.app.course.CourseApi
import com.course.source.app.course.CourseBean
import com.course.source.app.response.ResponseWrapper
import com.course.source.server.AppHttpClient
import com.g985892345.provider.api.annotation.ImplProvider
import io.github.seiko.ktorfit.annotation.generator.GenerateApi
import io.github.seiko.ktorfit.annotation.http.Field
import io.github.seiko.ktorfit.annotation.http.GET
import io.github.seiko.ktorfit.annotation.http.POST
import io.github.seiko.ktorfit.annotation.http.Query
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 19:56
 */
@ImplProvider
object CourseApiImpl : CourseApi by CourseApiProxy.create(AppHttpClient)

@GenerateApi
interface CourseApiProxy : CourseApi {

  @GET("/course/get")
  override suspend fun getCourseBean(
    @Query("num")
    num: String
  ): ResponseWrapper<CourseBean>

  @GET("/course/members")
  override suspend fun getClassMembers(
    @Query("classNum")
    classNum: String,
  ): ResponseWrapper<List<ClassMember>>

  @POST("/course/delete")
  override suspend fun deleteCourse(
    @Query("classPlanId")
    classPlanId: Int,
  ): ResponseWrapper<Unit>

//  @POST("/course/change")
//  @FormUrlEncoded
  override suspend fun changeCourse(
    @Field("classPlanId")
    classPlanId: Int,
    @Field("newDate")
    newDate: String,
    @Field("newBeginLesson")
    newBeginLesson: Int,
    @Field("newLength")
    newLength: Int,
    @Field("newClassroom")
    newClassroom: String,
  ): ResponseWrapper<Unit> {
    val result = AppHttpClient.request {
      method = HttpMethod.parse("POST")
      url("/course/change")
      val parameters = Parameters.build {
        append("classPlanId", newDate)
        append("newDate", newDate)
        append("newBeginLesson", newBeginLesson.toString())
        append("newLength", newLength.toString())
        append("newClassroom", newClassroom)
      }
      setBody(FormDataContent(parameters))
    }
    return result.body()
  }

//  @POST("/course/create")
//  @FormUrlEncoded
  override suspend fun createCourse(
    @Field("classNum")
    classNum: String,
    @Field("date")
    date: String,
    @Field("beginLesson")
    beginLesson: Int,
    @Field("length")
    length: Int,
    @Field("classroom")
    classroom: String,
  ): ResponseWrapper<Int> {
    val result = AppHttpClient.request {
      method = HttpMethod.parse("POST")
      url("/course/create")
      val parameters = Parameters.build {
        append("classNum", classNum)
        append("date", date)
        append("beginLesson", beginLesson.toString())
        append("length", length.toString())
        append("classroom", classroom)
      }
      setBody(FormDataContent(parameters))
    }
    return result.body()
  }
}