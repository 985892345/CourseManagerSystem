package com.course.applications.pro

import android.annotation.SuppressLint
import com.course.components.base.BaseComposeApp
import com.g985892345.provider.coursemanagersystem.courseapp.applications.pro.ProKtProviderInitializer

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/12 20:56
 */
@SuppressLint("DiscouragedPrivateApi")
class App : BaseComposeApp() {
  override fun onCreate() {
    super.onCreate()
    if (isMainProcess) {
      ProKtProviderInitializer.tryInitKtProvider()
      ProApp.initApp()
    }
  }
}