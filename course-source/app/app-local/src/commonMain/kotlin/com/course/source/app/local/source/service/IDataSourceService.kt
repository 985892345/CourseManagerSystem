package com.course.source.app.local.source.service

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableMap

/**
 * .
 *
 * @author 985892345
 * 2024/3/20 15:30
 */
@Stable
interface IDataSourceService {

  /**
   * 用于标识该数据源的一个小组件，可以是一个图片或者其他东西
   */
  @Composable
  fun Identifier()

  /**
   * @param sourceData 自定义保存的数据，由子类自己实现，会存进本地数据，可以保存代码内容。如果为空，则说明之前没有设置数据
   * @return 返回 header, 一般用于提供一些特殊设置，比如 WebView 支持单独设置 url，其他纯脚本时可以不用设置 header
   */
  fun config(sourceData: String?): Config

  /**
   * @return 返回自定义保存的数据，返回 null 时说明不能创建数据，建议此时 toast 原因
   */
  fun createSourceData(code: String, editContents: List<String>): String?

  /**
   * 进行请求
   * @param sourceData 自定义保存的数据
   * @param parameterWithValue 参数名字与值
   */
  suspend fun request(sourceData: String?, parameterWithValue: Map<String, String>): String

  @Stable
  data class Config(
    val codeHint: String,
    val codeContent: String?,
    val editTitleHintContent: ImmutableMap<String, EditContent>,
  )

  @Stable
  data class EditContent(
    val hint: String,
    val content: String?,
  )
}