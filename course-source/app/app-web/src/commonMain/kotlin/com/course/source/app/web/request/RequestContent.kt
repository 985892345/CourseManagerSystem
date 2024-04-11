package com.course.source.app.web.request

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.course.components.utils.coroutine.AppComposeCoroutineScope
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.preferences.longState
import com.course.components.utils.provider.Provider
import com.russhwolf.settings.long
import com.russhwolf.settings.string
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

/**
 * .
 *
 * @author 985892345
 * 2024/3/22 15:44
 */
@Stable
data class RequestContent<T : Any>(
  val key: String,
  val parameterWithHint: LinkedHashMap<String, String>,
  val resultSerializer: KSerializer<T?>,
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

  private val settings = createSettings("RequestContent-$key")

  val requestUnits: SnapshotStateList<RequestUnit> = settings.getStringOrNull("requestUnits")
    ?.let { Json.decodeFromString<List<RequestUnit>>(it).toMutableStateList() }
    ?: SnapshotStateList()

  var requestTimestamp: Long by settings.longState("requestTimestamp", 0L)
    private set

  var responseTimestamp: Long by settings.longState("responseTimestamp", 0L)
    private set

  var cacheExpiration: Long by settings.long("cacheExpiration", 12.hours.inWholeMilliseconds)

  private var prevRequestValues: String by settings.string("prevRequestValues", "[]")

  var requestContentStatus: RequestContentStatus by mutableStateOf(
    when {
      requestUnits.isEmpty() -> RequestContentStatus.Empty
      requestTimestamp == 0L -> RequestContentStatus.None
      responseTimestamp >= requestTimestamp -> RequestContentStatus.Success
      else -> RequestContentStatus.Failure
    }
  )
    private set

  suspend fun request(isForce: Boolean, vararg values: String): T? {
    if (values.size != parameterWithHint.size)
      throw IllegalArgumentException("参数数量不匹配，应有 ${parameterWithHint.size}, 实有: ${values.size}")
    if (requestUnits.isEmpty()) throw IllegalStateException("未设置请求")
    val nowTime = Clock.System.now().toEpochMilliseconds()
    val newValues = Json.encodeToString(values)
    val isCacheValid = nowTime - responseTimestamp < cacheExpiration &&
        newValues == prevRequestValues
    prevRequestValues = newValues
    if (!isForce && isCacheValid) {
      for (unit in requestUnits.toList()) {
        if (unit.requestUnitStatus == RequestUnit.RequestUnitStatus.Success) {
          try {
            return Json.decodeFromString(resultSerializer, unit.response!!).apply {
              requestContentStatus = RequestContentStatus.HitCache
            }
          } catch (e: Exception) {
            unit.error = e.stackTraceToString()
            unit.requestUnitStatus = RequestUnit.RequestUnitStatus.Failure
          }
        }
      }
    }
    requestContentStatus = RequestContentStatus.Requesting
    requestTimestamp = nowTime
    var index = 0
    val parameters = parameterWithHint.mapValues { values[index++] }
    for (unit in requestUnits.toList()) {
      try {
        unit.requestUnitStatus = RequestUnit.RequestUnitStatus.Requesting
        val response: String
        unit.duration = measureTime {
          response = withTimeout(10.seconds) {
            unit.request(parameters)
          }
        }.inWholeMilliseconds
        unit.response = response
        unit.error = null
        val result = Json.decodeFromString(resultSerializer, response)
        // 如果 result 反序列化异常，则认为请求失败
        responseTimestamp = Clock.System.now().toEpochMilliseconds()
        requestContentStatus = RequestContentStatus.Success
        unit.requestUnitStatus = RequestUnit.RequestUnitStatus.Success
        return result
      } catch (e: Exception) {
        // nothing
        unit.error = e.stackTraceToString()
        unit.requestUnitStatus = RequestUnit.RequestUnitStatus.Failure
      }
    }
    if (isCacheValid) {
      for (unit in requestUnits.toList()) {
        if (unit.requestUnitStatus == RequestUnit.RequestUnitStatus.Success) {
          try {
            return Json.decodeFromString(resultSerializer, unit.response!!).apply {
              requestContentStatus = RequestContentStatus.FailureButHitCache
            }
          } catch (e: Exception) {
            unit.error = e.stackTraceToString()
            unit.requestUnitStatus = RequestUnit.RequestUnitStatus.Failure
          }
        }
      }
    }
    requestContentStatus = RequestContentStatus.Failure
    throw IllegalStateException("请求全部失败")
  }

  init {
    AppComposeCoroutineScope.launch {
      snapshotFlow { requestUnits.toList() }.drop(1).collect {
        settings.putString("requestUnits", Json.encodeToString(it))
        if (requestUnits.isNotEmpty() && requestContentStatus == RequestContentStatus.Empty) {
          requestContentStatus = RequestContentStatus.None
        } else if (requestUnits.isEmpty()) {
          requestContentStatus = RequestContentStatus.Empty
        }
      }
    }
  }

  enum class RequestContentStatus {
    Empty, // 未设置请求
    None, // 设置了请求但未触发过请求
    HitCache, // 命中缓存
    Requesting, // 请求中
    Success, // 请求成功
    Failure, // 请求失败
    FailureButHitCache, // 请求失败但缓存可用
  }
}