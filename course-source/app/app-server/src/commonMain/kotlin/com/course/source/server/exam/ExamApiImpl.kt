package com.course.source.server.exam

import com.course.source.app.exam.ExamApi
import com.course.source.app.exam.ExamTermBean
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
 * 2024/5/16 19:59
 */
@ImplProvider
object ExamApiImpl : ExamApi by ExamApiProxy.create(AppHttpClient)

@GenerateApi
interface ExamApiProxy : ExamApi {

  @GET("/exam/get")
  override suspend fun getExam(
    @Query("stuNum")
    stuNum: String,
  ): ResponseWrapper<ExamTermBean>
}