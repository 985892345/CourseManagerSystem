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
