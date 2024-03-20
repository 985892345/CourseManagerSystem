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

  fun getAccount(): ResponseWrapper<AccountBean?>
}

@Serializable
data class AccountBean(
  val num: String,
  val name: String,
  val isStuOrElseTea: Boolean, // 学生为 true，老师为 false
)