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
inline fun <T : Any> ResponseWrapper<T>.onFailure(
  action: (response: ResponseWrapper<T>) -> Unit
): ResponseWrapper<T> {
  contract {
    callsInPlace(action, InvocationKind.AT_MOST_ONCE)
  }
  if (data == null) action.invoke(this)
  return this
}

@OptIn(ExperimentalContracts::class)
inline fun <T : Any> ResponseWrapper<T>.onSuccess(
  action: (response: T) -> Unit
): ResponseWrapper<T> {
  contract {
    callsInPlace(action, InvocationKind.AT_MOST_ONCE)
  }
  data?.let(action)
  return this
}

fun <T : Any> ResponseWrapper<T>.getOrDefault(defaultValue: T): T {
  return data ?: defaultValue
}

fun <T : Any> ResponseWrapper<T>.getOrNull(): T? {
  return data
}

fun <T : Any> ResponseWrapper<T>.getOrThrow(): T {
  return data ?: throw ResponseException(this)
}

@OptIn(ExperimentalContracts::class)
inline fun <R : Any, T : R> ResponseWrapper<T>.getOrElse(onFailure: (response: ResponseWrapper<T>) -> R): R {
  contract {
    callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
  }
  return data ?: onFailure(this)
}

fun <T : Any> ResponseWrapper<T>.throwOnFailure() {
  data ?: throw ResponseException(this)
}

fun <T : Any> ResponseWrapper<T>.result(): Result<T> {
  return runCatching { getOrThrow() }
}

class ResponseException(
  val response: ResponseWrapper<*>
) : RuntimeException(response.toString())