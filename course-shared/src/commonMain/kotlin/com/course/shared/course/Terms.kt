package com.course.shared.course

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * .
 *
 * @author 985892345
 * 2024/3/12 12:44
 */
enum class Terms(val chinese: String) {
  FreshmanFall("大一上"),
  FreshmanSpring("大一下"),
  SophomoreFall("大二上"),
  SophomoreSpring("大二下"),
  JuniorFall("大三上"),
  JuniorSpring("大三下"),
  SeniorFall("大四上"),
  SeniorSpring("大四下"),
  FifthFall("大五上"),
  FifthSpring("大五下"),
  SixthFall("大六上"),
  SixthSpring("大六下"),
  SeventhFall("大七上"),
  SeventhSpring("大七下"),
  EighthFall("大八上"),
  EighthSpring("大八下"),
}

class TermsIntSerializer : KSerializer<Terms> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Terms", PrimitiveKind.INT)

  override fun deserialize(decoder: Decoder): Terms {
    return Terms.entries[decoder.decodeInt()]
  }

  override fun serialize(encoder: Encoder, value: Terms) {
    encoder.encodeInt(value.ordinal)
  }
}