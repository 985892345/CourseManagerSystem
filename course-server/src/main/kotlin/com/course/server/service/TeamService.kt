package com.course.server.service

import com.course.shared.time.MinuteTimeDate
import com.course.source.app.schedule.ScheduleRepeat
import com.course.source.app.team.*

/**
 * .
 *
 * @author 985892345
 * 2024/5/23 21:43
 */
interface TeamService {
  fun getTeamList(userId: Int): List<TeamBean>
  fun getTeamDetail(teamId: Int): TeamDetail
  fun updateTeam(
    senderId: Int,
    teamId: Int,
    teamName: String,
    identity: String,
    description: String,
    members: List<TeamMember>,
  )
  fun createTeam(
    senderId: Int,
    teamName: String,
    identity: String,
    description: String,
    members: List<TeamMember>,
  )
  fun deleteTeam(userId: Int, teamId: Int)
  fun searchMember(key: String): List<SearchMember>
  fun sendTeamNotification(
    senderId: Int,
    teamId: Int,
    title: String,
    content: String,
  )
  fun getTeamAllSchedule(userId: Int): List<TeamScheduleBean>
  fun getTeamSchedule(teamId: Int): List<TeamScheduleBean>
  fun createTeamSchedule(
    senderId: Int,
    teamId: Int,
    title: String,
    content: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
    textColor: String,
    backgroundColor: String,
  ): Int
  fun updateTeamSchedule(
    senderId: Int,
    id: Int,
    title: String,
    content: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
    textColor: String,
    backgroundColor: String,
  )
  fun deleteTeamSchedule(senderId: Int, id: Int)
}