package com.course.functions.account.oauth

import com.course.components.utils.init.IInitialService
import com.course.components.utils.preferences.Preferences
import com.course.functions.network.Network
import com.course.shared.app.oauth.OauthApi
import com.course.shared.app.oauth.RefreshTokenBean
import com.g985892345.provider.api.annotation.ImplProvider
import com.russhwolf.settings.long
import com.russhwolf.settings.nullableString
import io.ktor.client.HttpClient
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.concurrent.Volatile
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/20 16:25
 */
@OptIn(DelicateCoroutinesApi::class)
internal object Token {

  /**
   * 获取当前的 accessToken，如果允许等待并想获取最新的状态，则推荐使用 [getOrWaitAccessToken]
   */
  val accessToken: String?
    get() = token?.accessToken

  @Volatile
  private var token: RefreshTokenBean? = tryGetTokenFromCache()

  private var tokenPreferences by Preferences.nullableString("token")
  private var tokenExpirationTimestamp by Preferences.long("token_expiration_timestamp", 0L)

  /**
   * 获取当前 accessToken，如果此时正在请求刷新 token，则进行等待
   */
  suspend fun getOrWaitAccessToken(): String? {
    val job = _refreshTokenJob ?: return accessToken
    return suspendCancellableCoroutine { coroutine ->
      val handle = job.invokeOnCompletion {
        if (it == null) {
          @OptIn(ExperimentalCoroutinesApi::class)
          coroutine.resume(job.getCompleted().accessToken)
        } else {
          coroutine.resumeWithException(it)
        }
      }
      coroutine.invokeOnCancellation {
        // 等待 token 请求的协程被取消
        handle.dispose()
      }
    }
  }

  fun updateToken(token: RefreshTokenBean?) {
    tokenPreferences = token?.let { Json.encodeToString(it) }
    tokenExpirationTimestamp = token?.expiresIn?.let {
      // 提前 10 分钟过期
      it + Clock.System.now().minus(10, DateTimeUnit.MINUTE).toEpochMilliseconds()
    } ?: 0L
    this.token = token
  }

  private fun tryGetTokenFromCache(): RefreshTokenBean? {
    val jsonBean = tokenPreferences
    if (jsonBean != null) {
      try {
        return Json.decodeFromString(jsonBean)
      } catch (e: Exception) {
        tokenPreferences = null
      }
    }
    return null
  }

  private val refreshTokenMutex = Mutex()

  /**
   * 尝试请求新的 token 或者挂起等待 token 的请求，内部确保了多协程竞争时只会触发一次 token 请求
   */
  suspend fun tryOrWaitRefreshToken(oldAccessToken: String): RefreshTokenBean {
    // 将 oldToken 与当前 token 进行比较，不相等时说明 token 已经更新，则直接返回
    token!!.let { if (oldAccessToken != it.accessToken) return it }
    // 1. 如果没有在请求 token 时就发起请求
    // 2. 如果已经在请求时则其他协程进行等待
    // 这里的判断需要原子性
    return refreshTokenMutex.withLock {
      // 双检锁 DCL
      // 如果前面的协程已经请求到了 token，则之后进入锁的协程直接返回 token
      token!!.let { if (oldAccessToken != it.accessToken) return it }
      waitRefreshTokenJob()
    }
  }

  private suspend fun waitRefreshTokenJob(): RefreshTokenBean {
    return suspendCancellableCoroutine { coroutine ->
      val job = getOrCreateRefreshTokenJob()
      val handle = job.invokeOnCompletion {
        if (it == null) {
          @OptIn(ExperimentalCoroutinesApi::class)
          coroutine.resume(job.getCompleted())
        } else {
          coroutine.resumeWithException(it)
        }
      }
      coroutine.invokeOnCancellation {
        // 等待 token 请求的协程被取消
        handle.dispose()
      }
    }
  }

  @Volatile
  private var _refreshTokenJob: Deferred<RefreshTokenBean>? = null

  private fun getOrCreateRefreshTokenJob(): Deferred<RefreshTokenBean> {
    _refreshTokenJob?.let { return it }
    // 注意: 这里使用新的协程作用域
    return GlobalScope.async(Dispatchers.IO) {
      try {
        val refreshToken = token?.refreshToken
        check(refreshToken != null) { "refreshToken 为空" }
        val response = tokenClient.post(OauthApi.Refresh) {
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
      } finally {
        _refreshTokenJob = null // 一旦请求完成即置为 null，这里与当前方法不属于同个堆栈
      }
    }.also { _refreshTokenJob = it }
  }

  private val tokenClient = HttpClient {
    Network.defaultConfig(this)
    Network.initClientInitializer(this)
  }

  @ImplProvider(clazz = IInitialService::class, name = "CheckAccessTokenExpiration")
  private object CheckAccessTokenExpiration : IInitialService {
    override fun onAppInit() {
      val token = token ?: return
      val nowTimestamp = Clock.System.now().toEpochMilliseconds()
      val expiresInTimestamp = tokenExpirationTimestamp
      if (nowTimestamp >= expiresInTimestamp) {
        // 如果 token 过期，则在应用初始化时进行刷新
        GlobalScope.launch(Dispatchers.IO) {
          tryOrWaitRefreshToken(token.accessToken)
        }
      }
    }
  }
}