package com.course.shared.app.oauth

import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/20 14:49
 */
object OauthApi {
  const val Refresh = "/oauth/refresh"
  const val Login = "/oauth/login"
  const val Logout = "/oauth/logout"
}

@Serializable
data class RefreshTokenBean(
  val accessToken: String,
  val refreshToken: String,
  val expiresIn: Long,
)

@Serializable
data class LoginBean(
  val token: RefreshTokenBean
)

@Serializable
data class ILogoutBean(
  val result: String? = null
)