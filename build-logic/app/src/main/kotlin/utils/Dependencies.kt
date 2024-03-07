package utils

import org.gradle.kotlin.dsl.DependencyHandlerScope

/**
 * .
 *
 * @author 985892345
 * 2024/3/6 18:52
 */

fun DependencyHandlerScope.kspMultiplatform(
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
//  add("kspWasmJs", dependencyNotation)
}