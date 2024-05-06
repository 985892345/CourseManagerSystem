package com.course.source.app.schedule

import com.course.shared.time.Date
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/1 11:00
 */
@Serializable
sealed interface ScheduleRepeat {

  val frequency: Int
  val count: Int

  /**
   * @param repeatCurrentIndex 当前重复的次数，以 0 开始
   */
  fun getDate(beginDate: Date, repeatCurrentIndex: Int): Date

  companion object {
    val Once = Day(1, 1)
  }

  @Serializable
  data class Day(
    override val frequency: Int,
    override val count: Int
  ) : ScheduleRepeat {
    override fun getDate(beginDate: Date, repeatCurrentIndex: Int): Date {
      return beginDate.plusDays(frequency * repeatCurrentIndex)
    }
  }
  @Serializable
  data class Week(
    override val frequency: Int,
    override val count: Int
  ) : ScheduleRepeat {
    override fun getDate(beginDate: Date, repeatCurrentIndex: Int): Date {
      return beginDate.plusWeeks(frequency * repeatCurrentIndex)
    }
  }
  @Serializable
  data class Month(
    override val frequency: Int,
    override val count: Int
  ) : ScheduleRepeat {
    override fun getDate(beginDate: Date, repeatCurrentIndex: Int): Date {
      return beginDate.plusMonths(frequency * repeatCurrentIndex)
    }
  }
}
