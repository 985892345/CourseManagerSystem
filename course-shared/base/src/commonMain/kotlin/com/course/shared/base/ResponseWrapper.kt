package com.course.shared.base

import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/14 17:37
 */
@Serializable
data class ResponseInfo(
  override val code: Int,
  override val info: String,
) : IResponse

@Serializable
data class ResponseWrapper<T : Any?>(
  val data: T,
  override val code: Int,
  override val info: String,
) : IResponse

interface IResponse {
  val code: Int
  val info: String

  fun isSuccess(): Boolean {
    return code == 10000
  }
}