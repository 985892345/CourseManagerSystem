package com.course.source.app.web.source.service

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.g985892345.provider.api.annotation.ImplProvider

/**
 * .
 *
 * @author 985892345
 * 2024/3/24 16:09
 */
@ImplProvider
class TestDataSourceServiceImpl : IDataSourceService {
  @Composable
  override fun Identifier() {
    Text(text = "Test")
  }

  override fun config(sourceData: String?): IDataSourceService.Config {
    return IDataSourceService.Config(
      codeHint = "",
      codeContent = null,
      editTitleHintContent = emptyList(),
    )
  }

  override fun createSourceData(code: String, editContents: List<String>): String? {
    return null
  }

  override suspend fun request(
    sourceData: String?,
    parameterWithValue: Map<String, String>
  ): String {
    return ""
  }
}