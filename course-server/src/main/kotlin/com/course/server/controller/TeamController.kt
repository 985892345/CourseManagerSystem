package com.course.server.controller

import com.course.server.service.TeamService
import com.course.server.utils.TokenUtils
import com.course.shared.time.MinuteTimeDateSerializer
import com.course.source.app.response.ResponseWrapper
import com.course.source.app.team.SearchMember
import com.course.source.app.team.TeamBean
import com.course.source.app.team.TeamDetail
import com.course.source.app.team.TeamScheduleBean
import kotlinx.serialization.json.Json
import org.springframework.web.bind.annotation.*

/**
 * .
 *
 * @author 985892345
 * 2024/5/23 21:44
 */
@RestController
@RequestMapping("/team")
class TeamController(
  private val teamService: TeamService
) {

  @GetMapping("/list")
  fun getTeamList(
    @RequestHeader(TokenUtils.header) token: String,
  ): ResponseWrapper<List<TeamBean>> {
    val info = TokenUtils.parseToken(token)
    val result = teamService.getTeamList(info.userId)
    return ResponseWrapper.success(result)
  }

  @GetMapping("/detail")
  fun getTeamDetail(teamId: Int): ResponseWrapper<TeamDetail> {
    val result = teamService.getTeamDetail(teamId)
    return ResponseWrapper.success(result)
  }

  @PostMapping("/update")
  fun updateTeam(
    @RequestHeader(TokenUtils.header) token: String,
    teamId: Int,
    name: String,
    identity: String,
    description: String,
    members: String,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    teamService.updateTeam(
      senderId = info.userId,
      teamId = teamId,
      teamName = name,
      identity = identity,
      description = description,
      members = Json.decodeFromString(members),
    )
    return ResponseWrapper.success(Unit)
  }

  @PostMapping("/create")
  fun createTeam(
    @RequestHeader(TokenUtils.header) token: String,
    name: String,
    identity: String,
    description: String,
    members: String,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    teamService.createTeam(
      senderId = info.userId,
      teamName = name,
      identity = identity,
      description = description,
      members = Json.decodeFromString(members),
    )
    return ResponseWrapper.success(Unit)
  }

  @PostMapping("/delete")
  fun deleteTeam(
    @RequestHeader(TokenUtils.header) token: String,
    teamId: Int,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    teamService.deleteTeam(
      userId = info.userId,
      teamId = teamId,
    )
    return ResponseWrapper.success(Unit)
  }

  @GetMapping("/search")
  fun searchMember(key: String): ResponseWrapper<List<SearchMember>> {
    val result = teamService.searchMember(key)
    return ResponseWrapper.success(result)
  }

  @PostMapping("/notification")
  fun sendNotification(
    @RequestHeader(TokenUtils.header) token: String,
    teamId: Int,
    title: String,
    content: String,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    teamService.sendTeamNotification(
      senderId = info.userId,
      teamId = teamId,
      title = title,
      content = content,
    )
    return ResponseWrapper.success(Unit)
  }

  @GetMapping("/schedule/all")
  fun getTeamAllSchedule(
    @RequestHeader(TokenUtils.header) token: String,
  ): ResponseWrapper<List<TeamScheduleBean>> {
    val info = TokenUtils.parseToken(token)
    val result = teamService.getTeamAllSchedule(info.userId)
    return ResponseWrapper.success(result)
  }

  @GetMapping("/schedule/get")
  fun getTeamSchedule(
    @RequestHeader(TokenUtils.header) token: String,
    teamId: Int,
  ): ResponseWrapper<List<TeamScheduleBean>> {
    val result = teamService.getTeamSchedule(teamId)
    return ResponseWrapper.success(result)
  }

  @PostMapping("/schedule/create")
  fun createTeamSchedule(
    @RequestHeader(TokenUtils.header) token: String,
    teamId: Int,
    title: String,
    content: String,
    startTime: String,
    minuteDuration: Int,
    repeat: String,
    textColor: String,
    backgroundColor: String,
  ): ResponseWrapper<Int> {
    val info = TokenUtils.parseToken(token)
    val result = teamService.createTeamSchedule(
      senderId = info.userId,
      teamId = teamId,
      title = title,
      content = content,
      startTime = MinuteTimeDateSerializer.deserialize(startTime),
      minuteDuration = minuteDuration,
      repeat = Json.decodeFromString(repeat),
      textColor = textColor,
      backgroundColor = backgroundColor,
    )
    return ResponseWrapper.success(result)
  }

  @PostMapping("/schedule/update")
  fun updateTeamSchedule(
    @RequestHeader(TokenUtils.header) token: String,
    id: Int,
    title: String,
    content: String,
    startTime: String,
    minuteDuration: Int,
    repeat: String,
    textColor: String,
    backgroundColor: String,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    teamService.updateTeamSchedule(
      senderId = info.userId,
      id = id,
      title = title,
      content = content,
      startTime = MinuteTimeDateSerializer.deserialize(startTime),
      minuteDuration = minuteDuration,
      repeat = Json.decodeFromString(repeat),
      textColor = textColor,
      backgroundColor = backgroundColor,
    )
    return ResponseWrapper.success(Unit)
  }

  @PostMapping("/schedule/delete")
  fun deleteTeamSchedule(
    @RequestHeader(TokenUtils.header) token: String,
    id: Int,
  ): ResponseWrapper<Unit> {
    val info = TokenUtils.parseToken(token)
    teamService.deleteTeamSchedule(info.userId, id)
    return ResponseWrapper.success(Unit)
  }
}