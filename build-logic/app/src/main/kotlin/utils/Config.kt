package utils

import org.gradle.api.Project

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/12 14:47
 */
object Config {
  fun getBaseName(project: Project): String {
    val top = "course-app"
    var baseName = ""
    var p: Project? = project
    while (p != null && p.name != top) {
      baseName = p.name.replaceFirstChar { it.uppercaseChar() } + baseName
      p = p.parent
    }
    return baseName
  }

  fun getNamespace(project: Project): String {
    val top = "course-app"
    var namespace = ""
    var p: Project? = project
    while (p != null && p.name != top) {
      namespace = "${p.name}.$namespace"
      p = p.parent
    }
    return "com.course.$namespace"
  }
}