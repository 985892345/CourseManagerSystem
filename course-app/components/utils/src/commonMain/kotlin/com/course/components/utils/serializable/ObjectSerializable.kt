package com.course.components.utils.serializable

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import com.course.components.utils.provider.Provider
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

/**
 * .
 *
 * @author 985892345
 * 2024/3/5 21:50
 */

/**
 * 用于编译器收集 KSerializer<*>，并建立映射关系，[ObjectSerializable] 使用该映射关系来实现反序列化类的查找
 *
 * 需要在 build.gradle.kts 中引入 id("app.function.serialization") 插件
 *
 * 不支持带有泛型的类
 */
@Suppress("UNCHECKED_CAST")
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ObjectSerializable {
  companion object {

    fun isSerializable(clazz: KClass<*>): Boolean {
      return ObjectSerializableCollector.ByClazz[clazz] != null
    }

    fun serialize(data: Any?): String {
      val clazz = if (data != null) data::class else Null::class
      val serializer = ObjectSerializableCollector.ByClazz[clazz]
      check(serializer != null) { "未找到 $clazz 的 serializer, 请检查是否添加了 @SerializableCollector" }
      val key = serializer.first
      if (data == null) {
        return Json.encodeToString(key to "")
      }
      val obj = Json.encodeToString(serializer.third as KSerializer<Any>, data)
      return Json.encodeToString(key to obj)
    }

    fun deserialize(str: String): Any? {
      val pair = try {
        Json.decodeFromString<Pair<String, String>>(str)
      } catch (e: Exception) {
        throw RuntimeException("反序列化失败, str = $str", e)
      }
      val serializer = ObjectSerializableCollector.ByKey[pair.first]
      check(serializer != null) { "未找到 key = ${pair.first} 对应的 serializer" }
      try {
        if (serializer.second == Null::class) return null
        return Json.decodeFromString(serializer.third as KSerializer<Any>, pair.second)
      } catch (e: Exception) {
        throw RuntimeException(
          "反序列化失败, str = $str, key = ${serializer.second}, serializer = ${serializer.third}",
          e
        )
      }
    }
  }
}

// 由 ksp 实现
interface ObjectSerializableCollector {

  val collected: List<Triple<String, KClass<*>, KSerializer<*>>>

  companion object {
    private val AllImpl by lazy {
      Provider.getAllImpl(ObjectSerializableCollector::class)
        .map { it.value.get().collected }
        .flatten() +
          listOf(
            Triple("Null", Null::class, Null.serializer()),
            Triple("TextUnit", TextUnit::class, TextUnitSerializable()),
            Triple("Color", Color::class, ColorSerializable()),
          )
    }
    internal val ByKey by lazy {
      buildMap {
        AllImpl.forEach { put(it.first, it) }
      }
    }
    internal val ByClazz by lazy {
      buildMap {
        AllImpl.forEach { put(it.second, it) }
      }
    }
  }
}

@Serializable
private object Null
