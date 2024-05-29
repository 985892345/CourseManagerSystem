package com.course.pages.team.ui.course

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.course.components.base.ui.toast.toast
import com.course.components.utils.debug.logg
import com.course.components.utils.provider.Provider
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.api.IScheduleService
import com.course.pages.schedule.api.item.edit.ScheduleColorData
import com.course.pages.team.ui.course.base.BottomSheetCourseController
import com.course.pages.team.ui.course.base.MemberCourseItemData
import com.course.shared.time.Date
import com.course.source.app.schedule.ScheduleBean
import com.course.source.app.team.TeamApi
import com.course.source.app.team.TeamBean
import com.course.source.app.team.TeamMember
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.*

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 14:10
 */
class TeamCourseBottomSheet(
  val teamBean: TeamBean,
  members: List<TeamMember>,
) : BottomSheetCourseController(
  members = members.map { MemberCourseItemData.Member(name = it.name, num = it.num, type = it.type) },
  excludeCourseNum = emptySet(),
  controllers = persistentListOf(),
) {

  private val scheduleBeansMap = mutableMapOf<Int, ScheduleBean>()

  private val scheduleCourseItemGroup = Provider.impl(IScheduleService::class)
    .getScheduleCourseItemGroup(
      colorData = ScheduleColorData(Color(0xFF6D4C41), Color(0xFFD7CCC8)),
      onCreate = { createSchedule(it) },
      onUpdate = { updateSchedule(it) },
      onDelete = { deleteSchedule(it) },
    )

  @Composable
  override fun Content(weekBeginDate: Date, timeline: CourseTimeline, scrollState: ScrollState) {
    super.Content(weekBeginDate, timeline, scrollState)
    scheduleCourseItemGroup.Content(weekBeginDate, timeline, scrollState)
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
            description = it.content,
            startTime = it.startTime,
            minuteDuration = it.minuteDuration,
            repeat = it.repeat,
            textColor = it.textColor,
            backgroundColor = it.backgroundColor,
          )
        }
        scheduleCourseItemGroup.resetData(scheduleBeansMap.values)
      }
    }
  }

  private suspend fun createSchedule(bean: ScheduleBean) {
    withContext(Dispatchers.IO) {
      runCatching {
        Source.api(TeamApi::class)
          .createTeamSchedule(
            teamId = teamBean.teamId,
            title = bean.title,
            content = bean.description,
            startTime = bean.startTime,
            minuteDuration = bean.minuteDuration,
            repeat = bean.repeat,
            textColor = bean.textColor,
            backgroundColor = bean.backgroundColor,
          ).getOrThrow()
      }.tryThrowCancellationException().onSuccess {
        scheduleBeansMap[it] = bean.copy(id = it)
        scheduleCourseItemGroup.resetData(scheduleBeansMap.values)
      }.onFailure {
        logg(it.stackTraceToString())
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
            content = bean.description,
            startTime = bean.startTime,
            minuteDuration = bean.minuteDuration,
            repeat = bean.repeat,
            textColor = bean.textColor,
            backgroundColor = bean.backgroundColor,
          ).getOrThrow()
      }.tryThrowCancellationException().onSuccess {
        scheduleBeansMap[bean.id] = bean
        scheduleCourseItemGroup.resetData(scheduleBeansMap.values)
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
        scheduleCourseItemGroup.resetData(scheduleBeansMap.values)
      }.onFailure {
        toast("网络异常")
      }
    }
  }
}