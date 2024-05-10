package com.course.source.app.local.source.webview

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * .
 *
 * @author 985892345
 * 2024/3/20 16:18
 */
private val WaitChanel = Channel<AndroidWebViewUnit>(3)

private const val MaxSize = 10
private var size = 0
private val mutex = Mutex()

internal actual suspend fun requestByWebView(
  url: String?,
  js: String?,
  println: (String) -> Unit
): String {
  val webView = WaitChanel.tryReceive()
    .getOrNull()
    ?: mutex.withLock {
      if (size < MaxSize) {
        size++
        AndroidWebViewUnit()
      } else null
    } ?: WaitChanel.receive()
  try {
    return webView.load(url, js, println)
  } finally {
    if (WaitChanel.trySend(webView).isFailure) {
      webView.destroy()
      mutex.withLock {
        size--
      }
    }
  }
}

