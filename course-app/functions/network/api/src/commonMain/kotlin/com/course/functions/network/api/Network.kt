package com.course.functions.network.api

import com.course.components.utils.provider.impl
import io.ktor.client.HttpClient

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/22 14:59
 */
object Network : INetworkService by INetworkService::class.impl {

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
}
