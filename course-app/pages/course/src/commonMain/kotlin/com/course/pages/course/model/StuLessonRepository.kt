package com.course.pages.course.model

import com.course.components.utils.coroutine.AppCoroutineScope
import com.course.components.utils.debug.logg
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.utils.source.onSuccess
import com.course.source.app.course.CourseApi
import com.course.source.app.course.CourseBean
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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

  fun getCourseBean(stuNum: String, termIndex: Int = -1): Flow<CourseBean> {
    return flow {
      if (termIndex < 0) {
        emit(refreshCourseBean(stuNum, termIndex))
      } else {
        val cache = getCourseBeanFromCache(stuNum, termIndex)
        if (cache != null) emit(cache)
        val new = refreshCourseBean(stuNum, termIndex)
        if (new != cache) emit(new)
      }
    }.catch { logg("wtf: ${it.stackTraceToString()}") }
  }

  fun getCourseBeanFromCache(stuNum: String, termIndex: Int = -1): CourseBean? {
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

  private val refreshDefendMap: MutableMap<Pair<String, Int>, Deferred<CourseBean>> = hashMapOf()

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
          }
          .onSuccess {
            setCourseBeanToCache(stuNum, it.termIndex, it)
            if (fixedTermIndex != -1 && fixedTermIndex != it.termIndex) {
              throw IllegalStateException("请求的学期与当前学期不一致")
            }
          }
          .getOrThrow()
      }
    }.await()
  }
}