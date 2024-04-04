package com.course.shared.time

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * .
 *
 * @author 985892345
 * 2024/3/9 15:37
 */
@JvmInline
@Serializable
value class MinuteTimeDate(val value: Int) : Comparable<MinuteTimeDate> {

  constructor(date: Date, time: MinuteTime) : this((date.value shl 10) or time.value)

  constructor(date: Date, hour: Int, minute: Int) : this(date, MinuteTime(hour, minute))

  constructor(year: Int, month: Int, dayOfMonth: Int, time: MinuteTime) : this(
    Date(year, month, dayOfMonth), time
  )

  constructor(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int) : this(
    Date(year, month, dayOfMonth), MinuteTime(hour, minute)
  )

  val date: Date
    get() = Date(value ushr 10) // 最多能保存到公元 8191 年，应该够了

  val time: MinuteTime
    get() = MinuteTime(value and 0x3FF)

  fun minutesUntil(other: MinuteTimeDate): Int {
    return date.daysUntil(other.date) * 24 * 60 + time.minutesUntil(other.time)
  }

  fun plusMinutes(minutes: Int): MinuteTimeDate {
    val hourDiff = (time.minute + minutes) / 60
    val dateDiff = (time.hour + hourDiff) / 24
    return MinuteTimeDate(
      date.plusDays(dateDiff),
      (time.hour + hourDiff) % 24,
      (time.minute + minutes) % 60
    )
  }

  fun minusMinutes(minutes: Int): MinuteTimeDate {
    return plusMinutes(-minutes)
  }

  fun plusDays(days: Int): MinuteTimeDate {
    return MinuteTimeDate(date.plusDays(days), time)
  }

  fun minusDays(days: Int): MinuteTimeDate {
    return plusDays(-days)
  }

  fun plusWeeks(weeks: Int): MinuteTimeDate {
    return plusDays(weeks * 7)
  }

  fun minusWeeks(weeks: Int): MinuteTimeDate {
    return plusDays(-weeks * 7)
  }

  fun plusMonths(months: Int): MinuteTimeDate {
    return MinuteTimeDate(date.plusMonths(months), time)
  }

  fun minusMonths(months: Int): MinuteTimeDate {
    return plusMonths(-months)
  }

  fun plusYears(years: Int): MinuteTimeDate {
    return MinuteTimeDate(date.plusYears(years), time)
  }

  fun minusYears(years: Int): MinuteTimeDate {
    return plusYears(-years)
  }

  override fun compareTo(other: MinuteTimeDate): Int {
    return value.compareTo(other.value)
  }
}

fun LocalDateTime.toMinuteTimeDate(): MinuteTimeDate {
  return MinuteTimeDate(
    year = this.year,
    month = this.monthNumber,
    dayOfMonth = this.dayOfMonth,
    hour = this.hour,
    minute = this.minute,
  )
}