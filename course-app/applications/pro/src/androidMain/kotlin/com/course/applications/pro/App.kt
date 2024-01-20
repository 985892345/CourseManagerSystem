package com.course.applications.pro

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.os.Build
import android.os.Process

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/12 20:56
 */
@SuppressLint("DiscouragedPrivateApi")
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    if (isMainProcess) {
      ProApp.initApp()
    }
  }

  private val isMainProcess: Boolean
    get() = currentProcessName == packageName

  // https://cloud.tencent.com/developer/article/1708529
  private val currentProcessName: String by lazy {
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