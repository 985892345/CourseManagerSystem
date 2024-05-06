package com.course.source.app.local.course

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.util.fastForEach
import com.course.components.utils.result.tryThrowCancellationException
import com.course.pages.course.api.controller.CourseController
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.shared.time.Date
import com.course.source.app.account.AccountBean
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * 2024/4/28 15:17
 */
class SourceCourseController(
  val account: AccountBean?
) : CourseController() {

  private var oldTimeline: CourseTimeline? = null
  private var oldData: List<SourceCourseItemData> = emptyList()
  private var dataMapState: MutableState<Map<Date, List<SourceCourseItemData>>> =
    mutableStateOf(emptyMap())

  private var requestJob: Job? = null

  override fun onChangedTermIndex(termIndex: Int, startDate: Date) {
    super.onChangedTermIndex(termIndex, startDate)
    if (termIndex < 0 && requestJob == null) {
      // 只请求当前学期的数据
      requestJob = coroutineScope.launch {
        runCatching {
          CourseApiImpl.courseRequestGroup.request(
            false,
            true,
            account?.num ?: "",
            startDate.toString(),
          )
        }.onFailure {
          requestJob = null
        }.tryThrowCancellationException().onSuccess { map ->
          resetData(
            map.flatMap { entry ->
              entry.value.map { it.copy(id = "${entry.key.key}-${it.id}") }
            }
          )
        }
      }
    }
  }

  private fun resetData(data: List<SourceCourseItemData>) {
    oldData = data
    val timeline = oldTimeline
    if (timeline != null) {
      dataMapState.value = data.groupBy { timeline.getItemWhichDate(it.startTime).weekBeginDate }
    }
  }

  @Composable
  override fun Content(weekBeginDate: Date, timeline: CourseTimeline, scrollState: ScrollState) {
    super.Content(weekBeginDate, timeline, scrollState)
    if (oldTimeline != timeline) {
      oldTimeline = timeline
      resetData(oldData)
    }
    dataMapState.value[weekBeginDate]?.fastForEach {
      with(it) { SourceContent(weekBeginDate, timeline) }
    }
  }
}