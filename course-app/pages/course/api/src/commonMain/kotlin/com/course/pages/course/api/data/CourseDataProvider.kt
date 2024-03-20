package com.course.pages.course.api.data

import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.course.components.utils.provider.Provider
import com.course.pages.course.api.item.ICourseItem
import com.course.shared.time.Date
import com.course.shared.time.MinuteTime
import com.course.shared.time.MinuteTimeDate
import kotlinx.coroutines.CoroutineScope

/**
 * .
 *
 * @author 985892345
 * 2024/3/7 23:14
 */

@Stable
open class CourseDataProvider {

  protected lateinit var coroutineScope: CoroutineScope
    private set

  private val listByDate = HashMap<MinuteTimeDate, SnapshotStateList<ICourseItem>>()

  @Stable
  fun getOrCreate(date: MinuteTimeDate): SnapshotStateList<ICourseItem> {
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
  fun add(item: ICourseItem) {
    getOrCreate(item.startTime).add(item)
  }

  fun addAll(items: Collection<ICourseItem>) {
    if (items.isEmpty()) return
    items.groupBy {
      MinuteTimeDate(it.startTime.date.weekBeginDate, TimelineDelayMinuteTime)
    }.forEach {
      listByDate.getOrPut(it.key) { SnapshotStateList() }.addAll(it.value)
    }
  }

  fun remove(item: ICourseItem) {
    getOrCreate(item.startTime).remove(item)
  }

  /**
   * 选中日期发送改变时回调
   */
  open fun onChangedClickDate(date: Date) {
  }

  open fun initProvider(coroutineScope: CoroutineScope) {
    this.coroutineScope = coroutineScope
  }

  companion object {
    // 每天的时间起点
    val TimelineDelayMinuteTime = MinuteTime(4, 0)
  }
}