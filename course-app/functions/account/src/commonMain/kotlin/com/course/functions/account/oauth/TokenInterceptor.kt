package com.course.functions.account.oauth

import com.course.functions.network.IClientInitializer
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
@ImplProvider(clazz = IClientInitializer::class, name = "TokenPlugin")
object TokenInterceptor : IClientInitializer {

  private val plugin = object : HttpClientPlugin<Unit, TokenInterceptor> {
    override val key: AttributeKey<TokenInterceptor> = AttributeKey("TokenPlugin")

    override fun prepare(block: Unit.() -> Unit): TokenInterceptor {
      return this@TokenInterceptor
    }

    override fun install(plugin: TokenInterceptor, scope: HttpClient) {
      install(scope)
    }
  }

  override fun init(config: HttpClientConfig<*>) {
    config.install(plugin)
  }

  fun install(client: HttpClient) {
    client.plugin(HttpSend).intercept { request ->
      val oldToken = Token.accessToken ?: return@intercept execute(request)
      val requestProxy = prepareRequest(request)
      requestProxy.bearerAuth(oldToken)
      var call = execute(requestProxy)
      if (call.response.headers["AccessToken-Expiration"] == "true") {
        val newToken = Token.tryOrWaitRefreshToken(oldToken)
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

class RefreshTokenExpirationException : RuntimeException("refreshToken 过期")