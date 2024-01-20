package com.course.applications.pro

import android.app.Application
import com.g985892345.provider.coursemanagersystem.courseapp.applications.pro.ProKtProviderInitializer

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/12 20:56
 */
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    ProKtProviderInitializer.tryInitKtProvider()
  }
}