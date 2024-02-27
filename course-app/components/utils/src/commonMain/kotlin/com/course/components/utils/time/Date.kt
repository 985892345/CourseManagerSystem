package com.course.components.utils.time

import androidx.compose.runtime.Stable
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.isoDayNumber
import kotlin.jvm.JvmInline
import kotlin.math.abs

/**
 * 使用 value class 压缩存储日期，可以用来代替 LocalDate
 *
 * 大部分计算借用 java 的 LocalDate
 * 同时添加 @Stable 标志设置为稳定类，Kotlin 的 LocalDate 不稳定
 *
 * @author 985892345
 * @date 2024/2/25 15:17
 */
@Stable
@JvmInline
value class Date(
  val time: Int,
) : Comparable<Date> {

  constructor(
    year: Int,
    month: Int,
    dayOfMonth: Int,
    noOverflow: Boolean = false, // 防止溢出，如果日溢出则改为该月最后一天，如果月溢出则年 + 1
  ) : this(getTime(year, month, dayOfMonth, noOverflow))

  val year: Int
    get() = time shr 9

  val month: Month
    get() = Month(monthNumber)

  val monthNumber: Int
    get() = time shr 5 and 0xF

  val dayOfMonth: Int
    get() = time and 0x1F

  val dayOfWeek: DayOfWeek
    get() = DayOfWeek(((toEpochDays() + 3) % 7).let { if (it < 0) it + 7 else it } + 1)

  val dayOfWeekOrdinal: Int
    get() = dayOfWeek.ordinal

  val dayOfWeekNumber: Int
    get() = dayOfWeek.isoDayNumber

  val lengthOfMonth: Int
    get() = lengthOfMonth(year, monthNumber)

  fun daysUntil(date: Date): Int {
    return date.toEpochDays() - toEpochDays()
  }

  fun isLeapYear(): Boolean {
    return isLeapYear(year)
  }

  fun toEpochDays(): Int {
    val y = year
    val m = monthNumber
    var total = 365 * y
    if (y >= 0) {
      total += (y + 3) / 4 - (y + 99) / 100 + (y + 399) / 400
    } else {
      total -= y / -4 - y / -100 + y / -400
    }
    total += (367 * m - 362) / 12
    total += dayOfMonth - 1
    if (m > 2) {
      total--
      if (!isLeapYear()) {
        total--
      }
    }
    return total - DAYS_0000_TO_1970
  }

  fun copy(
    year: Int = this.year,
    monthNumber: Int = this.monthNumber,
    dayOfMonth: Int = this.dayOfMonth,
    noOverflow: Boolean = false, // 防止溢出，如果日溢出则改为该月最后一天，如果月溢出则年 + 1
  ) : Date {
    return Date(year, monthNumber, dayOfMonth, noOverflow)
  }

  fun plusDays(daysToAdd: Int): Date {
    if (daysToAdd == 0) return this
    val dom = dayOfMonth + daysToAdd
    if (dom > 0) {
      if (dom <= 28) {
        return Date(year, monthNumber, dom)
      } else if (dom <= 59) { // 59th Jan is 28th Feb, 59th Feb is 31st Mar
        val monthLen = lengthOfMonth
        return if (dom <= monthLen) {
          Date(year, monthNumber, dom)
        } else if (monthNumber < 12) {
          Date(year, monthNumber + 1, dom - monthLen)
        } else {
          Date(year + 1, 1, dom - monthLen)
        }
      }
    }
    val mjDay = toEpochDays() + daysToAdd
    return ofEpochDay(mjDay)
  }

  fun plusWeeks(weeksToAdd: Int): Date {
    return plusDays(weeksToAdd * 7)
  }

  fun plusMonths(monthsToAdd: Int): Date {
    if (monthsToAdd == 0) return this
    return copy(monthNumber = monthNumber + monthsToAdd, noOverflow = true)
  }

  fun plusYears(yearsToAdd: Int): Date {
    if (yearsToAdd == 0) return this
    return copy(year = year + yearsToAdd, noOverflow = true)
  }

  fun minusDays(daysToMinus: Int): Date {
    return plusDays(-daysToMinus)
  }

  fun minusWeeks(weeksToMinus: Int): Date {
    return plusWeeks(-weeksToMinus)
  }

  fun minusMonths(monthsToMinus: Int): Date {
    return plusMonths(-monthsToMinus)
  }

  fun minusYears(yearsToMinus: Int): Date {
    return plusYears(-yearsToMinus)
  }

  override fun toString(): String {
    val yearValue = year
    val monthValue = monthNumber
    val dayValue = dayOfMonth
    val absYear = abs(yearValue)
    val buf = StringBuilder(10)
    if (absYear < 1000) {
      if (yearValue < 0) {
        buf.append(yearValue - 10000).deleteAt(1)
      } else {
        buf.append(yearValue + 10000).deleteAt(0)
      }
    } else {
      if (yearValue > 9999) {
        buf.append('+')
      }
      buf.append(yearValue)
    }
    return buf.append(if (monthValue < 10) "-0" else "-")
      .append(monthValue)
      .append(if (dayValue < 10) "-0" else "-")
      .append(dayValue)
      .toString()
  }

  companion object {

    private const val DAYS_PER_CYCLE = 146097

    /**
     * The number of days from year zero to year 1970.
     * There are five 400 year cycles from year zero to 2000.
     * There are 7 leap years from 1970 to 2000.
     */
    const val DAYS_0000_TO_1970 = DAYS_PER_CYCLE * 5 - (30 * 365 + 7)

    /**
     * @param noOverflow 防止溢出，如果日溢出则改为该月最后一天(负数则为1)，如果月溢出则年 + 1 或 -1
     */
    private fun getTime(
      year: Int,
      monthNumber: Int,
      dayOfMonth: Int,
      noOverflow: Boolean,
    ): Int {
      val y: Int
      val m: Int
      val d: Int
      if (noOverflow) {
        y = year + if (monthNumber > 0) (monthNumber - 1) / 12 else monthNumber / 12 - 1
        m = if (monthNumber > 0) (monthNumber - 1) % 12 + 1 else monthNumber % 12 + 12
        d = dayOfMonth.coerceIn(1, DateUtils.lengthOfMonth(y, m))
      } else {
        check(year > 0) { "year = $year, must > 0" }
        check(monthNumber in 1..12) { ", month = $monthNumber, must in 1..12" }
        val lengthOfMonth = DateUtils.lengthOfMonth(year, monthNumber)
        check(dayOfMonth in 1..lengthOfMonth) {
          "month = $monthNumber, dayOfMonth = $dayOfMonth, must in 1..$lengthOfMonth"
        }
        y = year
        m = monthNumber
        d = dayOfMonth
      }
      return (y shl 9) + (m shl 5) + d
    }

    fun isLeapYear(year: Int): Boolean {
      return year and 3 == 0 && (year % 100 != 0 || year % 400 == 0)
    }

    fun lengthOfMonth(year: Int, month: Int): Int {
      return when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
      }
    }

    fun lengthOfMonth(date: LocalDate): Int {
      return lengthOfMonth(date.year, date.dayOfMonth)
    }

    fun ofEpochDay(epochDay: Int): Date {
      var zeroDay = epochDay + DAYS_0000_TO_1970
      // find the march-based year
      zeroDay -= 60 // adjust to 0000-03-01 so leap day is at end of four year cycle

      var adjust = 0
      if (zeroDay < 0) {
        // adjust negative years to positive for calculation
        val adjustCycles = (zeroDay + 1) / DAYS_PER_CYCLE - 1
        adjust = adjustCycles * 400
        zeroDay += -adjustCycles * DAYS_PER_CYCLE
      }
      var yearEst = (400 * zeroDay + 591) / DAYS_PER_CYCLE
      var doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400)
      if (doyEst < 0) {
        // fix estimate
        yearEst--
        doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400)
      }
      yearEst += adjust // reset any negative year

      val marchDoy0 = doyEst

      // convert march-based values back to january-based
      val marchMonth0 = (marchDoy0 * 5 + 2) / 153
      val month = (marchMonth0 + 2) % 12 + 1
      val dom = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1
      yearEst += marchMonth0 / 10

      return Date(yearEst, month, dom)
    }
  }

  override fun compareTo(other: Date): Int {
    var cmp: Int = year - other.year
    if (cmp == 0) {
      cmp = monthNumber - other.monthNumber
      if (cmp == 0) {
        cmp = dayOfMonth - other.dayOfMonth
      }
    }
    return cmp
  }
}

fun LocalDate.toDate(): Date {
  return Date(year, monthNumber, dayOfMonth)
}