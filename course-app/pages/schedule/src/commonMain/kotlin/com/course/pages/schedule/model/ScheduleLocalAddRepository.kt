package com.course.pages.schedule.model

import com.course.components.utils.preferences.createSettings
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.schedule.ScheduleBean
import com.course.source.app.schedule.ScheduleRepeat
import com.russhwolf.settings.string
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 保存未请求成功的添加操作
 *
 * @author 985892345
 * 2024/5/4 15:37
 */
object ScheduleLocalAddRepository {

  private val localAddByNum = mutableMapOf<String, ScheduleLocalAdd>()

  private fun getLocalAdd(num: String): ScheduleLocalAdd {
    return localAddByNum.getOrPut(num) {
      ScheduleLocalAdd(num)
    }
  }

  fun observeAddSchedule(num: String): Flow<List<ScheduleBean>> {
    return getLocalAdd(num).flow
  }

  fun addSchedule(
    num: String,
    title: String,
    description: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
  ): ScheduleBean {
    return getLocalAdd(num).add(
      title = title,
      description = description,
      startTime = startTime,
      minuteDuration = minuteDuration,
      repeat = repeat
    )
  }

  fun updateAddSchedule(
    num: String,
    bean: ScheduleBean
  ): Boolean {
    return getLocalAdd(num).update(bean)
  }

  fun removeAddSchedule(
    num: String,
    id: Int,
  ): Boolean {
    return getLocalAdd(num).remove(id)
  }

  fun getAddSchedule(num: String): List<ScheduleBean> {
    return getLocalAdd(num).flow.value
  }

  fun clearAddSchedule(num: String) {
    getLocalAdd(num).clear()
  }

  private class ScheduleLocalAdd(val num: String) {

    private val settings = createSettings("Schedule-add-$num")

    private var localScheduleBeansStr by settings.string("beans", "[]")

    val flow = MutableStateFlow(
      Json.decodeFromString<List<ScheduleBean>>(localScheduleBeansStr).toPersistentList()
    )

    fun add(
      title: String,
      description: String,
      startTime: MinuteTimeDate,
      minuteDuration: Int,
      repeat: ScheduleRepeat,
    ): ScheduleBean {
      // 本地日程 id 以负数进行递减
      val id = flow.value.lastOrNull()?.id ?: -1
      val bean = ScheduleBean(
        id = id - 1,
        title = title,
        description = description,
        startTime = startTime,
        minuteDuration = minuteDuration,
        repeat = repeat,
      )
      val new = flow.value.add(bean)
      localScheduleBeansStr = Json.encodeToString<List<ScheduleBean>>(new)
      flow.value = new
      return bean
    }

    fun update(
      bean: ScheduleBean
    ): Boolean {
      val oldBeanIndex = flow.value.indexOfFirst { it.id == bean.id }
      if (oldBeanIndex == -1) return false
      val new = flow.value.set(oldBeanIndex, bean)
      localScheduleBeansStr = Json.encodeToString<List<ScheduleBean>>(new)
      flow.value = new
      return true
    }

    fun remove(id: Int): Boolean {
      val oldBeanIndex = flow.value.indexOfFirst { it.id == id }
      if (oldBeanIndex == -1) return false
      val new = flow.value.removeAt(oldBeanIndex)
      localScheduleBeansStr = Json.encodeToString<List<ScheduleBean>>(new)
      flow.value = new
      return true
    }

    fun clear() {
      localScheduleBeansStr = "[]"
      flow.value = persistentListOf()
    }
  }
}