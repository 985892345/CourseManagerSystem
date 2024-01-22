package com.course.functions.network

import com.course.components.utils.provider.Provider
import com.course.components.utils.provider.implOrNull
import com.course.functions.network.api.IClientInitializer
import com.course.functions.network.api.IDebugClientInitializer
import com.course.functions.network.api.INetworkService
import com.course.functions.network.api.ITokenClientInitializer
import com.g985892345.provider.api.annotation.ImplProvider
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
@ImplProvider
object NetworkServiceImpl : INetworkService {

  override fun defaultConfig(config: HttpClientConfig<*>) {
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
      IDebugClientInitializer::class.implOrNull?.initClientConfig(config)
    }
  }

  override fun initTokenClientInitializer(config: HttpClientConfig<*>) {
    ITokenClientInitializer::class.implOrNull?.initClientConfig(config)
  }

  override fun initClientInitializer(config: HttpClientConfig<*>) {
    Provider.getAllImpl(IClientInitializer::class).forEach {
      it.value.get().initClientConfig(config)
    }
  }
}