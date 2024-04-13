package com.course.source.app.web.request

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.preferences.stringState
import com.course.components.utils.provider.Provider
import com.course.source.app.web.source.service.IDataSourceService
import com.russhwolf.settings.Settings
import com.russhwolf.settings.long
import com.russhwolf.settings.nullableString
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * .
 *
 * @author 985892345
 * 2024/3/22 16:36
 */
@Stable
@Serializable
data class RequestUnit(
  val contentKey: String,
  val id: Int,
  val serviceKey: String,
) {

  private val requestContent by lazy { RequestContent.find(contentKey)!! }

  @Transient
  private val settings = getSettings(contentKey, id)

  var title = settings.stringState("title", "${contentKey}${id}-${serviceKey}")

  var sourceData: String? by settings.nullableString("sourceData")

  var error: String? by settings.nullableString("error")

  var response: String? by settings.nullableString("response")

  var duration: Long by settings.long("duration", -1)

  var requestUnitStatus by mutableStateOf(
    when {
      error != null -> RequestUnitStatus.Failure
      response != null -> RequestUnitStatus.Success
      else -> RequestUnitStatus.None
    }
  )

  // 发生修改的次数
  var changedCount: Int by mutableStateOf(0)

  suspend fun request(
    parameters: Map<String, String>,
  ): String {
    val service = Provider.implOrNull(IDataSourceService::class, serviceKey)
      ?: throw RuntimeException("未找到服务 $serviceKey")
    return service.request(sourceData, parameters)
  }

  fun delete() {
    requestContent.requestUnits.remove(this)
    settings.clear()
  }

  companion object {
    private fun getSettings(contentKey: String, id: Int): Settings {
      return createSettings("RequestContent-$contentKey-$id")
    }

    fun create(
      contentKey: String,
      id: Int,
      serviceKey: String,
    ): RequestUnit {
      val settings = getSettings(contentKey, id)
      settings.clear()
      return RequestUnit(
        contentKey = contentKey,
        id = id,
        serviceKey = serviceKey,
      )
    }
  }

  enum class RequestUnitStatus {
    None, // 设置了请求但未触发过请求
    Requesting, // 请求中
    Success, // 请求成功
    Failure, // 请求失败
  }
}