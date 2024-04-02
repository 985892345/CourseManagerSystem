package com.course.source.app.web.source.service

import androidx.compose.foundation.Image
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.collections.immutable.persistentMapOf
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

/**
 * .
 *
 * @author 985892345
 * 2024/3/24 16:09
 */
@ImplProvider
class TextDataSourceServiceImpl : IDataSourceService {

  @OptIn(ExperimentalResourceApi::class)
  @Composable
  override fun Identifier() {
    Image(
      painter = painterResource(DrawableResource("drawable/ic_text.xml")),
      contentDescription = null,
    )
  }

  override fun config(sourceData: String?): IDataSourceService.Config {
    return IDataSourceService.Config(
      codeHint = "直接返回输入文本",
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