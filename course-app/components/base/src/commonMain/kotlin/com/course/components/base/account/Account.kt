package com.course.components.base.account

import com.course.components.utils.coroutine.AppCoroutineScope
import com.course.components.utils.init.IInitialService
import com.course.components.utils.preferences.Settings
import com.course.components.utils.source.Source
import com.course.components.utils.source.onFailure
import com.course.components.utils.source.onSuccess
import com.course.source.app.account.AccountApi
import com.course.source.app.account.AccountBean
import com.g985892345.provider.api.annotation.ImplProvider
import com.russhwolf.settings.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.log

/**
 * .
 *
 * @author 985892345
 * 2024/3/19 19:34
 */
object Account {

  private val stateFlow = MutableStateFlow<AccountBean?>(null)

  fun observeAccount(): StateFlow<AccountBean?> {
    return stateFlow
  }

  fun refreshAccount() {
    AppCoroutineScope.launch(Dispatchers.IO) {
      runCatching {
        Source.api(AccountApi::class).getAccount()
          .onSuccess {
            Settings["account"] = Json.encodeToString(it)
            stateFlow.tryEmit(it)
          }.onFailure {
            Settings.remove("account")
            stateFlow.tryEmit(null)
          }
      }
    }
  }

  fun clearAccount() {
    Settings.remove("account")
    stateFlow.tryEmit(null)
  }

  @ImplProvider(clazz = IInitialService::class, name = "AccountInitialServiceImpl")
  object AccountInitialServiceImpl : IInitialService {
    override fun onAppInit() {
      val account = Settings.getStringOrNull("account")
        ?.let { Json.decodeFromString<AccountBean>(it) }
      stateFlow.tryEmit(account)
    }
  }
}