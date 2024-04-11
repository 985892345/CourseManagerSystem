package com.course.source.app.account

import com.course.source.app.response.ResponseWrapper
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/3/19 19:24
 */
interface AccountApi {

  suspend fun getAccount(): ResponseWrapper<AccountBean>
}

@Serializable
data class AccountBean(
  val num: String,
  val name: String,
  val type: AccountType,
)

enum class AccountType {
  Student, Teacher
}