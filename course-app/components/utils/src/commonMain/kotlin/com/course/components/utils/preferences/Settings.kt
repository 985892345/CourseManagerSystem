package com.course.components.utils.preferences

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import com.russhwolf.settings.Settings
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/12 20:16
 */

val Settings = createSettings("defaultSettings")

expect fun createSettings(name: String): Settings

fun Settings.stringState(key: String, defaultValue: String): ReadWriteProperty<Any?, String> {
  val state = mutableStateOf(getString(key, defaultValue))
  return object : ReadWriteProperty<Any?, String> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
      return state.value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
      putString(key, value)
      state.value = value
    }
  }
}

fun Settings.intState(key: String, defaultValue: Int): ReadWriteProperty<Any?, Int> {
  val state = mutableIntStateOf(getInt(key, defaultValue))
  return object : ReadWriteProperty<Any?, Int> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
      return state.intValue
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
      putInt(key, value)
      state.intValue = value
    }
  }
}

fun Settings.booleanState(key: String, defaultValue: Boolean): ReadWriteProperty<Any?, Boolean> {
  val state = mutableStateOf(getBoolean(key, defaultValue))
  return object : ReadWriteProperty<Any?, Boolean> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
      return state.value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
      putBoolean(key, value)
      state.value = value
    }
  }
}

fun Settings.longState(key: String, defaultValue: Long): ReadWriteProperty<Any?, Long> {
  val state = mutableLongStateOf(getLong(key, defaultValue))
  return object : ReadWriteProperty<Any?, Long> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Long {
      return state.longValue
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
      putLong(key, value)
      state.longValue = value
    }
  }
}

fun Settings.floatState(key: String, defaultValue: Float): ReadWriteProperty<Any?, Float> {
  val state = mutableFloatStateOf(getFloat(key, defaultValue))
  return object : ReadWriteProperty<Any?, Float> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Float {
      return state.floatValue
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
      putFloat(key, value)
      state.floatValue = value
    }
  }
}