package com.course.components.utils.source

import com.course.components.utils.provider.Provider
import com.course.source.app.response.ResponseWrapper
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
  action: (response: ResponseWrapper<T>) -> Unit
): ResponseWrapper<T> {
  contract {
    callsInPlace(action, InvocationKind.AT_MOST_ONCE)
  }
  if (!isSuccess()) action.invoke(this)
  return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T> ResponseWrapper<T>.onSuccess(
  action: (response: T) -> Unit
): ResponseWrapper<T> {
  contract {
    callsInPlace(action, InvocationKind.AT_MOST_ONCE)
  }
  if (isSuccess()) action.invoke(data)
  return this
}

fun <T> ResponseWrapper<T>.getOrDefault(defaultValue: T): T {
  return if (isSuccess()) data else defaultValue
}

fun <T> ResponseWrapper<T>.getOrNull(): T? {
  return if (isSuccess()) data else null
}

fun <T> ResponseWrapper<T>.getOrThrow(): T {
  return if (isSuccess()) data else throw ResponseException(this)
}

@OptIn(ExperimentalContracts::class)
inline fun <R, T : R> ResponseWrapper<T>.getOrElse(onFailure: (exception: Throwable) -> R): R {
  contract {
    callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
  }
  return if (isSuccess()) data else onFailure(Exception())
}

fun <T> ResponseWrapper<T>.throwOnFailure() {
  if (!isSuccess()) throw ResponseException(this)
}

fun <T> ResponseWrapper<T>.result(): Result<T> {
  return if (isSuccess()) {
    Result.success(data)
  } else {
    Result.failure(ResponseException(this))
  }
}

class ResponseException(
  val response: ResponseWrapper<*>
) : RuntimeException(response.toString())