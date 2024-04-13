package com.course.components.utils.preferences

import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import com.russhwolf.settings.Settings

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/12 20:16
 */

val Settings = createSettings("defaultSettings")

expect fun createSettings(name: String): Settings

fun Settings.stringState(key: String, defaultValue: String): MutableState<String> {
  val state = mutableStateOf(getString(key, defaultValue))
  return object : MutableState<String> {
    private val setter: (String) -> Unit = { value = it }
    override var value: String
      get() = state.value
      set(value) {
        putString(key, value)
        state.value = value
      }

    override fun component1(): String {
      return value
    }

    override fun component2(): (String) -> Unit {
      return setter
    }
  }
}

fun Settings.intState(key: String, defaultValue: Int): MutableIntState {
  val state = mutableIntStateOf(getInt(key, defaultValue))
  return object : MutableIntState {
    private val setter: (Int) -> Unit = { intValue = it }
    override var intValue: Int
      get() = state.intValue
      set(value) {
        putInt(key, value)
        state.intValue = value
      }

    override fun component1(): Int {
      return intValue
    }

    override fun component2(): (Int) -> Unit {
      return setter
    }
  }
}

fun Settings.booleanState(key: String, defaultValue: Boolean): MutableState<Boolean> {
  val state = mutableStateOf(getBoolean(key, defaultValue))
  return object : MutableState<Boolean> {
    private val setter: (Boolean) -> Unit = { value = it }
    override var value: Boolean
      get() = state.value
      set(value) {
        putBoolean(key, value)
        state.value = value
      }

    override fun component1(): Boolean {
      return value
    }

    override fun component2(): (Boolean) -> Unit {
      return setter
    }
  }
}

fun Settings.longState(key: String, defaultValue: Long): MutableLongState {
  val state = mutableLongStateOf(getLong(key, defaultValue))
  return object : MutableLongState {
    private val setter: (Long) -> Unit = { longValue = it }
    override var longValue: Long
      get() = state.longValue
      set(value) {
        putLong(key, value)
        state.longValue = value
      }

    override fun component1(): Long {
      return longValue
    }

    override fun component2(): (Long) -> Unit {
      return setter
    }
  }
}

fun Settings.floatState(key: String, defaultValue: Float): MutableFloatState {
  val state = mutableFloatStateOf(getFloat(key, defaultValue))
  return object : MutableFloatState {
    private val setter: (Float) -> Unit = { floatValue = it }
    override var floatValue: Float
      get() = state.floatValue
      set(value) {
        putFloat(key, value)
        state.floatValue = value
      }

    override fun component1(): Float {
      return floatValue
    }

    override fun component2(): (Float) -> Unit {
      return setter
    }
  }
}