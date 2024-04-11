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
   * 选中日期发送改变时回调
   */
  open fun onChangedClickDate(date: Date) {
  }

  open fun onComposeInit(coroutineScope: CoroutineScope) {
    this.coroutineScope = coroutineScope
  }
}

interface DataChangedListener {
  fun add(item: ICourseItem?)
  fun addAll(items: Collection<ICourseItem>?)
  fun remove(item: ICourseItem?)
  fun removeAll(items: Collection<ICourseItem>?)
}