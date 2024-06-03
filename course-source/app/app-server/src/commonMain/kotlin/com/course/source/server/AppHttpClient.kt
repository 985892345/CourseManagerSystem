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
  defaultRequest {
    url("http://192.168.5.228:8080")
  }
  install(ContentNegotiation) {
    json(Json {
      isLenient = true
      ignoreUnknownKeys = true
      encodeDefaults = true
      explicitNulls = false
    })
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
