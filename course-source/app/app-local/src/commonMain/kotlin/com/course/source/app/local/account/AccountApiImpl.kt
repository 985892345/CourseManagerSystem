package com.course.source.app.local.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.course.components.base.account.Account
import com.course.components.utils.init.IInitialService
import com.course.source.app.account.AccountApi
import com.course.source.app.account.AccountBean
import com.course.source.app.response.ResponseWrapper
import com.course.source.app.local.request.SourceRequest
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2024/3/19 19:24
 */
@ImplProvider(clazz = AccountApi::class)
@ImplProvider(clazz = SourceRequest::class, name = "AccountApiImpl")
object AccountApiImpl : SourceRequest(), AccountApi {

  private val accountBeanRequest by requestContent<AccountBean>(
    key = "Account",
    name = "用户信息",
    linkedMapOf(),
    """
      // 返回以下 json 格式，如果无数据，则返回 null
      {
        num: String,
        name: String,
        type: String, // Student 或者 Teacher
      }
      
    """.trimIndent()
  )

  override suspend fun getAccount(): ResponseWrapper<AccountBean> {
    val data = accountBeanRequest.request(isForce = true, cacheable = false)
    return if (data != null) {
      ResponseWrapper.success(data)
    } else {
      throw RuntimeException("获取当前主用户信息失败")
    }
  }

  @ImplProvider(clazz = IInitialService::class, name = "AccountApiImpl")
  object AccountInitialServiceImpl : IInitialService {

    @Composable
    override fun onComposeInit() {
      LaunchedEffect(Unit) {
        accountBeanRequest // 第一次类初始化调用不能在 snapshotFlow 中触发
        snapshotFlow {
          accountBeanRequest.requestUnits.toList().also { list ->
            list.forEach {
              it.changedCount // 如果内容发生改变也需要更新
            }
          }
        }.collect {
          if (it.isNotEmpty()) {
            Account.refreshAccount()
          } else {
            Account.clearAccount()
          }
        }
      }
    }
  }
}
