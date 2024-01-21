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

interface ITokenClientInitializer : IClientInitializer

interface IDebugClientInitializer : IClientInitializer