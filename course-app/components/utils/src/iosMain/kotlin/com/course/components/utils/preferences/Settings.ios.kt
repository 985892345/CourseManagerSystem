package com.course.components.utils.preferences

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/12 20:16
 */

actual fun createSettings(name: String): Settings {
  return NSUserDefaultsSettings.Factory().create(name)
}