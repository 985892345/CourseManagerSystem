package com.course.source.app.web.request

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import com.course.components.utils.provider.Provider
import com.course.components.utils.serializable.StringStateSerializable
import com.course.source.app.web.source.service.IDataSourceService
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

/**
 * .
 *
 * @author 985892345
 * 2024/3/22 16:36
 */
@Stable
@Serializable
class RequestUnit(
  var title: String,
  val serviceKey: String,
  val id: Int = -1,
  var sourceData: String? = null,
  var error: String? = null,
  var response: String? = null,
  var duration: Duration = (-1).seconds,
) {

  suspend fun request(
    parameters: Map<String, String>,
  ): String {
    val service = Provider.implOrNull(IDataSourceService::class, serviceKey)
      ?: throw RuntimeException("未找到服务 $serviceKey")
    val newResponse: String
    duration = measureTime {
      newResponse = withTimeout(10.seconds) {
        service.request(sourceData, parameters)
      }
    }
    response = newResponse
    return newResponse
  }
}