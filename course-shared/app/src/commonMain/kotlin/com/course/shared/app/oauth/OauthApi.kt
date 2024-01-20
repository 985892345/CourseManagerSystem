package com.course.shared.app.oauth

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

interface IRefreshTokenBean {
  val accessToken: String
  val refreshToken: String
}

interface ILoginBean {
  val token: IRefreshTokenBean
}

interface ILogoutBean