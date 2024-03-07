package com.course.compiler.ksp.navigator

/**
 * .
 *
 * @author 985892345
 * 2024/3/6 12:59
 */
class Options(
  val packageName: String,
  val className: String,
) {
  constructor(options: Map<String, String>) : this(
    options["NavigatorPackageName"]!!, // It assigned by the Gradle plugin
    options["NavigatorClassName"]!!, // It assigned by the Gradle plugin
  )
}