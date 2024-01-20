package com.course.functions.network

import com.course.components.utils.provider.Provider
import com.course.functions.network.api.IClientInitializer
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/12 15:23
 */
object Network {

  /**
   * 默认 HttpClient
   *
   * 如果需要自定义 HttpClient，请使用:
   * ```
   * client.config {
   *   // 覆盖默认配置
   * }
   * ```
   */
  val client by lazy {
    HttpClient {
      defaultConfig(this)
      Provider.getAllImpl(IClientInitializer::class).forEach {
        it.value.get().initClientConfig(this)
      }
    }
  }

  private fun defaultConfig(config: HttpClientConfig<*>) {
    with(config) {
      install(ContentNegotiation) {
        json(Json {
          isLenient = true
          ignoreUnknownKeys = true
          encodeDefaults = true
          @OptIn(ExperimentalSerializationApi::class)
          explicitNulls = false
        })
      }
      defaultRequest {
        url("http://127.0.0.1:8080")
      }
    }
  }
}