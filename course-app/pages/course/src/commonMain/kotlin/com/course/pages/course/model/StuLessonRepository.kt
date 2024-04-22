package com.course.pages.course.model

import com.course.components.utils.coroutine.AppCoroutineScope
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.utils.source.onSuccess
import com.course.components.utils.time.Today
import com.course.source.app.course.CourseApi
import com.course.source.app.course.CourseBean
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2024/3/18 21:26
 */
object StuLessonRepository {

  /**
   * @param termIndex 为负数时请求当前学期数据
   */
  fun getCourseBean(stuNum: String, termIndex: Int): Flow<CourseBean> {
    return flow {
      if (termIndex < 0) {
        runCatching {
          // termIndex 为负数表示获取当前学期数据
          refreshCourseBean(stuNum, termIndex)
        }.tryThrowCancellationException().onSuccess {
          emit(it)
        }.onFailure {
          // 如果网络请求失败则回退到本地数据策略
          // 先尝试获取本地保存的当前学期数据
          val nowTermIndex = getNowTermIndex(stuNum)
          if (nowTermIndex != Int.MIN_VALUE) {
            getCourseBeanFromCache(stuNum, nowTermIndex)
              ?.let { emit(it) }
          } else {
            // 本地当前学期已经失效了，去拿上一次加载的学期数据
            val lastTermIndex = getLastTermIndex(stuNum)
            if (lastTermIndex != Int.MIN_VALUE) {
              getCourseBeanFromCache(stuNum, nowTermIndex)
                ?.let { emit(it) }
            } else {
              // 如果连上一次加载的学期数据都失效，那说明本地无数据
            }
          }
        }
      } else {
        val cache = getCourseBeanFromCache(stuNum, termIndex)
        if (cache != null) emit(cache)
        val new = runCatching {
          refreshCourseBean(stuNum, termIndex)
        }.tryThrowCancellationException().getOrNull()
        if (new != null && new != cache) emit(new)
      }
    }
  }

  fun getCourseBeanFromCache(stuNum: String, termIndex: Int): CourseBean? {
    if (termIndex < 0) return null
    val settings = createSettings("StuLesson-$stuNum")
    val key = termIndex.toString()
    val json0 = settings.getStringOrNull(key) ?: return null
    val json1 = settings.getString("${key}_1", "")
    return try {
      Json.decodeFromString<CourseBean>(json0 + json1)
    } catch (e: SerializationException) {
      settings.remove(key)
      settings.remove("${key}_1")
      null
    }
  }

  private fun setCourseBeanToCache(stuNum: String, termIndex: Int, courseBean: CourseBean) {
    val settings = createSettings("StuLesson-$stuNum")
    val termIndexStr = termIndex.toString()
    val json = Json.encodeToString(courseBean)
    if (json.length <= 8 * 1024) {
      // 解决长度超长问题
      settings.putString(termIndexStr, json)
      settings.putString("${termIndexStr}_1", "")
    } else {
      settings.putString(termIndexStr, json.substring(0, 8 * 1024))
      settings.putString("${termIndexStr}_1", json.substring(8 * 1024))
    }
  }

  /**
   * 返回当前学期数，如果不存在则返回 Int.MIN_VALUE
   */
  fun getNowTermIndex(stuNum: String): Int {
    val settings = createSettings("StuLesson-$stuNum")
    val lastSetDate = settings.getString("lastSetTermIndexTime", "")
    if (lastSetDate != Today.toString()) return Int.MIN_VALUE
    return settings.getInt("termIndex", Int.MIN_VALUE)
  }

  /**
   * 获取上一次加载数据的学期数
   */
  fun getLastTermIndex(stuNum: String): Int {
    val settings = createSettings("StuLesson-$stuNum")
    return settings.getInt("termIndex", Int.MIN_VALUE)
  }

  private fun setNowTermIndex(stuNum: String, termIndex: Int) {
    val settings = createSettings("StuLesson-$stuNum")
    settings.putInt("termIndex", termIndex)
    settings.putString("lastSetTermIndexTime", Today.toString())
  }

  private val refreshDefendMap: MutableMap<Pair<String, Int>, Deferred<CourseBean>> = hashMapOf()

  /**
   * @param termIndex 为负数时请求当前学期数据
   */
  suspend fun refreshCourseBean(
    stuNum: String,
    termIndex: Int,
  ): CourseBean {
    val fixedTermIndex = maxOf(termIndex, -1)
    val key = stuNum to fixedTermIndex
    return refreshDefendMap.getOrPut(key) {
      AppCoroutineScope.async(Dispatchers.IO) {
        Source.api(CourseApi::class).getCourseBean(stuNum, fixedTermIndex)
          .also {
            refreshDefendMap.remove(key)
          }.onSuccess {
            if (it.termIndex < 0) {
              throw IllegalStateException("学期索引不能为负")
            }
            if (fixedTermIndex != -1 && fixedTermIndex != it.termIndex) {
              throw IllegalStateException("请求的学期与当前学期不一致")
            }
            setNowTermIndex(stuNum, it.termIndex)
          }.onSuccess {
            setCourseBeanToCache(stuNum, it.termIndex, it)
          }.getOrThrow()
      }
    }.await()
  }
}