package com.course.components.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
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
   */
  val client = HttpClient {
    install(ContentNegotiation) {
      json(Json {
        isLenient = true
        ignoreUnknownKeys = true
      })
    }
  }
}