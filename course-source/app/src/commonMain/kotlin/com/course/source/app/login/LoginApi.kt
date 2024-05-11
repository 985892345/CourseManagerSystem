package com.course.source.app.login

import com.course.source.app.response.ResponseWrapper
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/3/19 16:31
 */
interface LoginApi {

  suspend fun login(username: String, password: String): ResponseWrapper<RefreshTokenBean>

  suspend fun logout(): ResponseWrapper<Unit>

  suspend fun refresh(): ResponseWrapper<RefreshTokenBean>
}

@Serializable
data class RefreshTokenBean(
  val accessToken: String,
  val refreshToken: String,
  val expiresIn: Long,
)
