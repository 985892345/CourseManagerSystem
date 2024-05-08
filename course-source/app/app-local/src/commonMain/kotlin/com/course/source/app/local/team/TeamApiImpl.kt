package com.course.source.app.local.team

import com.course.source.app.account.AccountType
import com.course.source.app.response.ResponseWrapper
import com.course.source.app.team.SearchMember
import com.course.source.app.team.TeamApi
import com.course.source.app.team.TeamBean
import com.course.source.app.team.TeamDetail
import com.course.source.app.team.TeamMember
import com.course.source.app.team.TeamRank
import com.g985892345.provider.api.annotation.ImplProvider
import kotlinx.coroutines.delay

/**
 * .
 *
 * @author 985892345
 * 2024/5/8 12:53
 */
@ImplProvider
class TeamApiImpl : TeamApi {

  override suspend fun getTeamList(): ResponseWrapper<List<TeamBean>> {
    delay(200)
    return ResponseWrapper.success(
      listOf(
        TeamBean(
          id = 1,
          name = "群1",
          description = "测试",
          rank = TeamRank.Manager,
          identity = "小组长",
        ),
        TeamBean(
          id = 2,
          name = "群2",
          description = "测试",
          rank = TeamRank.Member,
          identity = "",
        ),
        TeamBean(
          id = 3,
          name = "群3",
          description = "测试",
          rank = TeamRank.Member,
          identity = "",
        ),
        TeamBean(
          id = 4,
          name = "群4",
          description = "测试",
          rank = TeamRank.Administrator,
          identity = "班长",
        ),
      )
    )
  }

  override suspend fun getTeamDetail(id: Int): ResponseWrapper<TeamDetail> {
    return ResponseWrapper.success(
      TeamDetail(
        name = "群$id",
        description = "测试",
        members = listOf(
          TeamMember(
            name = "甲",
            num = "111",
            identity = "超级管理员",
            type = AccountType.Student,
            rank = TeamRank.Administrator,
          ),
          TeamMember(
            name = "乙",
            num = "222",
            identity = "管理员A",
            type = AccountType.Student,
            rank = TeamRank.Manager,
          ),
          TeamMember(
            name = "丙",
            num = "333",
            identity = "管理员B",
            type = AccountType.Student,
            rank = TeamRank.Manager,
          ),
          TeamMember(
            name = "丁",
            num = "444",
            identity = "",
            type = AccountType.Student,
            rank = TeamRank.Member,
          ),
        )
      )
    )
  }

  override suspend fun updateTeam(
    id: Int,
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

  override suspend fun deleteTeam(id: Int): ResponseWrapper<Unit> {
    return ResponseWrapper.success(Unit)
  }

  override suspend fun searchMember(name: String): ResponseWrapper<List<SearchMember>> {
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
}