package com.course.pages.course.ui.pager

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.course.pages.course.api.data.CourseDataProvider
import com.course.pages.course.api.data.DataChangedListener
import com.course.pages.course.api.item.ICourseItem
import com.course.pages.course.api.timeline.CourseTimeline
import com.course.shared.time.Date
import com.course.shared.time.MinuteTimeDate

/**
 * .
 *
 * @author 985892345
 * 2024/4/10 18:21
 */
class WeekItemsProvider(
  val timeline: CourseTimeline
) {

  private var dataProvider: List<CourseDataProvider>? = null

  private val weekItemsMap = HashMap<MinuteTimeDate, SnapshotStateList<ICourseItem>>()

  private val listener = object : DataChangedListener {
    override fun add(item: ICourseItem?) {
      item ?: return
      getWeekItems(item.startTime).add(item)
    }

    override fun addAll(items: Collection<ICourseItem>?) {
      if (items.isNullOrEmpty()) return
      // SnapshotStateList 每次修改都会生成新的 List，所以这里分组后才添加
      items.groupBy {
        getWeekItemsBeginDate(it.startTime)
      }.forEach {
        weekItemsMap.getOrPut(it.key) { SnapshotStateList() }.addAll(it.value)
      }
    }

    override fun remove(item: ICourseItem?) {
      item ?: return
      getWeekItems(item.startTime).remove(item)
    }

    override fun removeAll(items: Collection<ICourseItem>?) {
      if (items.isNullOrEmpty()) return
      // SnapshotStateList 每次修改都会生成新的 List，所以这里分组后才移除
      items.groupBy {
        getWeekItemsBeginDate(it.startTime)
      }.forEach {
        weekItemsMap.getOrPut(it.key) { SnapshotStateList() }.removeAll(it.value)
      }
    }
  }

  fun init(
    dataProvider: List<CourseDataProvider>,
  ) {
    if (dataProvider != this.dataProvider) {
      this.dataProvider?.forEach {
        it.removeDataChangedListener(listener)
      }
      weekItemsMap.forEach { it.value.clear() }
      this.dataProvider = dataProvider
      dataProvider.forEach {
        it.addDataChangedListener(listener)
      }
    }
  }

  fun getWeekItems(date: MinuteTimeDate): SnapshotStateList<ICourseItem> {
    return weekItemsMap.getOrPut(getWeekItemsBeginDate(date)) {
      SnapshotStateList()
    }
  }

  private fun getWeekItemsBeginDate(date: MinuteTimeDate): MinuteTimeDate {
    val begin = MinuteTimeDate(date.date.weekBeginDate, timeline.delayMinuteTime)
    return if (date >= begin) {
      begin
    } else {
      begin.minusWeeks(1)
    }
  }

  fun destroy() {
    dataProvider?.forEach {
      it.removeDataChangedListener(listener)
    }
  }

  companion object {
    /**
     * 得到 item 应该放置在哪个日期列上
     */
    fun getItemWhichDate(item: ICourseItem, timeline: CourseTimeline): Date {
      return if (item.startTime.time >= timeline.delayMinuteTime) {
        item.startTime.date
      } else {
        item.startTime.date.minusDays(1)
      }
    }
  }
}