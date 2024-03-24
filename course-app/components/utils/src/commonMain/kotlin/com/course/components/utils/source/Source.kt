package com.course.components.utils.source

import com.course.components.utils.provider.Provider
import com.course.source.app.response.FailureResponseWrapper
import com.course.source.app.response.ResponseWrapper
import com.course.source.app.response.SuccessResponseWrapper
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

/**
 * .
 *
 * @author 985892345
 * 2024/3/19 17:41
 */
object Source {

  fun <T : Any> api(clazz: KClass<T>): T {
    return Provider.impl(clazz)
  }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> ResponseWrapper<T>.onFailure(
  action: (response: FailureResponseWrapper<T>) -> Unit
): ResponseWrapper<T> {
  contract {
    callsInPlace(action, InvocationKind.AT_MOST_ONCE)
  }
  if (this is FailureResponseWrapper) action.invoke(this)
  return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> ResponseWrapper<T>.onSuccess(
  action: (response: T) -> Unit
): ResponseWrapper<T> {
  contract {
    callsInPlace(action, InvocationKind.AT_MOST_ONCE)
  }
  if (this is SuccessResponseWrapper) action.invoke(data)
  return this
}

fun <T> ResponseWrapper<T>.getOrDefault(defaultValue: T): T {
  return if (this is SuccessResponseWrapper) data else defaultValue
}

fun <T> ResponseWrapper<T>.getOrNull(): T? {
  return if (this is SuccessResponseWrapper) data else null
}

fun <T> ResponseWrapper<T>.getOrThrow(): T {
  return when (this) {
    is SuccessResponseWrapper -> data
    is FailureResponseWrapper -> throw ResponseException(this)
  }
}

@OptIn(ExperimentalContracts::class)
inline fun <R, T : R> ResponseWrapper<T>.getOrElse(onFailure: (exception: Throwable) -> R): R {
  contract {
    callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
  }
  return if (this is SuccessResponseWrapper) data else onFailure(Exception())
}

fun <T> ResponseWrapper<T>.throwOnFailure() {
  if (this is FailureResponseWrapper) throw ResponseException(this)
}

fun <T> ResponseWrapper<T>.result(): Result<T> {
  return when (this) {
    is SuccessResponseWrapper -> Result.success(data)
    is FailureResponseWrapper -> Result.failure(ResponseException(this))
  }
}

class ResponseException(
  val response: FailureResponseWrapper<*>
) : RuntimeException(response.toString())