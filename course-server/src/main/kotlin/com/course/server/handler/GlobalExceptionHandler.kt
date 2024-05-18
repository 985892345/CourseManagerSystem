package com.course.server.handler

import com.course.server.utils.ResponseException
import com.course.source.app.response.ResponseWrapper
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 21:28
 */
@RestControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(value = [Exception::class])
  fun handle(e: Exception): ResponseWrapper<Unit> {
    if (e is ResponseException) {
      return ResponseWrapper.failure(e.code, e.info)
    } else {
      e.printStackTrace()
      return ResponseWrapper.failure(50000, e.message ?: "未知异常")
    }
  }
}