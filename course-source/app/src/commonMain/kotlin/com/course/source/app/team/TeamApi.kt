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

  suspend fun refuseDecision(teamId: Int): ResponseWrapper<Unit>

  suspend fun acceptDecision(teamId: Int): ResponseWrapper<Unit>

  suspend fun sendNotification(
    teamId: Int,
    title: String,
    content: String,
  ): ResponseWrapper<Unit>

  suspend fun getTeamAllSchedule(): ResponseWrapper<List<TeamScheduleBean>>

  suspend fun getTeamSchedule(teamId: Int): ResponseWrapper<List<TeamScheduleBean>>

  suspend fun createTeamSchedule(
    teamId: Int,
    title: String,
    description: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
    textColor: String,
    backgroundColor: String,
  ): ResponseWrapper<Int>

  suspend fun updateTeamSchedule(
    id: Int,
    title: String,
    description: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
    textColor: String,
    backgroundColor: String,
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
  val dissolvable: Boolean,
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
  data class Decision(
    val id: Int,
    val title: String,
    val content: String,
    val positiveText: String,
    val negativeText: String,
    val negativeDialog: String,
    var agreeOrNot: Boolean?,
  ) : TeamNotificationContent
}

@Serializable
data class TeamScheduleBean(
  val teamId: Int,
  val teamName: String,
  val id: Int,
  val title: String,
  val description: String,
  val startTime: MinuteTimeDate,
  val minuteDuration: Int,
  val repeat: ScheduleRepeat,
  val textColor: String,
  val backgroundColor: String,
)