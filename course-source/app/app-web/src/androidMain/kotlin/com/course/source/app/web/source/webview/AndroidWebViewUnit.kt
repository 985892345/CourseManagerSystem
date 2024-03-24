package com.course.source.app.web.source.webview

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.g985892345.android.utils.context.appContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * .
 *
 * @author 985892345
 * @date 2023/10/31 08:11
 */
class AndroidWebViewUnit {

  companion object {
    private val mainHandler = Handler(Looper.getMainLooper())
  }

  private val mWebView by lazy {
    WebView(appContext).apply {
      // 支持 js
      @SuppressLint("SetJavaScriptEnabled")
      settings.javaScriptEnabled = true
    }
  }

  private var mContinuation: CancellableContinuation<String>? = null

  private fun result(result: String) {
    mContinuation?.let {
      mContinuation = null
      mainHandler.post {
        clearWebView()
        it.resume(result)
      }
    }
  }

  suspend fun load(
    url: String?,
    js: String?,
  ): String = suspendCancellableCoroutine { continuation ->
    if (url == null && js == null) {
      continuation.resumeWithException(IllegalArgumentException("url 和 js 不能都为 null"))
      return@suspendCancellableCoroutine
    }
    mContinuation = continuation
    continuation.invokeOnCancellation {
      mContinuation = null
      mainHandler.post {
        clearWebView()
      }
    }
    if (url != null) {
      loadUrl(url, js)
    } else if (js != null) {
      loadJs(js)
    }
  }

  private fun loadUrl(url: String, js: String?) {
    mainHandler.post {
      mWebView.webViewClient = object : RequestWebViewClient(url, { result(it) }) {
        override fun onPageFinished(view: WebView, url: String) {
          super.onPageFinished(view, url)
          if (js != null) {
            view.evaluateJavascript(js) {
              result(it)
            }
          } else {
            view.evaluateJavascript("document.body.textContent;") {
              result(it)
            }
          }
        }
      }
      mWebView.loadUrl(url)
    }
  }

  private fun loadJs(js: String) {
    mainHandler.post {
      mWebView.webViewClient = mEmptyWebClient
      mWebView.evaluateJavascript(js, null)
    }
  }

  private val mEmptyWebClient = WebViewClient()

  private fun clearWebView() {
    mWebView.webViewClient = mEmptyWebClient
  }

  private open class RequestWebViewClient(
    val url: String?,
    val errorCallback: (String) -> Unit
  ) : WebViewClient() {
    override fun onReceivedError(
      view: WebView,
      request: WebResourceRequest,
      error: WebResourceError
    ) {
      super.onReceivedError(view, request, error)
      if (url == request.url.toString()) {
        errorCallback.invoke("errorCode=${error.errorCode}, description=${error.description}")
      }
    }

    override fun onReceivedHttpError(
      view: WebView,
      request: WebResourceRequest,
      errorResponse: WebResourceResponse
    ) {
      super.onReceivedHttpError(view, request, errorResponse)
      if (url == request.url.toString()) {
        errorCallback.invoke("statusCode=${errorResponse.statusCode}")
      }
    }
  }
}