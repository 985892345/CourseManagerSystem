package com.course.source.app.oauth

import com.course.source.app.response.ResponseWrapper
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/3/19 16:31
 */
interface OauthApi {

  fun login(username: String, password: String): ResponseWrapper<LoginBean>

  fun logout(): ResponseWrapper<Unit>

  fun refresh(): ResponseWrapper<RefreshTokenBean>
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