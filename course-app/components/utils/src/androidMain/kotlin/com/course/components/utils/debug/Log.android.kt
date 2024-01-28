package com.course.components.utils.debug

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/23 10:22
 */

private var LogJob: Job? = null

@OptIn(DelicateCoroutinesApi::class)
actual fun log(msg: String) {
  android.util.Log.d("ggg", "(${Exception().stackTrace[1].run { "$fileName:$lineNumber" }}) -> " +
      msg)
  LogJob?.cancel()
  LogJob = GlobalScope.launch(Dispatchers.Main.immediate) {
    delay(500)
    // 打印分割线
    android.util.Log.d("ggg", "(${Exception().stackTrace[0].run { "$fileName:$lineNumber" }}) -> " +
        "=======================================================================================")
  }
}