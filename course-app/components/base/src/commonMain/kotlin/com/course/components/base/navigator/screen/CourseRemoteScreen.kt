package com.course.components.base.navigator.screen

import com.course.components.utils.navigator.RemoteScreen
import com.course.components.utils.serializable.ObjectSerializable
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/3/7 13:57
 */
@Serializable
@ObjectSerializable
data class CourseRemoteScreen(
  val stuNum: String
) : RemoteScreen()