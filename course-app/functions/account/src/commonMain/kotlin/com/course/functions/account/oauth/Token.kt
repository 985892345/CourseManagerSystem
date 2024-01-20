package com.course.functions.account.oauth

import com.course.components.utils.preferences.Preferences
import com.course.functions.network.Network
import com.course.shared.app.oauth.IRefreshTokenBean
import com.course.shared.app.oauth.OauthApi
import com.russhwolf.settings.nullableString
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.concurrent.Volatile
import kotlin.coroutines.resume

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/20 16:25
 */
internal object Token {

  val accessToken: String?
    get() = tokenBean?.accessToken

  @Volatile
  private var tokenBean: RefreshTokenBean? = tryGetTokenFromCache()

  private var tokenBeanPreferences by Preferences.nullableString("token")

  fun updateToken(token: RefreshTokenBean?) {
    tokenBean = token
    tokenBeanPreferences = token?.let { Json.encodeToString(it) }
  }

  private fun tryGetTokenFromCache(): RefreshTokenBean? {
    val jsonBean = tokenBeanPreferences
    if (jsonBean != null) {
      try {
        return Json.decodeFromString(jsonBean)
      } catch (e: Exception) {
        tokenBeanPreferences = null
      }
    }
    return null
  }

  private var refreshTokenJob: Deferred<RefreshTokenBean>? = null

  private val refreshTokenMutex = Mutex()

  /**
   * 尝试请求新的 token 或者挂起等待 token 的请求，确保多协程竞争时只触发一次 token 请求
   */
  suspend fun tryOrWaitRefreshToken(oldToken: String): RefreshTokenBean {
    // 将 oldToken 与当前 token 进行比较，不相等时说明 token 已经更新，则直接返回
    tokenBean!!.let { if (oldToken != it.accessToken) return it }
    // 1. 如果没有在请求 token 就发起请求
    // 2. 如果已经在请求时则其他协程进行等待
    // 这里的判断需要原子性
    return refreshTokenMutex.withLock {
      // 双检锁 DCL
      // 如果前面的协程已经请求到了 token，则之后进入锁的协程直接返回 token
      tokenBean!!.let { if (oldToken != it.accessToken) return it }
      // 能运行到这里有两种情况
      // 1. 第一次刷新 token
      // 2. 第一次刷新 token 的协程被取消了，但 token 的请求还未结束，会由下一个协程进入，继续挂起
      waitRefreshTokenJob()
    }
  }

  private suspend fun waitRefreshTokenJob(): RefreshTokenBean {
    return suspendCancellableCoroutine { coroutine ->
      val job = refreshTokenJob ?: createRefreshTokenJob().also { refreshTokenJob = it }
      val handle = job.invokeOnCompletion {
        @OptIn(ExperimentalCoroutinesApi::class)
        coroutine.resume(job.getCompleted())
        refreshTokenJob = null
      }
      coroutine.invokeOnCancellation {
        // 等待 token 请求的协程被取消
        handle.dispose()
      }
    }
  }

  private fun createRefreshTokenJob(): Deferred<RefreshTokenBean> {
    // 注意: 这里使用新的协程作用域
    @OptIn(DelicateCoroutinesApi::class)
    return GlobalScope.async(Dispatchers.IO) {
      val refreshToken = tokenBean?.refreshToken
      check(refreshToken != null) { "refreshToken 为空" }
      val response = Network.client.post(OauthApi.Refresh) {
        setBody(FormDataContent(Parameters.build {
          append("refresh_token", refreshToken)
        }))
      }
      if (response.headers["RefreshToken-Expiration"] == "true") {
        // refreshToken 也过期了，需要重新登陆
        updateToken(null)
        throw RefreshTokenExpirationException()
      }
      response.body<RefreshTokenBean>().also { updateToken(it) }
    }
  }

  @Serializable
  class RefreshTokenBean(
    override val accessToken: String,
    override val refreshToken: String,
  ) : IRefreshTokenBean
}