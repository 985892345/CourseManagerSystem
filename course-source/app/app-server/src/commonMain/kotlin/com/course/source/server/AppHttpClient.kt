package com.course.source.server

import com.course.components.utils.preferences.Settings
import com.russhwolf.settings.nullableString
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 19:28
 */
var Token by Settings.nullableString("token")

val AppHttpClient = HttpClient {
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
}.apply {
  plugin(HttpSend).intercept { request ->
    val token = Token
    if (token != null) {
      execute(request.apply { header("token", token) })
    } else {
      execute(request)
    }
  }
}