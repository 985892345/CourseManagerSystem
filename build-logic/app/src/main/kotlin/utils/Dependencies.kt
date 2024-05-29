package utils

import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope

/**
 * .
 *
 * @author 985892345
 * 2024/3/6 18:52
 */

fun DependencyHandlerScope.kspMultiplatform(
  project: Project,
  dependencyNotation: Any,
  isNeedKspCommonMainMetadata: Boolean = true,
) {
  if (isNeedKspCommonMainMetadata) {
    add("kspCommonMainMetadata", dependencyNotation)
  }
  add("kspAndroid", dependencyNotation)
  add("kspDesktop", dependencyNotation)

  add("kspIosX64", dependencyNotation)
  add("kspIosArm64", dependencyNotation)
  add("kspIosSimulatorArm64", dependencyNotation)

  // wasm 等待第三方依赖完善
//  add("kspWasmJs", dependencyNotation)
}