package com.course.source.app.response

import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/14 17:37
 */
@Serializable
data class ResponseWrapper<T : Any?>(
  val data: T,
  val code: Int,
  val info: String,
) {
  fun isSuccess(): Boolean {
    return code == 10000
  }
}
