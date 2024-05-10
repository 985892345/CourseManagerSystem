package com.course.pages.schedule.model

import com.course.components.utils.preferences.createSettings
import com.course.source.app.schedule.ScheduleBean
import com.russhwolf.settings.string
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 保存未请求成功的更新操作
 *
 * @author 985892345
 * 2024/5/4 16:43
 */
object ScheduleLocalUpdateRepository {

  private val localUpdateByNum = mutableMapOf<String, ScheduleLocalUpdate>()

  private fun getLocalUpdate(num: String): ScheduleLocalUpdate {
    return localUpdateByNum.getOrPut(num) {
      ScheduleLocalUpdate(num)
    }
  }

  fun addUpdateSchedule(
    num: String,
    bean: ScheduleBean,
  ) {
    getLocalUpdate(num).add(bean)
  }

  fun getUpdateSchedule(num: String): List<ScheduleBean> {
    return getLocalUpdate(num).flow.value
  }

  fun observeUpdateSchedule(num: String): Flow<List<ScheduleBean>> {
    return getLocalUpdate(num).flow
  }

  fun removeUpdateSchedule(num: String, id: Int): Boolean {
    return getLocalUpdate(num).remove(id)
  }

  fun clearUpdateSchedule(num: String) {
    getLocalUpdate(num).clear()
  }

  private class ScheduleLocalUpdate(
    val num: String,
  ) {
    private val settings = createSettings("Schedule-update-$num")

    private var updateScheduleBeansStr by settings.string("beans", "[]")

    val flow = MutableStateFlow(
      runCatching {
        Json.decodeFromString<List<ScheduleBean>>(updateScheduleBeansStr)
      }.onFailure {
        updateScheduleBeansStr = "[]"
      }.getOrNull()?.toPersistentList() ?: persistentListOf()
    )

    fun add(bean: ScheduleBean) {
      val index = flow.value.indexOfFirst { it.id == bean.id }
      val new = if (index >= 0) {
        flow.value.set(index, bean)
      } else {
        flow.value.add(bean)
      }
      updateScheduleBeansStr = Json.encodeToString<List<ScheduleBean>>(new)
      flow.value = new
    }

    fun remove(id: Int): Boolean {
      val index = flow.value.indexOfFirst { it.id == id }
      if (index >= 0) {
        val new = flow.value.removeAt(index)
        updateScheduleBeansStr = Json.encodeToString<List<ScheduleBean>>(new)
        flow.value = new
      }
      return false
    }

    fun clear() {
      updateScheduleBeansStr = "[]"
      flow.value = persistentListOf()
    }
  }
}