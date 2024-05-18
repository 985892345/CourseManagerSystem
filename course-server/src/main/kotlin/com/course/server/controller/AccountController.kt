package com.course.server.controller

import com.course.server.service.AccountService
import com.course.server.utils.TokenUtils
import com.course.source.app.account.AccountBean
import com.course.source.app.response.ResponseWrapper
import org.springframework.web.bind.annotation.*

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 17:11
 */
@RestController
@RequestMapping("/account")
class AccountController(
  private val accountService: AccountService
) {

  @PostMapping("/login")
  fun login(num: String, password: String): ResponseWrapper<String> {
    val token = accountService.login(num, password)
    return if (token != null) {
      ResponseWrapper.success(token)
    } else {
      ResponseWrapper.failure(10001, "账号或密码错误")
    }
  }

  @PostMapping("/logout")
  fun logout(
    @RequestHeader(TokenUtils.header) token: String,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    accountService.logout(info)
    return ResponseWrapper.success(Unit)
  }

  @PostMapping("/register")
  fun register(
    num: String, password: String, name: String, major: String, entryYear: Int
  ): ResponseWrapper<Unit> {
    accountService.register(num, password, name, major, entryYear)
    return ResponseWrapper.success(Unit)
  }

  @GetMapping("/get")
  fun getAccount(
    @RequestHeader(TokenUtils.header) token: String,
  ): ResponseWrapper<AccountBean> {
    val info = TokenUtils.parseToken(token)
    return ResponseWrapper.success(accountService.getAccount(info))
  }
}