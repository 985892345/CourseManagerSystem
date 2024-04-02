package com.course.source.app.response

import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/14 17:37
 */
@Serializable
data class ResponseWrapper<T : Any>(
  val data: T?,
  val code: Int,
  val info: String,
) {
  companion object {
    fun <T : Any> success(data: T, info: String = ""): ResponseWrapper<T> {
      return ResponseWrapper(data, 10000, info)
    }

    fun <T : Any> failure(code: Int, info: String): ResponseWrapper<T> {
      return ResponseWrapper(null, code, info)
    }
  }
}
