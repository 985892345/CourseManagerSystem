package com.course.functions.network.api

import io.ktor.client.HttpClientConfig

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/20 15:10
 */
interface IClientInitializer {
  fun initClientConfig(config: HttpClientConfig<*>)
}

// 处理 token
interface ITokenClientInitializer : IClientInitializer

// debug 调试使用
interface IDebugClientInitializer : IClientInitializer