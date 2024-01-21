package com.course.functions.account

import com.course.components.utils.preferences.Preferences
import com.course.functions.account.oauth.Token
import com.course.functions.network.Network
import com.course.shared.app.oauth.LoginBean
import com.course.shared.app.oauth.OauthApi
import com.russhwolf.settings.string
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
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
@OptIn(DelicateCoroutinesApi::class)
object AccountState {

  private val _state = MutableStateFlow(tryLoginFromCache())
  val state: StateFlow<State> = _state.asStateFlow()

  fun login(username: String, password: String) {
    GlobalScope.launch(Dispatchers.IO) {
      val response = Network.client.post(OauthApi.Login) {
        setBody(FormDataContent(Parameters.build {
          append("username", username)
          append("password", password)
        }))
      }
      val bean = response.body<LoginBean>()
      Token.updateToken(bean.token)
      updateState(State.Login)
    }
  }

  fun logout() {
    GlobalScope.launch(Dispatchers.IO) {
      Network.client.post(OauthApi.Logout)
      Token.updateToken(null)
      updateState(State.Logout)
    }
  }

  enum class State {
    Login,
    Logout,
  }

  private var accountStatePreference by Preferences.string("accountState", State.Logout.name)

  private fun updateState(newState: State) {
    GlobalScope.launch(Dispatchers.IO) {
      accountStatePreference = newState.name
      _state.emit(newState)
    }
  }

  private fun tryLoginFromCache(): State {
    val oldStateName = accountStatePreference
    return try {
      State.valueOf(oldStateName)
    } catch (e: Exception) {
      accountStatePreference = State.Logout.name
      State.Logout
    }.also {
      if (it == State.Logout) {
        Token.updateToken(null)
      }
    }
  }
}
