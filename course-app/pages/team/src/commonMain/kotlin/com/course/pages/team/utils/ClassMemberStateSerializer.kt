package com.course.pages.team.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.course.source.app.team.ClassMember
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * .
 *
 * @author 985892345
 * 2024/5/11 12:06
 */
class ClassMemberStateSerializer : KSerializer<MutableState<List<ClassMember>>> {

  private val listSerializer = ListSerializer(ClassMember.serializer())

  override val descriptor: SerialDescriptor
    get() = listSerializer.descriptor

  override fun deserialize(decoder: Decoder): MutableState<List<ClassMember>> {
    return mutableStateOf(listSerializer.deserialize(decoder))
  }

  override fun serialize(encoder: Encoder, value: MutableState<List<ClassMember>>) {
    listSerializer.serialize(encoder, value.value)
  }
}