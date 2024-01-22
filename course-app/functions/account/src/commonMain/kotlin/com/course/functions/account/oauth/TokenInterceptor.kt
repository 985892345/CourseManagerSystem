package com.course.functions.account.oauth

import com.course.functions.network.api.ITokenClientInitializer
import com.g985892345.provider.api.annotation.ImplProvider
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CompletableJob

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/19 17:44
 */
@ImplProvider
object TokenInterceptor : ITokenClientInitializer {

  private val plugin = object : HttpClientPlugin<Unit, TokenInterceptor> {
    override val key: AttributeKey<TokenInterceptor> = AttributeKey("TokenPlugin")

    override fun prepare(block: Unit.() -> Unit): TokenInterceptor {
      return this@TokenInterceptor
    }

    override fun install(plugin: TokenInterceptor, scope: HttpClient) {
      install(scope)
    }
  }

  override fun initClientConfig(config: HttpClientConfig<*>) {
    config.install(plugin)
  }

  fun install(client: HttpClient) {
    client.plugin(HttpSend).intercept { request ->
      // 获取 accessToken，如果此时正处于刷新 token 的状态，则进行等待
      // 如果返回 null，则直接连接，保证不需要 token 的请求也能连接(但需要 token 的请求会被后端拒绝)
      val accessToken = Token.getOrWaitAccessToken() ?: return@intercept execute(request)
      val requestProxy = prepareRequest(request)
      requestProxy.bearerAuth(accessToken)
      var call = execute(requestProxy)
      // accessToken 过期
      if (call.response.headers["AccessToken-Expiration"] == "true") {
        // 尝试刷新 token，如果此时已经正处于刷新 token 的状态，则进行等待
        val newToken = Token.tryOrWaitRefreshToken(accessToken)
        val subRequest = prepareRequest(request)
        subRequest.bearerAuth(newToken.accessToken)
        call = execute(subRequest)
      }
      call
    }
  }

  private fun prepareRequest(request: HttpRequestBuilder): HttpRequestBuilder {
    val subRequest = HttpRequestBuilder().takeFrom(request)
    request.executionContext.invokeOnCompletion { cause ->
      val subRequestJob = subRequest.executionContext as CompletableJob
      if (cause == null) {
        subRequestJob.complete()
      } else subRequestJob.completeExceptionally(cause)
    }
    return subRequest
  }
}