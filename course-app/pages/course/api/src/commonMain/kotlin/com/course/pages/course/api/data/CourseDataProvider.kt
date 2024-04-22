package com.course.pages.course.api.data

import androidx.compose.runtime.Stable
import com.course.pages.course.api.item.ICourseItem
import com.course.shared.time.Date
import kotlinx.coroutines.CoroutineScope

/**
 * .
 *
 * @author 985892345
 * 2024/3/7 23:14
 */

@Stable
open class CourseDataProvider : DataChangedListener {

  protected lateinit var coroutineScope: CoroutineScope
    private set

  private val data = mutableListOf<ICourseItem>()

  private val changedListeners = mutableListOf<DataChangedListener>()

  override fun add(item: ICourseItem?) {
    item ?: return
    data.add(item)
    changedListeners.forEach {
      it.add(item)
    }
  }

  override fun addAll(items: Collection<ICourseItem>?) {
    items ?: return
    if (items.isEmpty()) return
    data.addAll(items)
    changedListeners.forEach {
      it.addAll(items)
    }
  }

  override fun remove(item: ICourseItem?) {
    item ?: return
    data.remove(item)
    changedListeners.forEach {
      it.remove(item)
    }
  }

  override fun removeAll(items: Collection<ICourseItem>?) {
    items ?: return
    if (items.isEmpty()) return
    data.removeAll(items)
    changedListeners.forEach {
      it.removeAll(items)
    }
  }

  fun addDataChangedListener(listener: DataChangedListener, isNeedNowData: Boolean = true) {
    changedListeners.add(listener)
    if (isNeedNowData) {
      listener.addAll(data)
    }
  }

  fun removeDataChangedListener(listener: DataChangedListener) {
    changedListeners.remove(listener)
  }

  /**
   * 选中日期发生改变时回调
   */
  open fun onChangedClickDate(date: Date) {
  }

  /**
   * 在 [CourseDetail] 请求下一个学期数据时回调
   *
   * - 当前学期返回 [termIndex] 负的学期数，如果当前学期未知，则为 Int.MIN_VALUE，其他学期则回调 >=0 的学期数
   */
  open fun onRequestTerm(termIndex: Int) {
  }

  /**
   * 在 [CourseDetail] 请求下一个学期数据完成时回调
   *
   * - 当前学期返回负的学期数，其他学期则回调 >=0 的学期数
   */
  open fun onChangedTermIndex(termIndex: Int, startDate: Date) {
  }

  /**
   * 在课表组件加载时回调
   *
   * 如果课表页面被摧毁再重新进入时会重新回调，
   * 即使重新回调，学号不会发生改变，因为学号发生改变时，会重新创建 CourseDataProvider
   */
  open fun onComposeInit(coroutineScope: CoroutineScope) {
    this.coroutineScope = coroutineScope
  }

  /**
   * 在课表组件摧毁时回调
   */
  open fun onComposeDispose() {
  }
}

interface DataChangedListener {
  fun add(item: ICourseItem?)
  fun addAll(items: Collection<ICourseItem>?)
  fun remove(item: ICourseItem?)
  fun removeAll(items: Collection<ICourseItem>?)
}