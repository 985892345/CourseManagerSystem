package com.course.source.app.team

import com.course.source.app.account.AccountType
import com.course.source.app.response.ResponseWrapper
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 11:21
 */
interface TeamApi {

  suspend fun getTeamList(): ResponseWrapper<List<TeamBean>>

  suspend fun getTeamDetail(id: Int): ResponseWrapper<TeamDetail>

  suspend fun updateTeam(
    id: Int,
    name: String,
    identity: String,
    description: String,
    members: List<TeamMember>,
  ): ResponseWrapper<Unit>

  suspend fun createTeam(
    name: String,
    identity: String,
    description: String,
    members: List<TeamMember>,
  ): ResponseWrapper<Unit>

  suspend fun deleteTeam(id: Int): ResponseWrapper<Unit>

  suspend fun searchMember(name: String): ResponseWrapper<List<SearchMember>>
}

@Serializable
data class TeamBean(
  val id: Int,
  val name: String,
  val identity: String,
  val description: String,
  val rank: TeamRank,
)

@Serializable
data class TeamDetail(
  val name: String,
  val description: String,
  val members: List<TeamMember>,
)

@Serializable
data class TeamMember(
  val name: String,
  val num: String,
  val identity: String,
  val type: AccountType,
  val rank: TeamRank,
)

@Serializable
data class SearchMember(
  val name: String,
  val num: String,
  val college: String,
  val type: AccountType,
)

enum class TeamRank {
  Administrator,
  Manager,
  Member,
}