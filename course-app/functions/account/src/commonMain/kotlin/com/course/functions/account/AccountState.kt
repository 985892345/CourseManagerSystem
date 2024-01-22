package com.course.functions.account

import com.course.components.utils.preferences.Preferences
import com.course.functions.account.api.IAccountService
import com.course.functions.account.oauth.Token
import com.course.functions.network.api.Network
import com.course.shared.app.oauth.LoginBean
import com.course.shared.app.oauth.OauthApi
import com.course.shared.base.ResponseInfo
import com.course.shared.base.ResponseWrapper
import com.g985892345.provider.api.annotation.ImplProvider
import com.russhwolf.settings.string
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/20 14:55
 */
@ImplProvider
@OptIn(DelicateCoroutinesApi::class)
object AccountState : IAccountService {

  private val _state = MutableStateFlow(tryLoginFromCache())
  override val state: StateFlow<IAccountService.State> = _state.asStateFlow()

  override suspend fun login(username: String, password: String) {
    val response = Network.client.post(OauthApi.Login) {
      setBody(FormDataContent(Parameters.build {
        append("username", username)
        append("password", password)
      }))
    }
    if (response.status.isSuccess()) {
      val wrapper = response.body<ResponseWrapper<LoginBean?>>()
      val loginBean = wrapper.data
      if (loginBean != null) {
        Token.updateToken(loginBean.token)
        updateState(IAccountService.State.Login)
      } else throw IAccountService.LoginException(wrapper)
    } else throw IAccountService.LoginException(response.body<ResponseInfo>())
  }

  override suspend fun logout() {
    val response = Network.client.post(OauthApi.Logout)
    if (response.status.isSuccess()) {
      val wrapper = response.body<ResponseInfo>()
      if (wrapper.isSuccess()) {
        Token.updateToken(null)
        updateState(IAccountService.State.Logout)
      } else throw IAccountService.LogoutException(wrapper)
    } else throw IAccountService.LogoutException(response.body<ResponseInfo>())
  }

  private var accountStatePreference by Preferences.string(
    "accountState", IAccountService.State.Logout.name
  )

  private fun updateState(newState: IAccountService.State) {
    GlobalScope.launch(Dispatchers.IO) {
      accountStatePreference = newState.name
      _state.emit(newState)
    }
  }

  private fun tryLoginFromCache(): IAccountService.State {
    val oldStateName = accountStatePreference
    return try {
      IAccountService.State.valueOf(oldStateName)
    } catch (e: Exception) {
      accountStatePreference = IAccountService.State.Logout.name
      IAccountService.State.Logout
    }.also {
      if (it == IAccountService.State.Logout && Token.accessToken != null) {
        Token.updateToken(null)
      }
    }
  }
}
