package com.course.functions.network

import com.course.components.utils.provider.Provider
import com.course.functions.network.api.IClientInitializer
import com.course.functions.network.api.IDebugClientInitializer
import com.course.functions.network.api.ITokenClientInitializer
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
   * 如果需要自定义 HttpClient，分为多种情况
   * ```
   * // 只需要默认的基础配置
   * HttpClient {
   *   defaultConfig(this)
   * }
   *
   * // 如果需要 token，则添加 initTokenClientInitializer(this)
   * HttpClient {
   *   defaultConfig(this)
   *   initTokenClientInitializer(this)
   * }
   *
   * // 如果允许其他的 IClientInitializer (不包含 token)
   * HttpClient {
   *   defaultConfig(this)
   *   initClientInitializer(this)
   * }
   * ```
   */
  val client by lazy {
    HttpClient {
      defaultConfig(this)
      initTokenClientInitializer(this)
      initClientInitializer(this)
    }
  }

  fun defaultConfig(config: HttpClientConfig<*>) {
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
      initDebugClientInitializer(this)
    }
  }

  fun initTokenClientInitializer(config: HttpClientConfig<*>) {
    Provider.getImplOrNull(ITokenClientInitializer::class)?.initClientConfig(config)
  }

  private fun initDebugClientInitializer(config: HttpClientConfig<*>) {
    Provider.getImplOrNull(IDebugClientInitializer::class)?.initClientConfig(config)
  }

  fun initClientInitializer(config: HttpClientConfig<*>) {
    Provider.getAllImpl(IClientInitializer::class).forEach {
      it.value.get().initClientConfig(config)
    }
  }
}