package com.course.source.app.web.request

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.course.components.utils.provider.Provider
import com.course.source.app.web.source.service.IDataSourceService
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * .
 *
 * @author 985892345
 * 2024/3/22 16:36
 */
@Stable
@Serializable
data class RequestUnit(
  var title: String,
  val serviceKey: String,
  val id: Int,
  var sourceData: String? = null,
  var error: String? = null,
  var response: String? = null,
  var duration: Duration = (-1).seconds,
) {

  var requestUnitStatus by mutableStateOf(
    when {
      error != null -> RequestUnitStatus.Failure
      response != null -> RequestUnitStatus.Success
      else -> RequestUnitStatus.None
    }
  )

  suspend fun request(
    parameters: Map<String, String>,
  ): String {
    val service = Provider.implOrNull(IDataSourceService::class, serviceKey)
      ?: throw RuntimeException("未找到服务 $serviceKey")
    return service.request(sourceData, parameters)
  }

  enum class RequestUnitStatus {
    None, // 设置了请求但未触发过请求
    Requesting, // 请求中
    Success, // 请求成功
    Failure, // 请求失败
  }
}