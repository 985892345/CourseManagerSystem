package com.course.source.app.local.login

import com.course.source.app.local.account.AccountApiImpl
import com.course.source.app.login.LoginApi
import com.course.source.app.login.RefreshTokenBean
import com.course.source.app.response.ResponseWrapper
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 22:24
 */
@ImplProvider
object LoginApiImpl : LoginApi {

  override suspend fun login(username: String, password: String): ResponseWrapper<RefreshTokenBean> {
    delay(500 + Random.nextLong(300))
    AccountApiImpl.injectorAccount(username)
    return ResponseWrapper.success(
      RefreshTokenBean("", "", 1000)
    )
  }

  override suspend fun logout(): ResponseWrapper<Unit> {
    return ResponseWrapper.success(Unit)
  }

  override suspend fun refresh(): ResponseWrapper<RefreshTokenBean> {
    return ResponseWrapper.success(
      RefreshTokenBean("", "", 1000)
    )
  }

}