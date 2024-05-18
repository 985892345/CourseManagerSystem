package com.course.server.service

import com.course.server.utils.TokenUtils
import com.course.source.app.account.AccountBean

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 18:07
 */
interface AccountService {

  fun login(num: String, password: String): String?

  fun logout(info: TokenUtils.TokenInfo)

  fun register(num: String, password: String, name: String, major: String, entryYear: Int)

  fun getAccount(info: TokenUtils.TokenInfo): AccountBean
}