package com.course.shared.time

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * .
 *
 * @author 985892345
 * 2024/3/9 15:30
 */
@JvmInline
@Serializable
value class MinuteTime(val value: Int) : Comparable<MinuteTime> {

  constructor(hour: Int, minute: Int) : this(
    (checkHour(hour) shl 6) or checkMinute(minute)
  )

  val hour: Int
    get() = value ushr 6 // 最多占 5 位，2^5 - 1

  val minute: Int
    get() = value and 0x3F // 最多占 6 位，2^6 - 1

  fun minutesUntil(time: MinuteTime, cyclic: Boolean = false): Int {
    if (this > time && cyclic) {
      return (24 - hour) * 60 - minute + time.hour * 60 + time.minute
    }
    return (time.hour - hour) * 60 + (time.minute - minute)
  }

  fun plusMinutes(minutes: Int): MinuteTime {
    val hourDiff = (minute + minutes) / 60
    return MinuteTime(
      (hour + hourDiff) % 24,
      (minute + minutes) % 60,
    )
  }

  fun minusMinutes(minutes: Int): MinuteTime {
    return plusMinutes(-minutes)
  }

  fun plusHours(hours: Int): MinuteTime {
    return MinuteTime(
      (hour + hours) % 24,
      minute
    )
  }

  fun minusHours(hours: Int): MinuteTime {
    return plusHours(-hours)
  }

  override fun compareTo(other: MinuteTime): Int {
    return value.compareTo(other.value)
  }

  override fun toString(): String {
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
  }

  companion object {
    fun checkHour(hour: Int): Int {
      if (hour in 0..23) return hour
      throw IllegalArgumentException("hour must in 0..23")
    }

    fun checkMinute(minute: Int): Int {
      if (minute in 0..59) return minute
      throw IllegalArgumentException("minute must in 0..59")
    }
  }
}