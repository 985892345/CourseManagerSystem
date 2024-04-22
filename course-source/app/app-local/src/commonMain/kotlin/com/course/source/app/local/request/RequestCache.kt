package com.course.source.app.local.request

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.preferences.longState
import com.russhwolf.settings.nullableString
import com.russhwolf.settings.string
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * .
 *
 * @author 985892345
 * 2024/4/19 15:03
 */
@Serializable
class RequestCache(
  val requestContentKey: String,
  val input: String,
) {

  @Transient
  private val cacheSettings = createSettings("$requestContentKey-$input")

  var responseTimestamp: Long by cacheSettings.longState("responseTimestamp", 0L)

  private var response1: String? by cacheSettings.nullableString("response1")
  private var response2: String by cacheSettings.string("response2", "")

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

  fun clear() {
    cacheSettings.clear()
  }
}