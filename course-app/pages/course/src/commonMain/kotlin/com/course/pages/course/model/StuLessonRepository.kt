package com.course.pages.course.model

import com.course.components.utils.coroutine.AppCoroutineScope
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.utils.source.onSuccess
import com.course.source.app.course.CourseApi
import com.course.source.app.course.CourseBean
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

/**
 * .
 *
 * @author 985892345
 * 2024/3/18 21:26
 */
object StuLessonRepository {

  fun getCourseBean(stuNum: String, termIndex: Int = getNowTermIndex(stuNum)): Flow<CourseBean> {
    return flow {
      if (termIndex < 0) {
        emit(refreshCourseBean(stuNum, termIndex))
      } else {
        val cache = getCourseBeanFromCache(stuNum, termIndex)
        if (cache != null) emit(cache)
        val new = refreshCourseBean(stuNum, termIndex)
        if (new != cache) emit(new)
      }
    }.catch {  }
  }

  fun getCourseBeanFromCache(stuNum: String, termIndex: Int = getNowTermIndex(stuNum)): CourseBean? {
    if (termIndex < 0) return null
    val settings = createSettings("StuLesson-$stuNum")
    return settings.getStringOrNull(termIndex.toString())?.let { Json.decodeFromString(it) }
  }

  private fun getNowTermIndex(stuNum: String): Int {
    val settings = createSettings("StuLesson-$stuNum")
    val lastSetTime = settings.getLong("lastSetTermIndexTime", 0)
    if ((Clock.System.now().toEpochMilliseconds() - lastSetTime).milliseconds > 20.days) {
      // 最长保持 20 天
      return -1
    }
    return settings.getInt("termIndex", -1)
  }

  private fun setNowTermIndex(stuNum: String, termIndex: Int) {
    val settings = createSettings("StuLesson-$stuNum")
    settings.putInt("termIndex", termIndex)
    settings.putLong("lastSetTermIndexTime", Clock.System.now().toEpochMilliseconds())
  }

  private fun setCourseBeanToCache(stuNum: String, termIndex: Int, courseBean: CourseBean) {
    val settings = createSettings("StuLesson-$stuNum")
    settings.putString(termIndex.toString(), Json.encodeToString(courseBean))
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
            if (fixedTermIndex == -1) {
              setNowTermIndex(stuNum, it.termIndex)
            } else if (fixedTermIndex != it.termIndex) {
              throw IllegalStateException("请求的学期与当前学期不一致")
            }
          }
          .getOrThrow()
      }
    }.await()
  }
}