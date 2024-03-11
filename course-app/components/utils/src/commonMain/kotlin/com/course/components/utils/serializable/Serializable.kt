package com.course.components.utils.serializable

import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * .
 *
 * @author 985892345
 * 2024/3/11 17:19
 */

class TextUnitSerializable : KSerializer<TextUnit> {

  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("androidx.compose.ui.unit.TextUnit", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): TextUnit {
    val decodeString = decoder.decodeString()
    val unit = when (decodeString.substringAfterLast(".")) {
      "sp" -> TextUnitType.Sp
      "em" -> TextUnitType.Em
      else -> TextUnitType.Unspecified
    }
    val value = decodeString.substringBeforeLast(".").toFloat()
    return TextUnit(value, unit)
  }

  override fun serialize(encoder: Encoder, value: TextUnit) {
    encoder.encodeString(value.toString())
  }
}

class ColorSerializable : KSerializer<Color> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("androidx.compose.ui.graphics.Color", PrimitiveKind.LONG)

  override fun deserialize(decoder: Decoder): Color {
    return Color(decoder.decodeLong().toULong())
  }

  override fun serialize(encoder: Encoder, value: Color) {
    encoder.encodeLong(value.value.toLong())
  }
}

class StateSerializable<T>(
  serializer: KSerializer<T>,
) : KSerializer<MutableState<T>> {

  private val wrapperSerializer = Wrapper.serializer(serializer)

  override val descriptor: SerialDescriptor = wrapperSerializer.descriptor

  override fun deserialize(decoder: Decoder): MutableState<T> {
    val wrapper = wrapperSerializer.deserialize(decoder)
    return mutableStateOf(wrapper.state)
  }

  override fun serialize(encoder: Encoder, value: MutableState<T>) {
    wrapperSerializer.serialize(encoder, Wrapper(value.value))
  }

  @Serializable
  private data class Wrapper<T>(
    val state: T,
  )
}

class FloatStateSerializable : KSerializer<MutableFloatState> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("androidx.compose.runtime.MutableFloatState", PrimitiveKind.FLOAT)

  override fun deserialize(decoder: Decoder): MutableFloatState {
    return mutableFloatStateOf(decoder.decodeFloat())
  }

  override fun serialize(encoder: Encoder, value: MutableFloatState) {
    encoder.encodeFloat(value.value)
  }
}

class IntStateSerializable : KSerializer<MutableIntState> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("androidx.compose.runtime.MutableIntState", PrimitiveKind.INT)

  override fun deserialize(decoder: Decoder): MutableIntState {
    return mutableIntStateOf(decoder.decodeInt())
  }

  override fun serialize(encoder: Encoder, value: MutableIntState) {
    encoder.encodeInt(value.value)
  }
}