package com.course.pages.team.ui.course

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import com.course.components.base.ui.toast.toast
import com.course.components.utils.provider.Provider
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.pages.course.api.controller.CourseController
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.api.IScheduleService
import com.course.shared.time.Date
import com.course.source.app.schedule.ScheduleBean
import com.course.source.app.team.TeamApi
import com.course.source.app.team.TeamBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * .
 *
 * @author 985892345
 * 2024/5/10 13:28
 */
class MemberCourseScheduleController(
  val teamBean: TeamBean
) : CourseController() {

  private val scheduleBeansMap = mutableMapOf<Int, ScheduleBean>()

  private val scheduleController = Provider.impl(IScheduleService::class)
    .getScheduleCourseItemGroup(
      onCreate = { createSchedule(it) },
      onUpdate = { updateSchedule(it) },
      onDelete = { deleteSchedule(it) },
    )

  @Composable
  override fun Content(weekBeginDate: Date, timeline: CourseTimeline, scrollState: ScrollState) {
    super.Content(weekBeginDate, timeline, scrollState)
    scheduleController.Content(weekBeginDate, timeline, scrollState)
  }

  override fun onComposeInit(coroutineScope: CoroutineScope) {
    super.onComposeInit(coroutineScope)
    coroutineScope.launch(Dispatchers.IO) {
      runCatching {
        Source.api(TeamApi::class)
          .getTeamSchedule(teamBean.teamId)
          .getOrThrow()
      }.tryThrowCancellationException().onSuccess { beans ->
        scheduleBeansMap.clear()
        beans.forEach {
          scheduleBeansMap[it.id] = ScheduleBean(
            id = it.id,
            title = it.title,
            description = it.description,
            startTime = it.startTime,
            minuteDuration = it.minuteDuration,
            repeat = it.repeat,
          )
        }
        scheduleController.resetData(scheduleBeansMap.values)
      }
    }
  }

  private suspend fun createSchedule(bean: ScheduleBean) {
    withContext(Dispatchers.IO) {
      runCatching {
        Source.api(TeamApi::class)
          .createTeamSchedule(
            title = bean.title,
            description = bean.description,
            startTime = bean.startTime,
            minuteDuration = bean.minuteDuration,
            repeat = bean.repeat,
            teamId = teamBean.teamId,
          ).getOrThrow()
      }.tryThrowCancellationException().onSuccess {
        scheduleBeansMap[it] = bean.copy(id = it)
        scheduleController.resetData(scheduleBeansMap.values)
      }.onFailure {
        toast("网络异常")
      }
    }
  }

  private suspend fun updateSchedule(bean: ScheduleBean) {
    withContext(Dispatchers.IO) {
      runCatching {
        Source.api(TeamApi::class)
          .updateTeamSchedule(
            id = bean.id,
            title = bean.title,
            description = bean.description,
            startTime = bean.startTime,
            minuteDuration = bean.minuteDuration,
            repeat = bean.repeat,
          ).getOrThrow()
      }.tryThrowCancellationException().onSuccess {
        scheduleBeansMap[bean.id] = bean
        scheduleController.resetData(scheduleBeansMap.values)
      }.onFailure {
        toast("网络异常")
      }
    }
  }

  private suspend fun deleteSchedule(bean: ScheduleBean) {
    withContext(Dispatchers.IO) {
      runCatching {
        Source.api(TeamApi::class)
          .deleteTeamSchedule(bean.id)
          .getOrThrow()
      }.tryThrowCancellationException().onSuccess {
        scheduleBeansMap.remove(bean.id)
        scheduleController.resetData(scheduleBeansMap.values)
      }.onFailure {
        toast("网络异常")
      }
    }
  }
}