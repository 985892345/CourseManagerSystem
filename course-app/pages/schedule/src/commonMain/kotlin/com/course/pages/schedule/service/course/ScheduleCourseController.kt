package com.course.pages.schedule.service.course

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import com.course.pages.course.api.IMainCourseController
import com.course.pages.course.api.controller.CourseController
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.api.item.showAddScheduleBottomSheet
import com.course.pages.schedule.model.ScheduleRepository
import com.course.shared.time.Date
import com.course.source.app.account.AccountBean
import com.course.source.app.account.AccountType
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * 2024/4/22 15:03
 */
@ImplProvider(clazz = IMainCourseController::class, name = "ScheduleMainCourseDataProvider")
class ScheduleMainCourseController : IMainCourseController {
  override fun createCourseController(account: AccountBean?): List<CourseController> {
    return when (account?.type) {
      AccountType.Student, AccountType.Teacher -> listOf(ScheduleCourseController(account))
      null -> emptyList()
    }
  }
}

class ScheduleCourseController(
  val account: AccountBean
) : CourseController() {

  private val scheduleCourseItemGroup = ScheduleCourseItemGroup(
    onCreate = {
      ScheduleRepository.addSchedule(
        title = it.title,
        description = it.description,
        startTime = it.startTime,
        minuteDuration = it.minuteDuration,
        repeat = it.repeat,
        textColor = it.textColor,
        backgroundColor = it.backgroundColor,
      )
    },
    onUpdate = {
      ScheduleRepository.updateSchedule(it)
    },
    onDelete = {
      ScheduleRepository.removeSchedule(it.id)
    },
    onClick = { item, repeatCurrent, weekBeginDate, timeline ->
      showAddScheduleBottomSheet(item, repeatCurrent, weekBeginDate, timeline)
    },
  )

  @Composable
  override fun Content(
    weekBeginDate: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState,
  ) {
    super.Content(weekBeginDate, timeline, scrollState)
    scheduleCourseItemGroup.Content(weekBeginDate, timeline, scrollState)
  }

  override fun onComposeInit(coroutineScope: CoroutineScope) {
    super.onComposeInit(coroutineScope)
    coroutineScope.launch {
      ScheduleRepository.observeScheduleBean().collect { list ->
        scheduleCourseItemGroup.resetData(list)
      }
    }
  }
}