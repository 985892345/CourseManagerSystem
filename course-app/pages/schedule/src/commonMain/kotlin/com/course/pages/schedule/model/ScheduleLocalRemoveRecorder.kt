package com.course.pages.schedule.model

import com.course.components.utils.preferences.createSettings
import com.russhwolf.settings.string
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2024/5/4 16:44
 */
object ScheduleLocalRemoveRecorder {

  private val flowByNum = mutableMapOf<String, ScheduleLocalRemove>()

  private fun getLocalRemove(num: String): ScheduleLocalRemove {
    return flowByNum.getOrPut(num) {
      ScheduleLocalRemove(num)
    }
  }

  fun addRemoveSchedule(
    num: String,
    id: Int,
  ) {
    getLocalRemove(num).add(id)
  }

  fun getRemoveScheduleIds(num: String): Set<Int> {
    return getLocalRemove(num).flow.value
  }

  fun observeRemoveSchedule(num: String): Flow<Set<Int>> {
    return getLocalRemove(num).flow
  }

  fun clearRemoveSchedule(num: String) {
    getLocalRemove(num).clear()
  }

  private class ScheduleLocalRemove(
    val num: String,
  ) {
    private val settings = createSettings("Schedule-remove-$num")

    private var removeScheduleBeansStr by settings.string("beans", "[]")

    val flow = MutableStateFlow(
      Json.decodeFromString<Set<Int>>(removeScheduleBeansStr).toPersistentSet()
    )

    fun add(id: Int) {
      val new = flow.value.add(id)
      removeScheduleBeansStr = Json.encodeToString<Set<Int>>(new)
      flow.value = new
    }

    fun clear() {
      removeScheduleBeansStr = "[]"
      flow.value = persistentSetOf()
    }
  }
}