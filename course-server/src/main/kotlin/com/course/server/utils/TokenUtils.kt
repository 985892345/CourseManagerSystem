package com.course.server.utils

import com.course.source.app.account.AccountType
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 19:48
 */
object TokenUtils {

  const val header = "token"

  private val Key = Keys.hmacShaKeyFor(
    Decoders.BASE64.decode("23rui90afjq3894u5r90asfjnkdcn90q23ru2389ru0cfhn923ur5902oji3ffwea23rqefcd23tgvqearaa")
  )

  fun generateToken(
    userId: Int,
    num: String,
    type: AccountType,
  ): String {
    return Jwts.builder()
      .claim("userId", userId)
      .claim("num", num)
      .claim("type", type.name)
      .signWith(Key)
      .compact()
  }

  fun parseToken(token: String): TokenInfo {
    return Jwts.parser()
      .verifyWith(Key)
      .build()
      .parseSignedClaims(token)
      .payload
      .let {
        TokenInfo(
          it["userId"].toString().toInt(),
          it["num"].toString(),
          AccountType.valueOf(it["type"].toString()),
        )
      }
  }

  @Serializable
  class TokenInfo(
    val userId: Int,
    val num: String,
    val type: AccountType,
  )
}