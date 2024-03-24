package com.course.source.app.web.request

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.properties.ReadOnlyProperty

/**
 * .
 *
 * @author 985892345
 * 2024/3/21 13:59
 */
abstract class SourceRequest {

  val requestContentMap: MutableMap<String, RequestContent<*>> = mutableMapOf()

  protected inline fun <reified T : Any> requestContent(
    name: String,
    parameterWithHint: LinkedHashMap<String, String>,
    format: String,
  ) : ReadOnlyProperty<SourceRequest, RequestContent<T>> {
    val requestContent =
      RequestContent(name, parameterWithHint, Json.serializersModule.serializer<T>(), format)
    requestContentMap[name] = requestContent
    // 使用属性代理以只允许全局变量
    return ReadOnlyProperty { _, _ ->
      requestContent
    }
  }
}