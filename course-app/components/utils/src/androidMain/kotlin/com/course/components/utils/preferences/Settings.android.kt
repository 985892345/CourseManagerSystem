package com.course.components.utils.preferences

import com.g985892345.android.utils.context.appContext
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/12 20:16
 */

actual fun createSettings(name: String): Settings {
  return SharedPreferencesSettings.Factory(appContext).create(name)
}