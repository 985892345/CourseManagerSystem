package com.course.source.app.response

import kotlinx.serialization.Serializable

/**
 * 规定：
 * - data 不为 null 时视为成功
 * - 对于无返回值的接口，应使用 ResponseWrapper<Unit>
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

    fun successWithoutData(info: String = ""): ResponseWrapper<Unit> {
      return success(Unit, info)
    }

    fun <T : Any> failure(code: Int, info: String): ResponseWrapper<T> {
      return ResponseWrapper(null, code, info)
    }
  }
}
