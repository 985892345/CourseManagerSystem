package com.course.server.utils

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 17:27
 */
class ResponseException(
  val info: String,
  val code: Int = 10001,
) : RuntimeException(info)