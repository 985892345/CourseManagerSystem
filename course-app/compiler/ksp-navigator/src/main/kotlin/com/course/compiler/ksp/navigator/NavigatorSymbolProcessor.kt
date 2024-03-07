package com.course.compiler.ksp.navigator

import com.g985892345.provider.api.annotation.ImplProvider
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.NonExistLocation
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import java.io.File
import kotlin.reflect.KClass

/**
 * .
 *
 * @author 985892345
 * 2024/3/5 20:26
 */
class NavigatorSymbolProcessor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
  private val options: Options,
) : SymbolProcessor {

  companion object {
    private const val SCREEN_ENTER_PACKAGE_NAME =
      "com.course.components.utils.navigator"
    private const val SCREEN_ENTER_CLASS_NAME = "RemoteScreenEnter"
    private const val SCREEN_ENTER_COLLECTOR_CLASS_NAME = "RemoteScreenEnterCollector"
    private const val REMOTE_SCREEN_CLASS_NAME = "RemoteScreen"
    private const val SCREEN_ENTER =
      "$SCREEN_ENTER_PACKAGE_NAME.$SCREEN_ENTER_CLASS_NAME"
  }

  private var processNowTimes = 0

  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver.getSymbolsWithAnnotation(SCREEN_ENTER)
      .filterIsInstance<KSFunctionDeclaration>()
      .map { getParameterDeclaration(it) to it }
      .toList()
      .generateScreen()
    processNowTimes++
    return emptyList()
  }

  private fun List<Pair<KSClassDeclaration, KSFunctionDeclaration>>.generateScreen() {
    if (isEmpty()) return
    val collectorClass = ClassName(
      SCREEN_ENTER_PACKAGE_NAME,
      SCREEN_ENTER_COLLECTOR_CLASS_NAME
    )
    val screenDataClass = ClassName(SCREEN_ENTER_PACKAGE_NAME, REMOTE_SCREEN_CLASS_NAME)
    val collectedType = MAP.parameterizedBy(
      KClass::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(screenDataClass)),
      LambdaTypeName.get(
        parameters = arrayOf(screenDataClass),
        returnType = UNIT
      ).copy(
        annotations = listOf(
          AnnotationSpec.builder(ClassName("androidx.compose.runtime", "Composable")).build()
        )
      )
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
              collectedType
            ).addModifiers(KModifier.OVERRIDE)
              .initializer(
                "mapOf(${joinToString { "%T::class to { %M(it as %T) }, " }})",
                *map {
                  listOf(
                    it.first.toClassName(),
                    MemberName(
                      it.second.packageName.asString(),
                      it.second.simpleName.asString()
                    ),
                    it.first.toClassName()
                  )
                }.flatten().toTypedArray()
              )
              .build()
          )
          .build()
      ).build().apply {
        writeTo(
          codeGenerator,
          true,
          map { listOf(it.first.containingFile, it.second.containingFile) }.flatten().mapNotNull { it }
        )
      }
    MemberName
  }

  private val collectedClassWithLocation = hashMapOf<String, String?>()

  private fun getParameterDeclaration(
    declaration: KSFunctionDeclaration
  ): KSClassDeclaration {
    check(declaration.functionKind == FunctionKind.TOP_LEVEL) {
      "@ScreenEnter 只支持顶级函数\n${declaration.locationStr}"
    }
    check(declaration.annotations.any { it.shortName.asString() == "Composable" }) {
      "@ScreenEnter 只支持 Composable 函数\n${declaration.locationStr}"
    }
    check(declaration.parameters.size == 1) {
      "@ScreenEnter 只支持单个参数\n${declaration.locationStr}"
    }
    val classDeclaration =
      declaration.parameters.first().type.resolve().declaration as KSClassDeclaration
    val key = classDeclaration.packageName.asString() + classDeclaration.simpleName.asString()
    val old = collectedClassWithLocation.remove(key)
    check(old == null) {
      "出现重复对应 ${classDeclaration.simpleName.asString()} 的函数\n$old\n${declaration.locationStr}"
    }
    collectedClassWithLocation[key] = declaration.locationStr
    val screenDataName = "$SCREEN_ENTER_PACKAGE_NAME.$REMOTE_SCREEN_CLASS_NAME"
    check(classDeclaration.getAllSuperTypes().any { it.declaration.qualifiedName?.asString() == screenDataName }) {
      "参数 ${classDeclaration.simpleName.asString()} 未实现 $REMOTE_SCREEN_CLASS_NAME" +
          "\nfunction: ${declaration.locationStr}\nclass: ${classDeclaration.locationStr}"
    }
    check(classDeclaration.classKind == ClassKind.CLASS || classDeclaration.classKind == ClassKind.OBJECT) {
      "参数 ${classDeclaration.simpleName.asString()} 必须是 class 或 object" +
          "\nfunction: ${declaration.locationStr}\nclass: ${classDeclaration.locationStr}"
    }
    check(classDeclaration.modifiers.all { it != Modifier.ABSTRACT && it != Modifier.SEALED && it != Modifier.VALUE }) {
      "class ${classDeclaration.simpleName.asString()} 不能为抽象类、密封类、内联类" +
          "\nfunction: ${declaration.locationStr}\nclass: ${classDeclaration.locationStr}"
    }
    // 其他模块编译时不包含 ObjectSerializable，交给运行时检查
//    logger.warn("annotations = ${classDeclaration.annotations.map { it.shortName.asString() }.toList()}")
//    check(classDeclaration.annotations.any { it.shortName.asString() == "ObjectSerializable" }) {
//      "class ${classDeclaration.simpleName.asString()} 必须包含 @ObjectSerializable 以实现序列化" +
//          "\nfunction: ${declaration.locationStr}\nclass: ${classDeclaration.locationStr}"
//    }
    return classDeclaration
  }

  private val KSNode.locationStr: String
    get() = when (val it = location) {
      NonExistLocation -> "不在当前模块，无法定位位置"
      is FileLocation -> "(${it.filePath.substringAfterLast(File.pathSeparator)}:${it.lineNumber})"
    }
}
