package com.course.source.app.local.source.service

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.Composable
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.collections.immutable.persistentMapOf

/**
 * .
 *
 * @author 985892345
 * 2024/3/24 16:09
 */
@ImplProvider(clazz = IDataSourceService::class, name = "Text")
class TextDataSourceServiceImpl : IDataSourceService {

  @Composable
  override fun Identifier() {
    Icon(
      imageVector = Icons.Default.Description,
      contentDescription = null,
    )
  }

  override fun config(sourceData: String?): IDataSourceService.Config {
    return IDataSourceService.Config(
      codeHint = "直接返回输入的文本",
      codeContent = sourceData,
      editTitleHintContent = persistentMapOf(),
    )
  }

  override fun createSourceData(code: String, editContents: List<String>): String {
    return code
  }

  override suspend fun request(
    sourceData: String?,
    parameterWithValue: Map<String, String>
  ): String {
    return sourceData!!
  }
}