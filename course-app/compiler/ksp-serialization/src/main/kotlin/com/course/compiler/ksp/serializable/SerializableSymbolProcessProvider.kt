package com.course.compiler.ksp.serializable

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * .
 *
 * @author 985892345
 * 2024/3/5 21:48
 */
class SerializableSymbolProcessProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
    return SerializableSymbolProcess(
      environment.codeGenerator,
      environment.logger,
      Options(environment.options),
    )
  }
}