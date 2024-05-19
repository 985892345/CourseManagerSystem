package com.course.pages.course.model

import com.course.components.utils.debug.logg
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.result.tryThrowCancellationException
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.utils.source.onSuccess
import com.course.source.app.course.CourseApi
import com.course.source.app.course.CourseBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2024/3/18 21:26
 */
object LessonRepository {

  fun getCourseBean(stuNum: String): Flow<CourseBean> {
    return flow {
      val cache = getCourseBeanFromCache(stuNum)
      if (cache != null) emit(cache)
      val new = runCatching {
        requestCourseBean(stuNum)
      }.tryThrowCancellationException().onFailure {
        logg("error = $it")
      }.getOrNull()
      if (new != null && new != cache) emit(new)
    }
  }

  fun getCourseBeanFromCache(stuNum: String): CourseBean? {
    val settings = createSettings("Lesson")
    val json0 = settings.getStringOrNull(stuNum) ?: return null
    val json1 = settings.getString("${stuNum}_1", "")
    val json2 = settings.getString("${stuNum}_2", "")
    return try {
      Json.decodeFromString<CourseBean>(json0 + json1 + json2)
    } catch (e: SerializationException) {
      settings.remove(stuNum)
      settings.remove("${stuNum}_1")
      settings.remove("${stuNum}_2")
      null
    }
  }

  private fun setCourseBeanToCache(stuNum: String, courseBean: CourseBean) {
    val settings = createSettings("Lesson")
    val json = Json.encodeToString(courseBean)
    if (json.length <= 8 * 1024) {
      // 解决长度超长问题
      settings.putString(stuNum, json)
      settings.putString("${stuNum}_1", "")
      settings.putString("${stuNum}_2", "")
    } else if (json.length <= 16 * 1024) {
      settings.putString(stuNum, json.substring(0, 8 * 1024))
      settings.putString("${stuNum}_1", json.substring(8 * 1024))
      settings.putString("${stuNum}_2", "")
    } else {
      settings.putString(stuNum, json.substring(0, 8 * 1024))
      settings.putString("${stuNum}_1", json.substring(8 * 1024, 16 * 1024))
      settings.putString("${stuNum}_2", json.substring(16 * 1024))
    }
  }

  suspend fun requestCourseBean(
    stuNum: String,
  ): CourseBean {
    return withContext(Dispatchers.IO) {
      Source.api(CourseApi::class).getCourseBean(stuNum)
        .onSuccess {
          setCourseBeanToCache(stuNum, it)
        }.getOrThrow()
    }
  }
}