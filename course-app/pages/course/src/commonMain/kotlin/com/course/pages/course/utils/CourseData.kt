package com.course.pages.course.utils

import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.course.components.utils.time.MinuteTimeDate
import com.course.pages.course.ui.item.ICourseItemBean
import com.course.pages.course.ui.pager.scroll.timeline.TimelineDelayMinuteTime

/**
 * .
 *
 * @author 985892345
 * 2024/3/7 23:14
 */
@Stable
class CourseData {

  private val listByDate = HashMap<MinuteTimeDate, SnapshotStateList<ICourseItemBean>>()

  @Stable
  fun getOrCreate(date: MinuteTimeDate): SnapshotStateList<ICourseItemBean> {
    val begin = MinuteTimeDate(date.date.weekBeginDate, TimelineDelayMinuteTime)
    return if (date >= begin) {
      listByDate.getOrPut(begin) {
        SnapshotStateList()
      }
    } else {
      listByDate.getOrPut(begin.minusWeeks(1)) {
        SnapshotStateList()
      }
    }
  }

  /**
   * 如果有大量数据建议使用 [addAll]，这里每次 add 都会复制一遍数组
   */
  fun add(item: ICourseItemBean) {
    val begin = MinuteTimeDate(item.startTime.date.weekBeginDate, TimelineDelayMinuteTime)
    if (item.startTime >= begin) {
      getOrCreate(item.startTime).add(item)
    } else {
      val endTime = item.startTime.plusMinutes(item.minutePeriod)
      if (endTime > begin) {
        // 开始和结束时间刚好跨越了周一的开始时间，则该 item 会存在两个周的页面中
        getOrCreate(item.startTime).add(item)
        getOrCreate(endTime).add(item)
      } else {
        getOrCreate(item.startTime).add(item)
      }
    }
  }

  fun addAll(items: Collection<ICourseItemBean>) {
    if (items.isEmpty()) return
    val map = HashMap<MinuteTimeDate, MutableList<ICourseItemBean>>()
    items.forEach {
      val begin = MinuteTimeDate(it.startTime.date.weekBeginDate, TimelineDelayMinuteTime)
      if (it.startTime >= begin) {
        map.getOrPut(begin) { ArrayList() }.add(it)
      } else {
        val endTime = it.startTime.plusMinutes(it.minutePeriod)
        if (endTime > begin) {
          map.getOrPut(begin) { ArrayList() }.add(it)
          map.getOrPut(begin.minusWeeks(1)) { ArrayList() }.add(it)
        } else {
          map.getOrPut(begin) { ArrayList() }.add(it)
        }
      }
    }
    map.forEach {
      listByDate.getOrPut(it.key) { SnapshotStateList() }.addAll(it.value)
    }
  }

  fun remove(item: ICourseItemBean) {
    val begin = MinuteTimeDate(item.startTime.date.weekBeginDate, TimelineDelayMinuteTime)
    if (item.startTime >= begin) {
      getOrCreate(item.startTime).remove(item)
    } else {
      val endTime = item.startTime.plusMinutes(item.minutePeriod)
      if (endTime > begin) {
        getOrCreate(item.startTime).remove(item)
        getOrCreate(endTime).remove(item)
      } else {
        getOrCreate(item.startTime).remove(item)
      }
    }
  }
}