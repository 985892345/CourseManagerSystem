package com.course.source.app.web.source.webview

import kotlinx.coroutines.channels.Channel

/**
 * .
 *
 * @author 985892345
 * 2024/3/20 16:18
 */

private val WaitChanel = Channel<AndroidWebViewUnit>(3).apply {
  repeat(3) {
    trySend(AndroidWebViewUnit())
  }
}

internal actual suspend fun requestByWebView(url: String?, js: String?): String {
  return WaitChanel.receive().load(url, js)
}