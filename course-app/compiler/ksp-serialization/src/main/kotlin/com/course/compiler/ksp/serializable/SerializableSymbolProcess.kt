package com.course.compiler.ksp.serializable

import com.g985892345.provider.api.annotation.ImplProvider
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.typeNameOf
import kotlinx.serialization.KSerializer
import java.io.File
import kotlin.reflect.KClass

/**
 * .
 *
 * @author 985892345
 * 2024/3/5 21:48
 */
class SerializableSymbolProcess(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
  private val options: Options,
) : SymbolProcessor {

  companion object {
    private const val OBJECT_SERIALIZABLE_PACKAGE_NAME =
      "com.course.components.utils.serializable"
    private const val OBJECT_SERIALIZABLE_CLASS_NAME = "ObjectSerializable"
    private const val OBJECT_SERIALIZABLE_COLLECTOR_CLASS_NAME = "ObjectSerializableCollector"
    private const val OBJECT_SERIALIZABLE =
      "$OBJECT_SERIALIZABLE_PACKAGE_NAME.$OBJECT_SERIALIZABLE_CLASS_NAME"
  }

  private var processNowTimes = 0

  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver.getSymbolsWithAnnotation(OBJECT_SERIALIZABLE)
      .filterIsInstance<KSClassDeclaration>()
      .onEach { checkSerializableClass(it) }
      .toList()
      .generateSerializableCollector()
    processNowTimes++
    return emptyList()
  }

  private fun List<KSClassDeclaration>.generateSerializableCollector() {
    if (isEmpty()) return
    val collectorClass = ClassName(
      OBJECT_SERIALIZABLE_PACKAGE_NAME,
      OBJECT_SERIALIZABLE_COLLECTOR_CLASS_NAME
    )
    val className = "_${options.className}_$processNowTimes"
    FileSpec.builder(options.packageName, className)
      .addType(
        TypeSpec.objectBuilder(className)
          .addAnnotation(
            AnnotationSpec.builder(ImplProvider::class)
              .addMember("%T::class", collectorClass)
              .addMember("%S", "${options.packageName}.${options.className}_$processNowTimes")
              .build()
          )
          .addSuperinterface(collectorClass)
          .addProperty(
            PropertySpec.builder(
              "collected",
              typeNameOf<List<Triple<String, KClass<*>, KSerializer<*>>>>()
            ).addModifiers(KModifier.OVERRIDE)
              .initializer(
                "listOf(${joinToString { "Triple(%S, %T::class, %T.serializer())" }})",
                *map {
                  listOf(it.qualifiedName!!.asString(), it.toClassName(), it.toClassName())
                }.flatten().toTypedArray()
              )
              .build()
          )
          .build()
      ).build().apply {
        writeTo(codeGenerator, true, mapNotNull { it.containingFile })
      }
  }

  private fun checkSerializableClass(declaration: KSClassDeclaration) {
    check(declaration.classKind == ClassKind.CLASS || declaration.classKind == ClassKind.OBJECT) {
      "@ObjectSerializable 只支持 class 和 object\nclass: ${declaration.locationStr}"
    }
    check(declaration.modifiers.all { it != Modifier.ABSTRACT && it != Modifier.SEALED && it != Modifier.VALUE }) {
      "@ObjectSerializable 不支持抽象类、密封类、内联类\nclass: ${declaration.locationStr}"
    }
    check(declaration.typeParameters.isEmpty()) {
      "@ObjectSerializable 不支持带有泛型的类\nclass: ${declaration.locationStr}"
    }
    check(declaration.annotations.any { it.shortName.asString() == "Serializable" }) {
      "@ObjectSerializable 需要 @Serializable 以提供支持\nclass: ${declaration.locationStr}"
    }
  }

  private val KSNode.locationStr: String
    get() = (location as FileLocation).run {
      "(${filePath.substringAfterLast(File.pathSeparator)}:${lineNumber})"
    }
}