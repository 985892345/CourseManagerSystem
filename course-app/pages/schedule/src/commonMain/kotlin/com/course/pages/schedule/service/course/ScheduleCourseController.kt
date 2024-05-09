package com.course.pages.schedule.service.course

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.util.fastForEach
import com.course.pages.course.api.IMainCourseDataProvider
import com.course.pages.course.api.controller.CourseController
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.pages.schedule.model.ScheduleRepository
import com.course.pages.schedule.ui.item.ScheduleItemGroup
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
@ImplProvider(clazz = IMainCourseDataProvider::class, name = "ScheduleMainCourseDataProvider")
class ScheduleMainCourseDataProvider : IMainCourseDataProvider {
  override fun createCourseDataProviders(account: AccountBean?): List<CourseController> {
    return when (account?.type) {
      AccountType.Student -> listOf(ScheduleCourseController(account))
      AccountType.Teacher -> emptyList()
      null -> emptyList()
    }
  }
}

class ScheduleCourseController(
  val account: AccountBean
) : CourseController() {

  private val scheduleItemGroups = mutableStateOf(mutableMapOf<Int, ScheduleItemGroup>())

  private val scheduleCourseItemGroup = ScheduleCourseItemGroup(scheduleItemGroups)
  private val placeholderScheduleCourseItemGroup = PlaceholderScheduleCourseItemGroup()

  @Composable
  override fun Content(
    weekBeginDate: Date,
    timeline: CourseTimeline,
    scrollState: ScrollState,
  ) {
    super.Content(weekBeginDate, timeline, scrollState)
    placeholderScheduleCourseItemGroup.Content(weekBeginDate, timeline, scrollState)
    scheduleCourseItemGroup.Content(weekBeginDate, timeline, scrollState)
  }

  override fun onComposeInit(coroutineScope: CoroutineScope) {
    super.onComposeInit(coroutineScope)
    coroutineScope.launch {
      ScheduleRepository.observeScheduleBean().collect { list ->
        val oldMap = scheduleItemGroups.value
        val newMap = mutableMapOf<Int, ScheduleItemGroup>()
        list.fastForEach {
          val oldItemGroup = oldMap[it.id]
          if (oldItemGroup != null) {
            oldItemGroup.changeBean(it)
            newMap[it.id] = oldItemGroup
          } else {
            newMap[it.id] = ScheduleItemGroup(it)
          }
        }
        scheduleItemGroups.value = newMap
      }
    }
  }
}