package com.course.source.app.local.request

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.preferences.stringState
import com.course.components.utils.provider.Provider
import com.course.source.app.local.source.service.IDataSourceService
import com.russhwolf.settings.Settings
import com.russhwolf.settings.nullableString
import com.russhwolf.settings.string
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

  // 需要 by lazy，防止与 RequestContent 初始化构成冲突
  private val requestContent by lazy { RequestContent.find(contentKey)!! }

  @Transient
  private val settings = getSettings(contentKey, id)

  val title: MutableState<String> by lazy { settings.stringState("title", "${requestContent.name}${id}-${serviceKey}") }

  var sourceData: String? by settings.nullableString("sourceData")

  var requestParameters: String? by settings.nullableString("requestParameters")

  var error: String? by settings.nullableString("error")

  // response 分解为 2 个解决字符长度超长问题
  private var response1: String? by settings.nullableString("response1")
  private var response2: String by settings.string("response2", "")
  var response: String?
    get() = response1?.let { it + response2 }
    set(value) {
      if (value == null) {
        response1 = null
        response2 = ""
      } else if (value.length <= 8 * 1024) {
        response1 = value
        response2 = ""
      } else {
        response1 = value.substring(0, 8 * 1024)
        response2 = value.substring(8 * 1024)
      }
    }

  var requestUnitStatus by mutableStateOf(
    when {
      error != null -> RequestUnitStatus.Failure
      response1 != null -> RequestUnitStatus.Success
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