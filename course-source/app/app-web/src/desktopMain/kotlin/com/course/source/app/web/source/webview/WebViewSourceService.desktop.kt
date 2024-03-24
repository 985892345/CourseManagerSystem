package com.course.source.app.web.source.webview

/**
 * .
 *
 * @author 985892345
 * 2024/3/20 16:18
 */
internal actual suspend fun requestByWebView(url: String?, js: String?): String {
  return DesktopWebViewUnit().load(url, js)
}