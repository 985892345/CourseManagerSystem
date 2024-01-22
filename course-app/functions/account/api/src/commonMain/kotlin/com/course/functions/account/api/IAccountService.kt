package com.course.functions.account.api

import com.course.shared.base.IResponse
import kotlinx.coroutines.flow.StateFlow

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/22 13:04
 */
interface IAccountService {

  val state: StateFlow<State>

  /**
   * @throws LoginException
   */
  suspend fun login(username: String, password: String)

  /**
   * @throws LogoutException
   */
  suspend fun logout()

  enum class State {
    Login,
    Logout,
  }

  class LoginException(
    reason: IResponse
  ) : RuntimeException(reason.toString())

  class LogoutException(
    reason: IResponse
  ) : RuntimeException(reason.toString())
}