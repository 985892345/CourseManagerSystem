package com.course.pages.schedule.model

import com.course.components.base.account.Account
import com.course.components.utils.coroutine.AppCoroutineScope
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.utils.source.onSuccess
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.schedule.ScheduleApi
import com.course.source.app.schedule.ScheduleBean
import com.course.source.app.schedule.ScheduleRepeat
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2024/5/4 14:57
 */
object ScheduleRepository {

  private val scheduleBeansFlowByNum =
    mutableMapOf<String, MutableStateFlow<PersistentList<ScheduleBean>>>()

  private fun getScheduleBeansFlow(num: String): MutableStateFlow<PersistentList<ScheduleBean>> {
    return scheduleBeansFlowByNum.getOrPut(num) {
      MutableStateFlow(getScheduleBeanFromCache(num) ?: persistentListOf())
    }
  }

  fun observeScheduleBean(): Flow<List<ScheduleBean>> {
    val num = Account.value?.num ?: return emptyFlow()
    return getScheduleBeansFlow(num).onStart {
      AppCoroutineScope.launch {
        // 开启其他协程进行网络请求
        runCatching {
          refreshScheduleBean(num)
        }
      }
    }.distinctUntilChanged()
  }

  fun getScheduleBeanFromCache(num: String): PersistentList<ScheduleBean>? {
    val settings = createSettings("Schedule-${num}")
    val json = settings.getStringOrNull("data")
    if (json == null) {
      return ScheduleLocalAddRepository.getAddSchedule(num).toPersistentList()
    } else {
      return try {
        val update = ScheduleLocalUpdateRepository.getUpdateSchedule(num).associateBy { it.id }
        val remove = ScheduleLocalRemoveRepository.getRemoveScheduleIds(num)
        Json.decodeFromString<List<ScheduleBean>>(json).map {
          update[it.id] ?: it
        }.filter {
          it.id !in remove
        }.toPersistentList()
          .addAll(ScheduleLocalAddRepository.getAddSchedule(num))
      } catch (e: SerializationException) {
        settings.remove("data")
        null
      }
    }
  }

  private fun setScheduleBeanToCache(num: String, beans: List<ScheduleBean>) {
    val settings = createSettings("Schedule-$num")
    val json = Json.encodeToString(beans)
    settings.putString("data", json)
  }

  /**
   * 获取所有日程数据
   */
  suspend fun refreshScheduleBean(num: String): List<ScheduleBean> {
    return withContext(Dispatchers.IO) {
      Source.api(ScheduleApi::class)
        .getSchedule(
          addBeans = ScheduleLocalAddRepository.getAddSchedule(num),
          updateBeans = ScheduleLocalUpdateRepository.getUpdateSchedule(num),
          removeIds = ScheduleLocalRemoveRepository.getRemoveScheduleIds(num),
        ).onSuccess {
          ScheduleLocalAddRepository.clearAddSchedule(num)
          ScheduleLocalUpdateRepository.clearUpdateSchedule(num)
          ScheduleLocalRemoveRepository.clearRemoveSchedule(num)
          setScheduleBeanToCache(num, it)
          getScheduleBeansFlow(num).value = it.toPersistentList()
        }.getOrThrow()
    }
  }

  fun addSchedule(
    title: String,
    description: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
    textColor: String,
    backgroundColor: String,
  ) {
    AppCoroutineScope.launch(Dispatchers.IO) {
      val num = Account.value?.num ?: return@launch
      val result = runCatching {
        Source.api(ScheduleApi::class)
          .addSchedule(
            title = title,
            description = description,
            startTime = startTime,
            minuteDuration = minuteDuration,
            repeat = repeat,
            textColor = textColor,
            backgroundColor = backgroundColor,
          )
          .getOrThrow()
      }.map {
        ScheduleBean(
          id = it,
          title = title,
          description = description,
          startTime = startTime,
          minuteDuration = minuteDuration,
          repeat = repeat,
          textColor = textColor,
          backgroundColor = backgroundColor,
        )
      }
      val bean = result.getOrElse {
        ScheduleLocalAddRepository.addSchedule(
          num = num,
          title = title,
          description = description,
          startTime = startTime,
          minuteDuration = minuteDuration,
          repeat = repeat,
          textColor = textColor,
          backgroundColor = backgroundColor,
        )
      }
      getScheduleBeansFlow(num).let { flow ->
        flow.value = flow.value.add(bean)
      }
    }
  }

  fun updateSchedule(bean: ScheduleBean) {
    val num = Account.value?.num ?: return
    val localUpdateIsSuccess = ScheduleLocalAddRepository.updateAddSchedule(num, bean)
    if (localUpdateIsSuccess) {
      getScheduleBeansFlow(num).let { flow ->
        flow.value = flow.value.set(
          flow.value.indexOfFirst { it.id == bean.id },
          bean
        )
      }
      return
    }
    AppCoroutineScope.launch(Dispatchers.IO) {
      runCatching {
        Source.api(ScheduleApi::class)
          .updateSchedule(bean)
          .getOrThrow()
      }.onFailure {
        ScheduleLocalUpdateRepository.addUpdateSchedule(num, bean)
      }
      getScheduleBeansFlow(num).let { flow ->
        flow.value = flow.value.set(
          flow.value.indexOfFirst { it.id == bean.id },
          bean
        )
      }
    }
  }

  fun removeSchedule(id: Int) {
    val num = Account.value?.num ?: return
    val localRemoveIsSuccess = ScheduleLocalAddRepository.removeAddSchedule(num, id)
    if (localRemoveIsSuccess) {
      getScheduleBeansFlow(num).let { flow ->
        flow.value = flow.value.removeAt(
          flow.value.indexOfFirst { it.id == id }
        )
      }
      return
    }
    ScheduleLocalUpdateRepository.removeUpdateSchedule(num, id)
    AppCoroutineScope.launch(Dispatchers.IO) {
      runCatching {
        Source.api(ScheduleApi::class)
          .removeSchedule(id)
          .getOrThrow()
      }.onFailure {
        ScheduleLocalRemoveRepository.addRemoveSchedule(num, id)
      }
      getScheduleBeansFlow(num).let { flow ->
        flow.value = flow.value.removeAt(
          flow.value.indexOfFirst { it.id == id }
        )
      }
    }
  }
}