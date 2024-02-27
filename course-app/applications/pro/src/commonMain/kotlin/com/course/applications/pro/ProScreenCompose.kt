package com.course.applications.pro

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import com.course.components.base.theme.AppTheme
import com.course.components.utils.time.Date
import com.course.components.view.calendar.CalendarCompose
import com.course.components.view.calendar.state.rememberCalendarState
import com.course.pages.course.ui.CourseContentCompose
import com.course.pages.course.ui.pager.CoursePagerData
import com.course.pages.course.ui.vp.CourseSemesterVpData
import com.course.pages.course.ui.vp.CourseWeeksVpData
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/22 16:22
 */
@Composable
fun ProScreenCompose() {
  AppTheme(darkTheme = false) {
    CalendarCompose(
      modifier = Modifier.systemBarsPadding(),
      state = rememberCalendarState(
        startDate = Date(2024, 1, 1),
        endDate = Date(2024, 12, 31),
      )
    ) {
      CourseContentCompose(
        modifier = Modifier.weight(1F),
        semesterVpData = CourseSemesterVpData(
          persistentListOf(
            CourseWeeksVpData(
              firstDate = Date(2024, 2, 19),
              weeks = List(30) { CoursePagerData(mutableStateListOf()) }.toImmutableList()
            )
          )
        ),
      )
    }
  }
}

