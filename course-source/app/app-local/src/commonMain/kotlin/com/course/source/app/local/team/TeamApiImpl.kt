package com.course.source.app.local.team

import com.course.shared.time.MinuteTimeDate
import com.course.source.app.account.AccountType
import com.course.source.app.response.ResponseWrapper
import com.course.source.app.schedule.ScheduleRepeat
import com.course.source.app.team.SearchMember
import com.course.source.app.team.TeamApi
import com.course.source.app.team.TeamBean
import com.course.source.app.team.TeamDetail
import com.course.source.app.team.TeamList
import com.course.source.app.team.TeamMember
import com.course.source.app.team.TeamNotification
import com.course.source.app.team.TeamNotificationContent
import com.course.source.app.team.TeamRank
import com.course.source.app.team.TeamScheduleBean
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.coroutines.delay

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 12:53
 */
@ImplProvider
object TeamApiImpl : TeamApi {

  override suspend fun getTeamList(): ResponseWrapper<TeamList> {
    delay(200)
    return ResponseWrapper.success(
      TeamList(
        hasNewNotification = true,
        list = listOf(
          TeamBean(
            teamId = 1,
            name = "群1",
            description = "测试",
            rank = TeamRank.Manager,
            identity = "小组长",
          ),
          TeamBean(
            teamId = 2,
            name = "群2",
            description = "测试",
            rank = TeamRank.Member,
            identity = "",
          ),
          TeamBean(
            teamId = 3,
            name = "群3",
            description = "测试",
            rank = TeamRank.Member,
            identity = "",
          ),
          TeamBean(
            teamId = 4,
            name = "群4",
            description = "测试",
            rank = TeamRank.Administrator,
            identity = "班长",
          ),
        )
      )
    )
  }

  override suspend fun getTeamDetail(teamId: Int): ResponseWrapper<TeamDetail> {
    return ResponseWrapper.success(
      TeamDetail(
        name = "群$teamId",
        description = "测试",
        members = listOf(
          TeamMember(
            name = "甲",
            num = "2023214399",
            identity = "超级管理员",
            type = AccountType.Student,
            rank = TeamRank.Administrator,
            isConfirmed = true,
          ),
          TeamMember(
            name = "乙",
            num = "2023212399",
            identity = "管理员A",
            type = AccountType.Student,
            rank = TeamRank.Manager,
            isConfirmed = true,
          ),
          TeamMember(
            name = "丙",
            num = "2023211399",
            identity = "",
            type = AccountType.Student,
            rank = TeamRank.Member,
            isConfirmed = true,
          ),
        ) + List(70) {
          TeamMember(
            name = "$it",
            num = (2023211300 + it).toString(),
            identity = "",
            type = AccountType.Student,
            rank = TeamRank.Member,
            isConfirmed = true,
          )
        }
      )
    )
  }

  override suspend fun updateTeam(
    teamId: Int,
    name: String,
    identity: String,
    description: String,
    members: List<TeamMember>
  ): ResponseWrapper<Unit> {
    return ResponseWrapper.success(Unit)
  }

  override suspend fun createTeam(
    name: String,
    identity: String,
    description: String,
    members: List<TeamMember>
  ): ResponseWrapper<Unit> {
    return ResponseWrapper.success(Unit)
  }

  override suspend fun deleteTeam(teamId: Int): ResponseWrapper<Unit> {
    return ResponseWrapper.success(Unit)
  }

  override suspend fun searchMember(key: String): ResponseWrapper<List<SearchMember>> {
    return ResponseWrapper.success(
      listOf(
        SearchMember(
          name = "甲",
          num = "111",
          college = "软件工程",
          type = AccountType.Student,
        ),
        SearchMember(
          name = "乙",
          num = "222",
          college = "计算机科学与技术",
          type = AccountType.Student,
        ),
        SearchMember(
          name = "丙",
          num = "333",
          college = "通信",
          type = AccountType.Student,
        ),
      )
    )
  }

  override suspend fun getTeamNotification(): ResponseWrapper<List<TeamNotification>> {
    return ResponseWrapper.success(
      listOf(
        TeamNotification(
          id = 1,
          time = MinuteTimeDate(2024, 5, 1, 11),
          content = TeamNotificationContent.AddSchedule(
            teamName = "测试",
            teamSenderName = "甲",
            scheduleTitle = "123测试",
            scheduleDescription = "123测试123测试123测试123测试123测试123测试123测试",
            scheduleStartTime = MinuteTimeDate(2024, 5, 1, 12),
            scheduleMinuteDuration = 30,
          ),
        ),
        TeamNotification(
          id = 2,
          time = MinuteTimeDate(2024, 5, 2, 11),
          content = TeamNotificationContent.InviteJoinTeam(
            teamName = "测试",
            teamId = 0,
            teamAdministratorName = "甲乙丙",
            teamDescription = "这是一段描述这是一段描述这是一段描述这是一段描述这是一段描述这是一段描述",
            agreeOrNot = null,
          ),
        ),
        TeamNotification(
          id = 3,
          time = MinuteTimeDate(2024, 5, 2, 11),
          content = TeamNotificationContent.Normal(
            title = "这是一段标题",
            content = "这是一段描述这是一段描述这是一段描述这是一段描述这是一段描述"
          ),
        ),
      )
    )
  }

  override suspend fun refuseJoinTeam(teamId: Int): ResponseWrapper<Unit> {
    return ResponseWrapper.success(Unit)
  }

  override suspend fun acceptJoinTeam(teamId: Int): ResponseWrapper<Unit> {
    return ResponseWrapper.success(Unit)
  }

  override suspend fun sendNotification(
    teamId: Int,
    title: String,
    content: String
  ): ResponseWrapper<Unit> {
    return ResponseWrapper.success(Unit)
  }

  override suspend fun getTeamAllSchedule(): ResponseWrapper<List<TeamScheduleBean>> {
    return ResponseWrapper.success(emptyList())
  }

  private val teamScheduleBeans = mutableListOf(
    TeamScheduleBean(
      id = 1,
      title = "123测试",
      description = "123测试123测试123测试123测试123测试123测试123测试",
      startTime = MinuteTimeDate(2024, 5, 11, 8),
      minuteDuration = 30,
      repeat = ScheduleRepeat.Once,
      teamId = 4,
      teamName = "TeamName"
    )
  )

  override suspend fun getTeamSchedule(teamId: Int): ResponseWrapper<List<TeamScheduleBean>> {
    return ResponseWrapper.success(
      teamScheduleBeans.groupBy {
        it.teamId
      }[teamId] ?: emptyList()
    )
  }

  override suspend fun createTeamSchedule(
    title: String,
    description: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
    teamId: Int
  ): ResponseWrapper<Int> {
    val id = teamScheduleBeans.maxBy { it.id }.id + 1
    teamScheduleBeans.add(
      TeamScheduleBean(
        id = id,
        title = title,
        description = description,
        startTime = startTime,
        minuteDuration = minuteDuration,
        repeat = repeat,
        teamId = teamId,
        teamName = "TeamName",
      )
    )
    return ResponseWrapper.success(id)
  }

  override suspend fun updateTeamSchedule(
    id: Int,
    title: String,
    description: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat
  ): ResponseWrapper<Unit> {
    teamScheduleBeans.indexOfFirst { it.id == id }.let {
      teamScheduleBeans.set(
        it,
        teamScheduleBeans[it].copy(
          title = title,
          description = description,
          startTime = startTime,
          minuteDuration = minuteDuration,
          repeat = repeat,
        )
      )
    }
    return ResponseWrapper.success(Unit)
  }

  override suspend fun deleteTeamSchedule(id: Int): ResponseWrapper<Unit> {
    teamScheduleBeans.removeAt(teamScheduleBeans.indexOfFirst { it.id == id })
    return ResponseWrapper.success(Unit)
  }
}