package com.course.components.utils.provider

import com.g985892345.provider.api.init.wrapper.ImplProviderWrapper
import com.g985892345.provider.api.init.wrapper.KClassProviderWrapper
import com.g985892345.provider.manager.KtProviderManager
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

/**
 * .
 *
 * @author 985892345
 * @date 2024/1/20 15:19
 */
object Provider {

  fun <T : Any> implOrNull(name: String): T? =
    KtProviderManager.getImplOrNull(name)

  fun <T : Any> impl(name: String): T =
    KtProviderManager.getImplOrThrow(name)

  fun <T : Any> implOrNull(clazz: KClass<out T>, name: String = ""): T? =
    KtProviderManager.getImplOrNull(clazz, name)

  fun <T : Any> impl(clazz: KClass<out T>, name: String = ""): T =
    KtProviderManager.getImplOrThrow(clazz, name)

  fun <T : Any> getAllImpl(clazz: KClass<out T>?): Map<String, ImplProviderWrapper<T>> =
    KtProviderManager.getAllImpl(clazz)

  fun <T : Any> clazzOrNull(name: String): KClass<out T>? =
    KtProviderManager.getKClassOrNull(name)

  fun <T : Any> clazz(name: String): KClass<out T> =
    KtProviderManager.getKClassOrThrow(name)

  fun <T : Any> clazzOrNull(clazz: KClass<out T>, name: String = ""): KClass<out T>? =
    KtProviderManager.getKClassOrNull(clazz, name)

  fun <T : Any> clazz(clazz: KClass<out T>, name: String = ""): KClass<out T> =
    KtProviderManager.getKClassOrThrow(clazz, name)

  fun <T : Any> getAllKClass(clazz: KClass<out T>?): Map<String, KClassProviderWrapper<T>> =
    KtProviderManager.getAllKClass(clazz)
}

fun <T : Any> providerImpl(name: String): ReadOnlyProperty<Any, T> {
  return ReadOnlyProperty { _, _ -> Provider.impl(name) }
}

fun <T : Any> providerImpl(clazz: KClass<out T>, name: String = ""): ReadOnlyProperty<Any, T> {
  return ReadOnlyProperty { _, _ -> Provider.impl(clazz, name) }
}

fun <T : Any> providerKClass(name: String): ReadOnlyProperty<Any, KClass<out T>> {
  return ReadOnlyProperty { _, _ -> Provider.clazz(name) }
}

fun <T : Any> providerKClass(clazz: KClass<out T>, name: String = ""): ReadOnlyProperty<Any, KClass<out T>> {
  return ReadOnlyProperty { _, _ -> Provider.clazz(clazz, name) }
}

val <T : Any> KClass<T>.impl: T
  get() =  Provider.impl(this)

val <T : Any> KClass<T>.implOrNull: T?
  get() =  Provider.implOrNull(this)
