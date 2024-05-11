package com.course.components.base.account

import com.course.components.utils.init.IInitialService
import com.course.components.utils.preferences.Settings
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.source.app.account.AccountApi
import com.course.source.app.account.AccountBean
import com.g985892345.provider.api.annotation.ImplProvider
import com.russhwolf.settings.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2024/3/19 19:34
 */
object Account {

  private val stateFlow = MutableStateFlow<AccountBean?>(null)

  val value: AccountBean?
    get() = stateFlow.value

  fun observeAccount(): StateFlow<AccountBean?> {
    return stateFlow
  }

  suspend fun refreshAccount(): Boolean {
    return withContext(Dispatchers.IO) {
      runCatching {
        Source.api(AccountApi::class)
          .getAccount()
          .getOrThrow()
      }.tryThrowCancellationException().onSuccess {
        Settings["account"] = Json.encodeToString(it)
        stateFlow.tryEmit(it)
      }.onFailure {
        Settings.remove("account")
        stateFlow.tryEmit(null)
      }.isSuccess
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