package com.course.shared.base

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/14 17:37
 */
interface IResponseWrapper<T : Any?> {
  val data: T
  val info: String
  val status: Int

  fun isSuccess(): Boolean {
    return status == 10000
  }
}
