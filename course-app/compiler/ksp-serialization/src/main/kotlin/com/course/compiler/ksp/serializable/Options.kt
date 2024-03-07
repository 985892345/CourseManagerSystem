package com.course.compiler.ksp.serializable

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
    options["SerializablePackageName"]!!, // It assigned by the Gradle plugin
    options["SerializableClassName"]!!, // It assigned by the Gradle plugin
  )
}