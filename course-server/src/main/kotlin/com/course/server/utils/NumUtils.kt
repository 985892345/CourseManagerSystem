package com.course.server.utils

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 19:30
 */
object NumUtils {

  fun isStudent(num: String): Boolean {
    return num.length == 10
  }

  fun isTeacher(num: String): Boolean {
    return num.length == 6
  }
}