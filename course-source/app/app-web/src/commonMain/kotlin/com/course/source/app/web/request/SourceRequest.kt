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
    key: String,
    parameterWithHint: LinkedHashMap<String, String>,
    format: String,
  ) : ReadOnlyProperty<SourceRequest, RequestContent<T>> {
    val requestContent =
      RequestContent(key, parameterWithHint, Json.serializersModule.serializer<T?>(), format)
    requestContentMap[key] = requestContent
    // 使用属性代理以只允许全局变量
    return ReadOnlyProperty { _, _ ->
      requestContent
    }
  }
}