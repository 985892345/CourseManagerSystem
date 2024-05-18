package com.course.pages.exam.model

import com.course.components.utils.debug.logg
import com.course.components.utils.preferences.createSettings
import com.course.components.utils.source.Source
import com.course.components.utils.source.getOrThrow
import com.course.components.utils.source.onSuccess
import com.course.source.app.exam.ExamApi
import com.course.source.app.exam.ExamTermBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
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

  fun getExamBean(stuNum: String): Flow<ExamTermBean> {
    return flow {
      val cache = getExamBeanFromCache(stuNum)
      if (cache != null) emit(cache)
      val new = refreshExamBean(stuNum)
      if (new != cache) emit(new)
    }.catch { logg("wtf: ${it.stackTraceToString()}") }
  }

  fun getExamBeanFromCache(stuNum: String): ExamTermBean? {
    val settings = createSettings("Exam-$stuNum")
    val json = settings.getStringOrNull("data") ?: return null
    return try {
      Json.decodeFromString<ExamTermBean>(json)
    } catch (e: SerializationException) {
      settings.remove("data")
      null
    }
  }

  private fun setExamBeanToCache(stuNum: String, beans: ExamTermBean) {
    val settings = createSettings("Exam-$stuNum")
    val json = Json.encodeToString(beans)
    settings.putString("data", json)
  }

  /**
   * 获取所有考试数据
   */
  suspend fun refreshExamBean(
    stuNum: String,
  ): ExamTermBean {
    return withContext(Dispatchers.IO) {
      Source.api(ExamApi::class).getExam(stuNum)
        .onSuccess {
          setExamBeanToCache(stuNum, it)
        }.getOrThrow()
    }
  }
}