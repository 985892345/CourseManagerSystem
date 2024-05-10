package com.course.pages.schedule.api.item

import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.schedule.ScheduleRepeat
import kotlinx.coroutines.CoroutineScope

/**
 * .
 *
 * @author 985892345
 * 2024/4/30 16:35
 */
interface BottomSheetScheduleItem {

  val id: Int

  val updatable: Boolean
    get() = true

  val deletable: Boolean
    get() = true

  val title: MutableState<String>

  val description: MutableState<String>

  val repeat: ScheduleRepeat

  val startTime: MinuteTimeDate

  val minuteDuration: Int

  var textColor: Color
  var backgroundColor: Color

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

  fun success(coroutineScope: CoroutineScope, dismiss: () -> Unit)

  fun delete(coroutineScope: CoroutineScope, dismiss: () -> Unit)

  fun dismissOnBackPress(dismiss: () -> Unit): Boolean

  fun dismissOnClickOutside(dismiss: () -> Unit): Boolean
}