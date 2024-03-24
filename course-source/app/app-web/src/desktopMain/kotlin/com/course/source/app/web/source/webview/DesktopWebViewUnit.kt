package com.course.source.app.web.source.webview

import com.course.components.utils.debug.logg
import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import kotlinx.coroutines.delay


/**
 * .
 *
 * @author 985892345
 * @date 2023/10/31 08:11
 */
class DesktopWebViewUnit {

  @Suppress("SetJavaScriptEnabled")
  suspend fun load(
    url: String?,
    js: String?,
  ): String {
    if (url == null && js == null) {
       throw IllegalArgumentException("url 和 js 不能都为 null")
    }
    val webClient = WebClient(BrowserVersion.BEST_SUPPORTED)
    webClient.options.isJavaScriptEnabled = true // 启动JS
    webClient.options.isUseInsecureSSL = true // 忽略ssl认证
    webClient.options.isCssEnabled = false // 禁用 css，可避免自动二次请求CSS进行渲染
    webClient.options.isThrowExceptionOnScriptError = false // 运行错误时，不抛出异常
    webClient.options.isThrowExceptionOnFailingStatusCode = false //屏蔽日志
    webClient.options.isDownloadImages = false // 禁用图片加载
    webClient.options.timeout = 3000 // 设置超时时间
    webClient.setAjaxController(NicelyResynchronizingAjaxController()) // 设置Ajax异步
    logg("111")
    try {
      val htmlPage: HtmlPage = webClient.getPage(url ?: "https://www.baidu.com/")
      delay(200)
      if (js == null) return htmlPage.body.textContent
      val result = htmlPage.executeJavaScript(js)
      return result.javaScriptResult.toString()
    } catch (e: Exception) {
      logg("e = $e")
      throw e
    } finally {
      webClient.close()
    }
  }
}