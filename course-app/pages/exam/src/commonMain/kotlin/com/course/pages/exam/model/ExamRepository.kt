package com.course.pages.exam.model

import com.course.components.utils.coroutine.AppCoroutineScope
import com.course.components.utils.debug.logg
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.utils.source.onSuccess
import com.course.source.app.exam.ExamApi
import com.course.source.app.exam.ExamTermBean
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
 * 2024/4/17 14:24
 */
object ExamRepository {

  fun getExamBean(stuNum: String): Flow<List<ExamTermBean>> {
    return flow {
      val cache = getExamBeanFromCache(stuNum)
      if (cache != null) emit(cache)
      val new = refreshExamBean(stuNum)
      if (new != cache) emit(new)
    }.catch { logg("wtf: ${it.stackTraceToString()}") }
  }

  fun getExamBeanFromCache(stuNum: String): List<ExamTermBean>? {
    val settings = createSettings("Exam-$stuNum")
    val json = settings.getStringOrNull("data") ?: return null
    return try {
      Json.decodeFromString<List<ExamTermBean>>(json)
    } catch (e: SerializationException) {
      settings.remove("data")
      null
    }
  }

  private fun setExamBeanToCache(stuNum: String, beans: List<ExamTermBean>) {
    val settings = createSettings("Exam-$stuNum")
    val json = Json.encodeToString(beans)
    settings.putString("data", json)
  }

  private val refreshDefendMap: MutableMap<String, Deferred<List<ExamTermBean>>> = hashMapOf()

  /**
   * 获取所有考试数据
   */
  suspend fun refreshExamBean(
    stuNum: String,
  ): List<ExamTermBean> {
    return refreshDefendMap.getOrPut(stuNum) {
      AppCoroutineScope.async(Dispatchers.IO) {
        Source.api(ExamApi::class).getExam(stuNum)
          .also {
            refreshDefendMap.remove(stuNum)
          }.onSuccess {
            setExamBeanToCache(stuNum, it)
          }.getOrThrow()
      }
    }.await()
  }
}