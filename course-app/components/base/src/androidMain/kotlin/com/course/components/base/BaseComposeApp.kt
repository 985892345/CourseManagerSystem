package com.course.components.base

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.os.Build
import android.os.Process

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/22 17:07
 */
@SuppressLint("DiscouragedPrivateApi")
open class BaseComposeApp : Application() {

  val isMainProcess: Boolean
    get() = currentProcessName == packageName

  // https://cloud.tencent.com/developer/article/1708529
  val currentProcessName: String by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      getProcessName()
    } else {
      try {
        // Android 9 之前无反射限制
        @SuppressLint("PrivateApi")
        val declaredMethod = Class
          .forName("android.app.ActivityThread", false, Application::class.java.classLoader)
          .getDeclaredMethod("currentProcessName")
        declaredMethod.isAccessible = true
        declaredMethod.invoke(null) as String
      } catch (e: Throwable) {
        (getSystemService(ACTIVITY_SERVICE) as ActivityManager)
          .runningAppProcesses
          .first {
            it.pid == Process.myPid()
          }.processName
      }
    }
  }
}