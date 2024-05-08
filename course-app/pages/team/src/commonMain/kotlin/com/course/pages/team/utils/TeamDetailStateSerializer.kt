package com.course.pages.team.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.course.source.app.team.TeamDetail
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 22:36
 */
class TeamDetailStateSerializer : KSerializer<MutableState<TeamDetail?>> {

  private val serializer = Json.serializersModule.serializer<TeamDetail?>()

  override val descriptor: SerialDescriptor = serializer.descriptor

  override fun deserialize(decoder: Decoder): MutableState<TeamDetail?> {
    return mutableStateOf(serializer.deserialize(decoder))
  }

  override fun serialize(encoder: Encoder, value: MutableState<TeamDetail?>) {
    serializer.serialize(encoder, value.value)
  }
}