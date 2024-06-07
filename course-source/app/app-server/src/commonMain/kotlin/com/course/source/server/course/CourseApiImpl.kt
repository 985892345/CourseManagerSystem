package com.course.source.server.course

import com.course.source.app.course.CourseApi
import com.course.source.app.course.CourseBean
import com.course.source.app.response.ResponseWrapper
import com.course.source.server.AppHttpClient
import com.g985892345.provider.api.annotation.ImplProvider
import io.github.seiko.ktorfit.annotation.generator.GenerateApi
import io.github.seiko.ktorfit.annotation.http.GET
import io.github.seiko.ktorfit.annotation.http.Query

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
}