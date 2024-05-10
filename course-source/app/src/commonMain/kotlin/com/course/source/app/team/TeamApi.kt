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

  suspend fun getTeamList(): ResponseWrapper<TeamList>

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

  suspend fun getTeamNotification(): ResponseWrapper<List<TeamNotification>>

  suspend fun refuseJoinTeam(teamId: Int): ResponseWrapper<Unit>

  suspend fun acceptJoinTeam(teamId: Int): ResponseWrapper<Unit>

  suspend fun sendNotification(
    teamId: Int,
    title: String,
    content: String,
  ): ResponseWrapper<Unit>

  suspend fun getTeamAllSchedule(): ResponseWrapper<List<TeamScheduleBean>>

  suspend fun getTeamSchedule(teamId: Int): ResponseWrapper<List<TeamScheduleBean>>

  suspend fun createTeamSchedule(
    title: String,
    description: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
    teamId: Int,
  ): ResponseWrapper<Int>

  suspend fun updateTeamSchedule(
    id: Int,
    title: String,
    description: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
  ): ResponseWrapper<Unit>

  suspend fun deleteTeamSchedule(id: Int): ResponseWrapper<Unit>
}

@Serializable
data class TeamList(
  val hasNewNotification: Boolean,
  val list: List<TeamBean>,
)

@Serializable
data class TeamBean(
  val teamId: Int,
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
  val isConfirmed: Boolean,
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

@Serializable
data class TeamNotification(
  val id: Int,
  val time: MinuteTimeDate,
  val content: TeamNotificationContent,
)

@Serializable
sealed interface TeamNotificationContent {
  @Serializable
  data class Normal(
    val title: String,
    val content: String,
  ) : TeamNotificationContent
  @Serializable
  data class AddSchedule(
    val teamName: String,
    val teamSenderName: String,
    val scheduleTitle: String,
    val scheduleDescription: String,
    val scheduleStartTime: MinuteTimeDate,
    val scheduleMinuteDuration: Int,
  ) : TeamNotificationContent
  @Serializable
  data class InviteJoinTeam(
    val teamId: Int,
    val teamAdministratorName: String,
    val teamName: String,
    val teamDescription: String,
    var agreeOrNot: Boolean?,
  ) : TeamNotificationContent
}

@Serializable
data class TeamScheduleBean(
  val id: Int,
  val title: String,
  val description: String,
  val startTime: MinuteTimeDate,
  val minuteDuration: Int,
  val repeat: ScheduleRepeat,
  val teamId: Int,
  val teamName: String,
)