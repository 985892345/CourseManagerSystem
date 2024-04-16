package com.course.source.app.local.source.webview

/**
 * .
 *
 * @author 985892345
 * 2024/3/20 16:18
 */
internal actual suspend fun requestByWebView(url: String?, js: String?): String {
  return IosWebViewUnit().load(url, js)
}