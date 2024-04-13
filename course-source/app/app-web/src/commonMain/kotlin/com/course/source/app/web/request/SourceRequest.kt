package com.course.source.app.web.request

import com.course.components.utils.provider.Provider
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

  companion object {
    val AllImpl by lazy {
      Provider.getAllImpl(SourceRequest::class).map {
        it.value.get()
      }
    }
  }

  val requestSource: MutableMap<String, IRequestSource> = LinkedHashMap()

  protected inline fun <reified T : Any> requestContent(
    key: String,
    name: String,
    parameterWithHint: LinkedHashMap<String, String>,
    format: String,
  ) : ReadOnlyProperty<SourceRequest, RequestContent<T>> {
    val requestContent =
      RequestContent(key, name, parameterWithHint, Json.serializersModule.serializer<T?>(), format)
    requestSource[key] = requestContent
    // 使用属性代理以只允许全局变量
    return ReadOnlyProperty { _, _ ->
      requestContent
    }
  }

  protected inline fun <reified T : Any> requestGroup(
    key: String,
    name: String,
    parameterWithHint: LinkedHashMap<String, String>,
    format: String,
  ) : ReadOnlyProperty<SourceRequest, RequestGroup<T>> {
    val requestGroup =
      RequestGroup(key, name, parameterWithHint, Json.serializersModule.serializer<T?>(), format)
    requestSource[key] = requestGroup
    return ReadOnlyProperty { _, _ ->
      requestGroup
    }
  }
}