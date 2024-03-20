package com.course.pages.course.model

import com.course.components.utils.coroutine.AppCoroutineScope
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.utils.source.onSuccess
import com.course.shared.course.Terms
import com.course.source.app.course.CourseApi
import com.course.source.app.course.CourseBean
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

  fun getCourseBean(stuNum: String, term: Terms? = null): Flow<CourseBean> {
    return flow {
      val cache = getCourseBeanFromCache(stuNum, term)
      if (cache != null) emit(cache)
      val new = refreshCourseBean(stuNum, term)
      if (new != cache) emit(new)
    }
  }

  fun getCourseBeanFromCache(stuNum: String, term: Terms? = null): CourseBean? {
    val settings = createSettings("StuLesson-$stuNum")
    val key = term?.name ?: settings.keys.maxBy { Terms.valueOf(it).ordinal }
    return settings.getStringOrNull(key)?.let { Json.decodeFromString(it) }
  }

  private val refreshDefendMap: MutableMap<Pair<String, Terms?>, Deferred<CourseBean>> = hashMapOf()

  suspend fun refreshCourseBean(
    stuNum: String,
    term: Terms? = null,
  ): CourseBean {
    val key = stuNum to term
    return refreshDefendMap.getOrPut(key) {
      AppCoroutineScope.async(Dispatchers.IO) {
        Source.api(CourseApi::class).getCourseBean(stuNum, term)
          .onSuccess {
            createSettings("StuLesson-$stuNum")
              .putString(it.term.name, Json.encodeToString(it))
          }
          .getOrThrow()
      }
    }.await()
  }
}