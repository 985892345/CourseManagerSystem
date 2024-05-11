package com.course.pages.course.api.item.lesson

import androidx.compose.runtime.Composable

/**
 * .
 *
 * @author 985892345
 * 2024/5/6 13:17
 */
interface ILessonPopBottom {

  val priority: Int

  val visibility: Boolean
    get() = true

  @Composable
  fun Content(data: LessonItemData, dismiss: () -> Unit)
}