package com.course.pages.course.api.controller

import androidx.compose.runtime.Stable
import com.course.pages.course.api.item.ICourseItemGroup
import com.course.shared.time.Date
import kotlinx.coroutines.CoroutineScope

/**
 * .
 *
 * @author 985892345
 * 2024/3/7 23:14
 */
@Stable
open class CourseController: ICourseItemGroup {

  protected lateinit var coroutineScope: CoroutineScope
    private set

  /**
   * 选中日期发生改变时回调
   */
  open fun onChangedClickDate(date: Date) {
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
