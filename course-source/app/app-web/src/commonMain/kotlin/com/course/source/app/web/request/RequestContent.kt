package com.course.source.app.web.request

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.provider.Provider
import com.russhwolf.settings.long
import com.russhwolf.settings.set
import kotlinx.datetime.Clock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.hours

/**
 * .
 *
 * @author 985892345
 * 2024/3/22 15:44
 */
@Stable
data class RequestContent<T : Any>(
  val name: String,
  val parameterWithHint: LinkedHashMap<String, String>,
  val resultSerializer: KSerializer<T>,
  val format: String,
) {

  companion object {
    val RequestMap by lazy {
      Provider.getAllImpl(SourceRequest::class)
        .map { it.value.get().requestContentMap }
        .fold(hashMapOf<String, RequestContent<*>>()) { map1, map2 ->
          map1.putAll(map2)
          map1
        }
    }
  }

  private val settings = createSettings("RequestContent-$name")

  val requestUnits: MutableList<RequestUnit> = settings.getStringOrNull("requestUnits")
    ?.let { Json.decodeFromString(it) } ?: mutableListOf()

  var requestTimestamp: Long by mutableLongStateOf(settings.getLong("requestTimestamp", 0L))
    private set

  var responseTimestamp: Long by mutableLongStateOf(settings.getLong("responseTimestamp", 0L))
    private set

  private var prevRequestCache: String? = settings.getStringOrNull("cache")

  var cacheExpiration: Long by settings.long("cacheExpiration", 12.hours.inWholeMilliseconds)

  var requestStatus: RequestStatus by mutableStateOf(
    when {
      requestUnits.isEmpty() -> RequestStatus.Empty
      requestTimestamp == 0L -> RequestStatus.None
      responseTimestamp >= requestTimestamp -> RequestStatus.Success
      else -> RequestStatus.Failure
    }
  )

  suspend fun request(isForce: Boolean, vararg values: String): T {
    if (values.size != parameterWithHint.size)
      throw IllegalArgumentException("参数数量不匹配，应有 ${parameterWithHint.size}, 实有: ${values.size}")
    val cache = prevRequestCache
    val nowTime = Clock.System.now().toEpochMilliseconds()
    val isCacheValid = cache != null && nowTime - responseTimestamp < cacheExpiration
    if (isForce || !isCacheValid) {
      if (requestUnits.isEmpty()) throw IllegalStateException("未设置请求")
      requestStatus = RequestStatus.Requesting
      requestTimestamp = nowTime
      var index = 0
      val parameters = parameterWithHint.mapValues { values[index++] }
      for (unit in requestUnits) {
        try {
          val response = unit.request(parameters)
          val result = Json.decodeFromString(resultSerializer, response)
          // 如果 result 反序列化异常，则认为请求失败
          responseTimestamp = Clock.System.now().toEpochMilliseconds()
          prevRequestCache = response
          save()
          requestStatus = RequestStatus.Success
          return result
        } catch (e: Exception) {
          // nothing
          unit.error = e.stackTraceToString()
        }
      }
      save()
      if (isCacheValid) {
        requestStatus = RequestStatus.FailureButHitCache
        return Json.decodeFromString(resultSerializer, cache!!)
      } else {
        requestStatus = RequestStatus.Failure
        throw IllegalStateException("请求全部失败")
      }
    } else {
      requestStatus = RequestStatus.HitCache
      return Json.decodeFromString(resultSerializer, cache!!)
    }
  }

  private fun save() {
    settings.putString("requestUnits", Json.encodeToString(requestUnits))
    settings.putLong("requestTimestamp", requestTimestamp)
    settings.putLong("responseTimestamp", responseTimestamp)
    settings["cache"] = prevRequestCache
  }

  enum class RequestStatus {
    Empty, // 未设置请求
    None, // 设置了请求但未触发过请求
    HitCache, // 命中缓存
    Requesting, // 请求中
    Success, // 请求成功
    Failure, // 请求失败
    FailureButHitCache, // 请求失败但缓存可用
  }
}