package com.course.pages.schedule.ui.item

import androidx.compose.runtime.MutableState
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.schedule.ScheduleRepeat

/**
 * .
 *
 * @author 985892345
 * 2024/4/30 16:35
 */
interface BottomSheetScheduleItem {

  val title: MutableState<String>

  val description: MutableState<String>

  val repeat: ScheduleRepeat

  val startTime: MinuteTimeDate

  val minuteDuration: Int

  fun checkBeginFinalTime(
    startTime: MinuteTimeDate = this.startTime,
    minuteDuration: Int = this.minuteDuration,
  ): Boolean

  suspend fun changeBeginFinalTime(
    startTime: MinuteTimeDate = this.startTime,
    minuteDuration: Int = this.minuteDuration,
  )

  fun changeRepeat(
    repeat: ScheduleRepeat,
  )

  fun success(dismiss: () -> Unit)

  fun delete(dismiss: () -> Unit)

  fun dismissOnBackPress(dismiss: () -> Unit): Boolean

  fun dismissOnClickOutside(dismiss: () -> Unit): Boolean
}