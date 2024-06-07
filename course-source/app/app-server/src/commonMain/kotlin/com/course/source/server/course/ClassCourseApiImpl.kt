package com.course.source.server.course

import com.course.source.app.course.ClassCourseApi
import com.course.source.app.course.ClassMember
import com.course.source.app.response.ResponseWrapper
import com.course.source.server.AppHttpClient
import io.github.seiko.ktorfit.annotation.generator.GenerateApi
import io.github.seiko.ktorfit.annotation.http.*

/**
 * .
 *
 * @author 985892345
 * 2024/6/7 10:10
 */
object ClassCourseApiImpl : ClassCourseApi by ClassCourseApiProxy.create(AppHttpClient)

@GenerateApi
interface ClassCourseApiProxy : ClassCourseApi {

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

  @POST("/course/change")
  @FormUrlEncoded
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
  ): ResponseWrapper<Unit>

  @POST("/course/create")
  @FormUrlEncoded
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
  ): ResponseWrapper<Int>
}