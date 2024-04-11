package com.course.source.app.web.source.webview

import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.htmlunit.BrowserVersion
import org.htmlunit.NicelyResynchronizingAjaxController
import org.htmlunit.WebClient
import org.htmlunit.corejs.javascript.Scriptable
import org.htmlunit.corejs.javascript.ScriptableObject
import org.htmlunit.corejs.javascript.annotations.JSConstructor
import org.htmlunit.corejs.javascript.annotations.JSFunction
import org.htmlunit.html.HtmlPage
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


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
    if (js != null) {
      webClient.options.isJavaScriptEnabled = true // 启动JS
    }
    webClient.options.isUseInsecureSSL = true // 忽略ssl认证
    webClient.options.isCssEnabled = false // 禁用 css，可避免自动二次请求CSS进行渲染
    webClient.options.isThrowExceptionOnScriptError = false // 运行错误时，不抛出异常
    webClient.options.isThrowExceptionOnFailingStatusCode = false //屏蔽日志
    webClient.options.isDownloadImages = false // 禁用图片加载
    webClient.options.timeout = 3000 // 设置超时时间
    webClient.setAjaxController(NicelyResynchronizingAjaxController()) // 设置Ajax异步
    webClient.use {
      val htmlPage: HtmlPage = webClient.getPage(url ?: "https://www.baidu.com/")
      delay(200)
      if (js == null) return htmlPage.body.textContent
      return executeJs(htmlPage, js)
    }
  }

  private suspend fun executeJs(
    htmlPage: HtmlPage,
    js: String
  ): String = suspendCancellableCoroutine {
    val key = it.toString()
    continuationMap[key] = it
    it.invokeOnCancellation { continuationMap.remove(key) }
    val scope = htmlPage.enclosingWindow.getScriptableObject<Scriptable>()
    ScriptableObject.defineClass(scope, Desktop2JsBridge::class.java)
    htmlPage.executeJavaScript("window.dataBridge = new Desktop2JsBridge(\"$key\");")
    htmlPage.executeJavaScript(js)
  }

  companion object {
    private val continuationMap = HashMap<String, Continuation<String>>()
  }

  class Desktop2JsBridge : ScriptableObject {

    private var key: String = ""

    constructor()

    @JSConstructor
    constructor(key: String) {
      this.key = key
    }

    override fun getClassName(): String {
      return "Desktop2JsBridge"
    }

    @JSFunction
    fun success(result: Any?) {
      continuationMap.remove(key)?.resume(result.toString())
    }

    @JSFunction
    fun error(result: Any?) {
      continuationMap.remove(key)?.resumeWithException(RuntimeException(result.toString()))
    }

    companion object {
      @JvmStatic
      private val serialVersionUID = 438270592527335642L
    }
  }
}