package com.course.functions.network.api

import io.ktor.client.HttpClientConfig

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/22 15:01
 */
interface INetworkService {

  fun defaultConfig(config: HttpClientConfig<*>)

  fun initTokenClientInitializer(config: HttpClientConfig<*>)

  fun initClientInitializer(config: HttpClientConfig<*>)
}