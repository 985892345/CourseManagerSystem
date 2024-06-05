import org.gradle.api.Project

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/12 14:47
 */
object Config {
  fun getBaseName(project: Project): String {
    var baseName = ""
    var p: Project? = project
    while (p != null && p != project.rootProject) {
      baseName = p.name.substringAfterLast("-").replaceFirstChar { it.uppercaseChar() } + baseName
      p = p.parent
    }
    return baseName
  }

  fun getNamespace(project: Project): String {
    var namespace = ""
    var p: Project? = project
    while (p != null && p != project.rootProject) {
      namespace = ".${p.name.substringAfterLast("-")}$namespace"
      p = p.parent
    }
    return "com.course$namespace"
  }
}