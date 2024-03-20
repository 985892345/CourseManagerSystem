package com.course.components.utils.preferences

import com.russhwolf.settings.Settings

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/12 20:16
 */

val Settings = createSettings("defaultSettings")

expect fun createSettings(name: String): Settings