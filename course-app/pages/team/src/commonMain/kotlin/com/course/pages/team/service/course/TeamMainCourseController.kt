package com.course.pages.team.service.course

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.course.components.utils.provider.Provider
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.pages.course.api.IMainCourseController
import com.course.pages.course.api.controller.CourseController
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.api.IScheduleService
import com.course.pages.schedule.api.item.showAddScheduleBottomSheet
import com.course.shared.time.Date
import com.course.source.app.account.AccountBean
import com.course.source.app.account.AccountType
import com.course.source.app.schedule.ScheduleBean
import com.course.source.app.team.TeamApi
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * 2024/5/10 15:05
 */
@ImplProvider(clazz = IMainCourseController::class, name = "MainCourseController")
class TeamMainCourseController : IMainCourseController {
  override fun createCourseController(account: AccountBean?): List<CourseController> {
    return when (account?.type) {
      AccountType.Student, AccountType.Teacher -> listOf(TeamCourseController(account))
      null -> emptyList()
    }
  }
}

private class TeamCourseController(
  val account: AccountBean,
) : CourseController() {

  private val teamNameByScheduleId = mutableMapOf<Int, String>()

  private val scheduleItemGroup = Provider.impl(IScheduleService::class)
    .getScheduleCourseItemGroup(
      onClick = { item, repeatCurrent, weekBeginDate, timeline ->
        showAddScheduleBottomSheet(item, repeatCurrent, weekBeginDate, timeline) {
          Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(
              modifier = Modifier.align(Alignment.BottomEnd),
              text = "来自${teamNameByScheduleId[item.id]}",
              fontSize = 12.sp,
              color = Color.LightGray,
            )
          }
        }
      }
    )

  override fun onComposeInit(coroutineScope: CoroutineScope) {
    super.onComposeInit(coroutineScope)
    coroutineScope.launch(Dispatchers.IO) {
      runCatching {
        Source.api(TeamApi::class)
          .getTeamAllSchedule()
          .getOrThrow()
      }.tryThrowCancellationException().onSuccess { beans ->
        teamNameByScheduleId.clear()
        scheduleItemGroup.resetData(beans.map {
          teamNameByScheduleId[it.id] = it.teamName
          ScheduleBean(
            id = it.id,
            title = it.title,
            description = it.content,
            startTime = it.startTime,
            minuteDuration = it.minuteDuration,
            repeat = it.repeat,
            textColor = it.textColor,
            backgroundColor = it.backgroundColor,
          )
        })
      }
    }
  }

  @Composable
  override fun Content(weekBeginDate: Date, timeline: CourseTimeline, scrollState: ScrollState) {
    super.Content(weekBeginDate, timeline, scrollState)
    scheduleItemGroup.Content(weekBeginDate, timeline, scrollState)
  }
}