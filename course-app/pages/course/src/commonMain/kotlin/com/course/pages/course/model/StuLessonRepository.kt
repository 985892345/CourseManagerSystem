package com.course.pages.course.model

import com.course.components.utils.coroutine.AppCoroutineScope
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.utils.source.onSuccess
import com.course.source.app.course.CourseApi
import com.course.source.app.course.CourseBean
import com.russhwolf.settings.int
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2024/3/18 21:26
 */
object StuLessonRepository {

  private var newTermIndex by createSettings("Lesson").int("newTermIndex", -1)

  fun getCourseBean(stuNum: String, termIndex: Int = newTermIndex): Flow<CourseBean> {
    return flow {
      if (termIndex < 0) {
        emit(refreshCourseBean(stuNum, termIndex))
      } else {
        val cache = getCourseBeanFromCache(stuNum, termIndex)
        if (cache != null) emit(cache)
        val new = refreshCourseBean(stuNum, termIndex)
        if (new != cache) emit(new)
      }
    }
  }

  fun getCourseBeanFromCache(stuNum: String, termIndex: Int = newTermIndex): CourseBean? {
    if (termIndex < 0) return null
    val settings = createSettings("StuLesson-$stuNum")
    return settings.getStringOrNull(termIndex.toString())?.let { Json.decodeFromString(it) }
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
          .onSuccess {
            createSettings("StuLesson-$stuNum")
              .putString(it.termIndex.toString(), Json.encodeToString(it))
            if (fixedTermIndex == -1) {
              newTermIndex = it.termIndex
            }
            if (fixedTermIndex != it.termIndex) {
              throw IllegalStateException("请求的学期与当前学期不一致")
            }
          }
          .getOrThrow()
      }
    }.await()
  }
}