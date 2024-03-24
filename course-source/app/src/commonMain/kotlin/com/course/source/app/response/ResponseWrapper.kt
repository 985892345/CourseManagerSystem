package com.course.source.app.response

import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/14 17:37
 */
@Serializable
sealed interface ResponseWrapper<T : Any?> {
  val code: Int
  val info: String

  companion object {
    fun <T : Any?> success(data: T, info: String = ""): SuccessResponseWrapper<T> {
      return SuccessResponseWrapper(data, 10000, info)
    }

    fun <T : Any?> failure(code: Int, info: String): FailureResponseWrapper<T> {
      return FailureResponseWrapper(code, info)
    }
  }
}

@Serializable
data class SuccessResponseWrapper<T : Any?>(
  val data: T,
  override val code: Int,
  override val info: String,
) : ResponseWrapper<T>

@Serializable
class FailureResponseWrapper<T : Any?>(
  override val code: Int,
  override val info: String,
) : ResponseWrapper<T>

