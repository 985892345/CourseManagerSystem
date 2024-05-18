package com.course.server.utils

import com.course.shared.time.Date

/**
 * .
 *
 * @author 985892345
 * @date 2024/2/29 19:12
 */
object SchoolCalendar {

  val beginDate = listOf(
    Date(2010, 9, 4), Date(2011, 2, 21), // 10-11
    Date(2011, 9, 5), Date(2012, 2, 20), // 11-12
    Date(2012, 9, 10), Date(2013, 2, 25), // 12-13
    Date(2013, 9, 9), Date(2014, 2, 24), // 13-14
    Date(2014, 9, 8), Date(2015, 3, 2), // 14-15
    Date(2015, 9, 7), Date(2016, 2, 29), // 15-16
    Date(2016, 9, 5), Date(2017, 2, 27), // 16-17
    Date(2017, 9, 11), Date(2018, 3, 5), // 17-18
    Date(2018, 9, 10), Date(2019, 2, 25), // 18-19
    Date(2019, 9, 2), Date(2020, 2, 17), // 19-20
    Date(2020, 9, 7), Date(2021, 3, 1), // 20-21
    Date(2021, 9, 6), Date(2022, 2, 28), // 21-22
    Date(2022, 9, 5), Date(2023, 2, 20), // 22-23
    Date(2023, 9, 4), Date(2024, 2, 26), // 23-24
  )

  fun getBeginDate(date: Date = Date.now()): Date {
    return beginDate[getBeginDateIndex(date)]
  }

  fun getBeginDateIndex(date: Date): Int {
    val index = (date.year - 2010) * 2 - 1
    if (index > beginDate.lastIndex) {
      return beginDate.lastIndex
    }
    val begin = beginDate[index]
    return if (date < begin) {
      index - 1
    } else {
      if (index + 1 > beginDate.lastIndex) return index
      val nextBegin = beginDate[index + 1]
      if (date >= nextBegin) {
        index + 1
      } else {
        index
      }
    }
  }

  fun getWeekNumber(date: Date = Date.now()): Int {
    return beginDate[getBeginDateIndex(date)].daysUntil(date) / 7 + 1
  }

  /**
   * 返回第几周，小于 1 时返回 null，大于 99 时返回数字周数，其余使用中文数字表示周数
   */
  fun getWeekStr(date: Date = Date.now()): String? {
    val number = getWeekNumber(date)
    if (number < 1) return null
    if (number >= 100) return "第${number}周"
    val a = when (number / 10) {
      1 -> "十"
      2 -> "二十"
      3 -> "三十"
      4 -> "四十"
      5 -> "五十"
      6 -> "六十"
      7 -> "七十"
      8 -> "八十"
      9 -> "九十"
      else -> ""
    }
    val b = when (number % 10) {
      1 -> "一"
      2 -> "二"
      3 -> "三"
      4 -> "四"
      5 -> "五"
      6 -> "六"
      7 -> "七"
      8 -> "八"
      9 -> "九"
      else -> ""
    }
    return "第${a}${b}周"
  }

  fun getTermNumber(startYear: Int, date: Date = Date.now()): Int {
    val startIndex = (startYear - 2010) * 2
    val nowIndex = getBeginDateIndex(date)
    return nowIndex - startIndex
  }

  fun getTermStr(startYear: Int, date: Date = Date.now()): String? {
    return when (getTermNumber(startYear, date)) {
      0 -> "大一上"
      1 -> "大一下"
      2 -> "大二上"
      3 -> "大二下"
      4 -> "大三上"
      5 -> "大三下"
      6 -> "大四上"
      7 -> "大四下"
      8 -> "大五上"
      9 -> "大五下"
      10 -> "大六上"
      11 -> "大六下"
      12 -> "大七上"
      13 -> "大七下"
      14 -> "大八上"
      15 -> "大八下"
      else -> null
    }
  }
}