package com.course.pages.team.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.course.source.app.team.TeamMember
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 22:33
 */
class TeamMemberStateSerializer : KSerializer<MutableState<List<TeamMember>>> {

  private val listSerializer = ListSerializer(TeamMember.serializer())

  override val descriptor: SerialDescriptor
    get() = listSerializer.descriptor

  override fun deserialize(decoder: Decoder): MutableState<List<TeamMember>> {
    return mutableStateOf(listSerializer.deserialize(decoder))
  }

  override fun serialize(encoder: Encoder, value: MutableState<List<TeamMember>>) {
    listSerializer.serialize(encoder, value.value)
  }
}