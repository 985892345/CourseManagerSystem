package com.course.compiler.ksp.navigator

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * .
 *
 * @author 985892345
 * 2024/3/5 20:25
 */
class NavigatorSymbolProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
    return NavigatorSymbolProcessor(
      environment.codeGenerator,
      environment.logger,
      Options(environment.options)
    )
  }
}