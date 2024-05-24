package com.course.source.app.team

import com.course.shared.time.MinuteTimeDate
import com.course.source.app.account.AccountType
import com.course.source.app.response.ResponseWrapper
import com.course.source.app.schedule.ScheduleRepeat
import kotlinx.serialization.Serializable

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 11:21
 */
interface TeamApi {

  suspend fun getTeamList(): ResponseWrapper<List<TeamBean>>

  suspend fun getTeamDetail(teamId: Int): ResponseWrapper<TeamDetail>

  suspend fun updateTeam(
    teamId: Int,
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

  suspend fun deleteTeam(teamId: Int): ResponseWrapper<Unit>

  suspend fun searchMember(key: String): ResponseWrapper<List<SearchMember>>

  suspend fun sendTeamNotification(
    teamId: Int,
    title: String,
    content: String,
  ): ResponseWrapper<Unit>

  suspend fun getTeamAllSchedule(): ResponseWrapper<List<TeamScheduleBean>>

  suspend fun getTeamSchedule(teamId: Int): ResponseWrapper<List<TeamScheduleBean>>

  suspend fun createTeamSchedule(
    teamId: Int,
    title: String,
    content: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
    textColor: String,
    backgroundColor: String,
  ): ResponseWrapper<Int>

  suspend fun updateTeamSchedule(
    id: Int,
    title: String,
    content: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
    textColor: String,
    backgroundColor: String,
  ): ResponseWrapper<Unit>

  suspend fun deleteTeamSchedule(id: Int): ResponseWrapper<Unit>
}

@Serializable
data class TeamBean(
  val teamId: Int,
  val name: String,
  val identity: String,
  val description: String,
  val role: TeamRole,
)

@Serializable
data class TeamDetail(
  val name: String,
  val description: String,
  val members: List<TeamMember>,
)

@Serializable
data class TeamMember(
  val userId: Int,
  val name: String,
  val num: String,
  val identity: String,
  val type: AccountType,
  val role: TeamRole,
  val isConfirmed: Boolean,
)

@Serializable
data class SearchMember(
  val userId: Int,
  val name: String,
  val num: String,
  val major: String,
  val type: AccountType,
)

enum class TeamRole {
  Administrator,
  Manager,
  Member,
}


@Serializable
data class TeamScheduleBean(
  val teamId: Int,
  val teamName: String,
  val id: Int,
  val title: String,
  val content: String,
  val startTime: MinuteTimeDate,
  val minuteDuration: Int,
  val repeat: ScheduleRepeat,
  val textColor: String,
  val backgroundColor: String,
)