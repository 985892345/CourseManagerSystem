package com.course.components.utils.debug

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 10:22
 */

actual fun log(msg: String) {
  println(".(${Exception().stackTrace[1].run { "$fileName:$lineNumber" }}) -> $msg")
}