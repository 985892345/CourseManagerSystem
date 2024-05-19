package com.course.source.server.account

import com.course.components.utils.debug.logg
import com.course.components.utils.source.onSuccess
import com.course.source.app.account.AccountApi
import com.course.source.app.account.AccountBean
import com.course.source.app.response.ResponseWrapper
import com.course.source.server.AppHttpClient
import com.course.source.server.Token
import com.g985892345.provider.api.annotation.ImplProvider
import io.github.seiko.ktorfit.annotation.generator.GenerateApi
import io.github.seiko.ktorfit.annotation.http.Field
import io.github.seiko.ktorfit.annotation.http.FormUrlEncoded
import io.github.seiko.ktorfit.annotation.http.GET
import io.github.seiko.ktorfit.annotation.http.POST

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 17:45
 */
@ImplProvider
object AccountApiImpl : AccountApi {

  private val proxy = AccountApiProxy.create(AppHttpClient)

  override suspend fun login(username: String, password: String): ResponseWrapper<String> {
    return proxy.login(username, password).onSuccess {
      Token = it
      logg("login: token = $it")
    }
  }

  override suspend fun logout(): ResponseWrapper<Unit> {
    return proxy.logout().onSuccess {
      Token = null
      logg("logout")
    }
  }

  override suspend fun getAccount(): ResponseWrapper<AccountBean> {
    return proxy.getAccount()
  }
}

@GenerateApi
interface AccountApiProxy : AccountApi {

  @POST("/account/login")
  @FormUrlEncoded
  override suspend fun login(
    @Field("num")
    username: String,
    @Field("password")
    password: String,
  ): ResponseWrapper<String>

  @POST("/account/logout")
  override suspend fun logout(): ResponseWrapper<Unit>

  @GET("/account/get")
  override suspend fun getAccount(): ResponseWrapper<AccountBean>
}