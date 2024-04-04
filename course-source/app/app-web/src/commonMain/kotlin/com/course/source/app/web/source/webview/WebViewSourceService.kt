package com.course.source.app.web.source.webview

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import com.course.components.base.ui.toast.toast
import com.course.source.app.web.source.service.IDataSourceService
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

/**
 * .
 *
 * @author 985892345
 * 2024/3/20 22:10
 */
@ImplProvider(clazz = IDataSourceService::class, name = "WebView")
object WebViewSourceService : IDataSourceService {

  @OptIn(ExperimentalResourceApi::class)
  @Composable
  override fun Identifier() {
    Image(
      painter = painterResource(DrawableResource("drawable/ic_web.xml")),
      contentDescription = null
    )
  }

  override fun config(sourceData: String?): IDataSourceService.Config {
    val webViewData = if (sourceData.isNullOrBlank()) {
      WebViewData(null, null)
    } else {
      Json.decodeFromString(sourceData)
    }
    return IDataSourceService.Config(
      codeHint = """
        请输入 js
        
        规则：
          1. 只填写 url，会自动获取页面文本，可用于 GET 请求
          2. 只填写 js，将直接执行，可用 js 发起 POST 请求
          2. 填写 url 和 js，则在页面加载完后执行 js
        
        与端上交互规则:
          // 调用 success() 返回结果，只允许调用一次
          dataBridge.success('...'); 
          // 调用 error() 返回异常，只允许调用一次
          dataBridge.error('...');
          
        端上传递请求参数规则:
          端上可以传递参数到 url 和 js 上
          引用规则: 
            以 {TEXT} 的方式进行引用, 在请求前会进行字符串替换
          例子:
            比如端上设置参数为: stu_num, 取值为: abc
            则对于如下 url: https://test/{stu_num}
            会被替换为: https://test/abc
            js 同样如此，但请注意这只是简单的替换字符串
          并不是所有请求都会有参数，是否存在参数请查看请求格式
          
        该面板支持双指缩放，暂不支持触摸板双指缩放，但可使用 Ctrl+双指滑动 代替，
      """.trimIndent(),
      codeContent = webViewData.js,
      persistentMapOf(
        "请求链接" to IDataSourceService.EditContent(
          "http/https (可以只写 js)",
          webViewData.url,
        ),
      )
    )
  }

  override fun createSourceData(code: String, editContents: List<String>): String? {
    val url = editContents[0]
    return if (url.isNotBlank() || code.isNotBlank()) Json.encodeToString(
      WebViewData(
        url,
        code
      )
    ) else {
      toast("url 和 js 不能都为空")
      null
    }
  }

  override suspend fun request(
    sourceData: String?,
    parameterWithValue: Map<String, String>
  ): String {
    if (sourceData.isNullOrBlank()) throw IllegalArgumentException("sourceData 不能为空")
    val webViewDate = Json.decodeFromString<WebViewData>(sourceData)
    val url = webViewDate.url.replaceValue(parameterWithValue)
    val js = webViewDate.js.replaceValue(parameterWithValue)
    return requestByWebView(url, js)
  }

  private fun String?.replaceValue(
    parameterWithValue: Map<String, String>
  ): String? {
    if (parameterWithValue.isEmpty()) return this
    var result = this ?: return null
    parameterWithValue.forEach {
      result = result.replace("{${it.key}}", it.value)
    }
    return result
  }

  @Serializable
  private data class WebViewData(
    val url: String?,
    val js: String?,
  )
}

internal expect suspend fun requestByWebView(url: String?, js: String?): String
