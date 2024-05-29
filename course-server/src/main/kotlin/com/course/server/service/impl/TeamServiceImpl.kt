package com.course.server.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.course.server.entity.*
import com.course.server.mapper.*
import com.course.server.service.NotificationService
import com.course.server.service.TeamService
import com.course.server.utils.ResponseException
import com.course.shared.time.MinuteTimeDate
import com.course.source.app.account.AccountType
import com.course.source.app.notification.DecisionBtn
import com.course.source.app.notification.NotificationContent
import com.course.source.app.schedule.ScheduleRepeat
import com.course.source.app.team.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service

/**
 * .
 *
 * @author 985892345
 * 2024/5/23 21:42
 */
@Service
class TeamServiceImpl(
  private val teamMapper: TeamMapper,
  private val teamMemberMapper: TeamMemberMapper,
  private val teamScheduleMapper: TeamScheduleMapper,
  private val userMapper: UserMapper,
  private val teacherMapper: TeacherMapper,
  private val studentMapper: StudentMapper,
  private val notificationService: NotificationService,
) : TeamService {

  override fun getTeamList(userId: Int): List<TeamBean> {
    return teamMemberMapper.selectList(
      KtQueryWrapper(TeamMemberEntity::class.java)
        .eq(TeamMemberEntity::userId, userId)
        .eq(TeamMemberEntity::isConfirmed, true)
    ).map {
      val team = teamMapper.selectById(it.teamId)
      TeamBean(
        teamId = team.teamId,
        name = team.teamName,
        identity = it.identity,
        description = team.description,
        role = it.role,
      )
    }
  }

  override fun getTeamDetail(teamId: Int): TeamDetail {
    val team = teamMapper.selectById(teamId)
    return TeamDetail(
      name = team.teamName,
      description = team.description,
      members = teamMemberMapper.selectList(
        KtQueryWrapper(TeamMemberEntity::class.java)
          .eq(TeamMemberEntity::teamId, teamId)
      ).map {
        val user = userMapper.selectById(it.userId)
        when (user.type) {
          AccountType.Student -> {
            val stu = studentMapper.selectOne(
              KtQueryWrapper(StudentEntity::class.java)
                .eq(StudentEntity::userId, it.userId)
            )
            TeamMember(
              userId = it.userId,
              name = stu.name,
              num = stu.stuNum,
              identity = it.identity,
              type = AccountType.Student,
              role = it.role,
              isConfirmed = it.isConfirmed
            )
          }

          AccountType.Teacher -> {
            val tea = teacherMapper.selectOne(
              KtQueryWrapper(TeacherEntity::class.java)
                .eq(TeacherEntity::userId, it.userId)
            )
            TeamMember(
              userId = it.userId,
              name = tea.name,
              num = tea.teaNum,
              identity = it.identity,
              type = AccountType.Teacher,
              role = it.role,
              isConfirmed = it.isConfirmed
            )
          }
        }
      }
    )
  }

  override fun updateTeam(
    senderId: Int,
    teamId: Int,
    teamName: String,
    identity: String,
    description: String,
    members: List<TeamMember>,
  ) {
    val senderName = getSenderName(senderId)
    teamMapper.update(
      KtUpdateWrapper(TeamEntity::class.java)
        .eq(TeamEntity::teamId, teamId)
        .set(TeamEntity::teamName, teamName)
        .set(TeamEntity::description, description)
    )
    val oldMembers = teamMemberMapper.selectList(
      KtQueryWrapper(TeamMemberEntity::class.java)
        .eq(TeamMemberEntity::teamId, teamId)
        .notIn(TeamMemberEntity::userId, senderId) // 去掉超级管理员自身
    ).map { it.userId to it }.toMap(mutableMapOf())
    val time = MinuteTimeDate.now()
    members.forEach {
      if (it.userId == senderId) return@forEach
      val old = oldMembers.remove(it.userId)
      if (old != null) {
        if (old.identity != it.identity) {
          teamMemberMapper.update(
            KtUpdateWrapper(TeamMemberEntity::class.java)
              .eq(TeamMemberEntity::teamId, teamId)
              .eq(TeamMemberEntity::userId, it.userId)
              .set(TeamMemberEntity::identity, it.identity)
          )
        }
      } else {
        // 新增成员
        teamMemberMapper.insert(
          TeamMemberEntity(
            teamId = teamId,
            userId = it.userId,
            roleStr = it.role.name,
            identity = it.identity,
            isConfirmed = it.isConfirmed,
            inviteNotificationId = inviteNotification(
              teamId = teamId,
              time = time,
              senderName = senderName,
              senderId = senderId,
              responseId = it.userId,
              responseName = it.name,
              teamName = teamName,
              content = description,
            )
          )
        )
      }
    }
    // 被移除的成员
    oldMembers.forEach {
      teamMemberMapper.delete(
        KtQueryWrapper(TeamMemberEntity::class.java)
          .eq(TeamMemberEntity::teamId, teamId)
          .eq(TeamMemberEntity::userId, it.key)
      )
      if (it.value.isConfirmed) {
        notificationService.addNotification(
          userId = it.key,
          time = time,
          content = NotificationServerContent.Normal(
            title = "被移出团队",
            content = "你被${senderName}从${teamName}中移除",
          )
        )
      } else if (it.value.inviteNotificationId != null) {
        notificationService.changeNotificationExpired(
          notificationId = it.value.inviteNotificationId!!,
          expiredTimestamp = System.currentTimeMillis(),
          expiredText = "邀请被取消",
        )
      }
    }
  }

  override fun createTeam(
    senderId: Int,
    teamName: String,
    identity: String,
    description: String,
    members: List<TeamMember>,
  ) {
    val team = TeamEntity(
      teamId = 0,
      teamName = teamName,
      description = description,
      adminId = senderId,
    )
    teamMapper.insert(team)
    val time = MinuteTimeDate.now()
    val senderName = getSenderName(senderId)
    members.forEach {
      if (it.userId == senderId) return@forEach
      teamMemberMapper.insert(
        TeamMemberEntity(
          teamId = team.teamId,
          userId = it.userId,
          roleStr = it.role.name,
          identity = it.identity,
          isConfirmed = false,
          inviteNotificationId = inviteNotification( // 发起通知
            teamId = team.teamId,
            time = time,
            senderName = senderName,
            senderId = senderId,
            responseId = it.userId,
            responseName = it.name,
            teamName = teamName,
            content = description,
          )
        )
      )
    }
    teamMemberMapper.insert(
      TeamMemberEntity(
        teamId = team.teamId,
        userId = senderId,
        roleStr = TeamRole.Administrator.name,
        identity = identity,
        isConfirmed = true,
        inviteNotificationId = null,
      )
    )
  }

  override fun deleteTeam(userId: Int, teamId: Int) {
    val teamMember = teamMemberMapper.selectOne(
      KtQueryWrapper(TeamMemberEntity::class.java)
        .eq(TeamMemberEntity::userId, userId)
        .eq(TeamMemberEntity::teamId, teamId)
    )
    if (teamMember == null) throw ResponseException("非团队成员")
    if (teamMember.role != TeamRole.Administrator) throw ResponseException("非超级管理员")
    val team = teamMapper.selectById(teamId)
    val teamMemberList = teamMemberMapper.selectList(
      KtQueryWrapper(TeamMemberEntity::class.java)
        .eq(TeamMemberEntity::teamId, teamId)
    )
    // 通知团体被解散
    val time = MinuteTimeDate.now()
    val userName = getSenderName(userId)
    teamMemberList.forEach {
      if (it.isConfirmed) {
        notificationService.addNotification(
          userId = it.userId,
          time = time,
          content = NotificationServerContent.Normal(
            title = "团队被解散",
            content = "${userName}解散了${team.teamName}"
          )
        )
      } else if (it.inviteNotificationId != null) {
        notificationService.changeNotificationExpired(
          notificationId = it.inviteNotificationId,
          expiredTimestamp = System.currentTimeMillis(),
          expiredText = "团队已解散",
        )
      }
    }
    teamMemberMapper.delete(
      KtQueryWrapper(TeamMemberEntity::class.java)
        .eq(TeamMemberEntity::teamId, teamId)
    )
    teamScheduleMapper.delete(
      KtQueryWrapper(TeamScheduleEntity::class.java)
        .eq(TeamScheduleEntity::teamId, teamId)
    )
  }

  override fun searchMember(key: String): List<SearchMember> {
    return studentMapper.selectList(
      KtQueryWrapper(StudentEntity::class.java)
        .like(StudentEntity::name, key)
        .or()
        .likeRight(StudentEntity::stuNum, key)
    ).map {
      SearchMember(
        userId = it.userId,
        name = it.name,
        num = it.stuNum,
        major = it.major,
        type = AccountType.Student,
      )
    } + teacherMapper.selectList(
      KtQueryWrapper(TeacherEntity::class.java)
        .like(TeacherEntity::name, key)
        .or()
        .likeRight(TeacherEntity::teaNum, key)
    ).map {
      SearchMember(
        userId = it.userId,
        name = it.name,
        num = it.teaNum,
        major = it.major,
        type = AccountType.Teacher,
      )
    }
  }

  override fun sendTeamNotification(
    senderId: Int,
    teamId: Int,
    title: String,
    content: String,
  ) {
    val team = teamMapper.selectById(teamId)
    val time = MinuteTimeDate.now()
    val senderName = getSenderName(senderId)
    teamMemberMapper.selectList(
      KtQueryWrapper(TeamMemberEntity::class.java)
        .eq(TeamMemberEntity::teamId, teamId)
        .eq(TeamMemberEntity::isConfirmed, true)
    ).forEach {
      notificationService.addNotification(
        userId = it.userId,
        time = time,
        content = NotificationServerContent.Normal(
          title = title,
          content = content,
          bottomEnd = "由${team.teamName}—${senderName}创建",
        )
      )
    }
  }

  override fun getTeamAllSchedule(userId: Int): List<TeamScheduleBean> {
    return teamMemberMapper.selectList(
      KtQueryWrapper(TeamMemberEntity::class.java)
        .eq(TeamMemberEntity::userId, userId)
        .eq(TeamMemberEntity::isConfirmed, true)
    ).flatMap { teamMember ->
      val team = teamMapper.selectById(teamMember.teamId)
      teamScheduleMapper.selectList(
        KtQueryWrapper(TeamScheduleEntity::class.java)
          .eq(TeamScheduleEntity::teamId, teamMember.teamId)
      ).map {
        TeamScheduleBean(
          teamId = team.teamId,
          teamName = team.teamName,
          id = it.teamScheduleId,
          title = it.title,
          content = it.content,
          startTime = it.startTime,
          minuteDuration = it.minuteDuration,
          repeat = it.repeat,
          textColor = it.textColor,
          backgroundColor = it.backgroundColor,
        )
      }
    }
  }

  override fun getTeamSchedule(teamId: Int): List<TeamScheduleBean> {
    val team = teamMapper.selectById(teamId)
    return teamScheduleMapper.selectList(
      KtQueryWrapper(TeamScheduleEntity::class.java)
        .eq(TeamScheduleEntity::teamId, teamId)
    ).map {
      TeamScheduleBean(
        teamId = team.teamId,
        teamName = team.teamName,
        id = it.teamScheduleId,
        title = it.title,
        content = it.content,
        startTime = it.startTime,
        minuteDuration = it.minuteDuration,
        repeat = it.repeat,
        textColor = it.textColor,
        backgroundColor = it.backgroundColor,
      )
    }
  }

  override fun createTeamSchedule(
    senderId: Int,
    teamId: Int,
    title: String,
    content: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
    textColor: String,
    backgroundColor: String,
  ): Int {
    val team = teamMapper.selectById(teamId) ?: throw ResponseException("团队不存在")
    val teamMember = teamMemberMapper.selectOne(
      KtQueryWrapper(TeamMemberEntity::class.java)
        .eq(TeamMemberEntity::teamId, teamId)
        .eq(TeamMemberEntity::userId, senderId)
    )
    if (teamMember == null || teamMember.role > TeamRole.Manager) throw ResponseException("非团队管理员")
    val teamSchedule = TeamScheduleEntity(
      teamScheduleId = 0,
      teamId = teamId,
      senderId = senderId,
      title = title,
      content = content,
      startTimeStr = startTime.toString(),
      minuteDuration = minuteDuration,
      repeatContent = Json.encodeToString(repeat),
      textColor = textColor,
      backgroundColor = backgroundColor,
    )
    teamScheduleMapper.insert(teamSchedule)
    // 发送通知
    val time = MinuteTimeDate.now()
    val senderName = getSenderName(senderId)
    teamMemberMapper.selectList(
      KtQueryWrapper(TeamMemberEntity::class.java)
        .eq(TeamMemberEntity::teamId, teamId)
        .eq(TeamMemberEntity::isConfirmed, true)
    ).forEach {
      notificationService.addNotification(
        userId = it.userId,
        time = time,
        content = NotificationServerContent.Normal(
          title = "新增团队日程：${title}",
          subtitle = "时间：${startTime}-${startTime.time.plusMinutes(minuteDuration)}",
          content = content,
          bottomEnd = "由${team.teamName}—${senderName}创建"
        )
      )
    }
    return teamSchedule.teamScheduleId
  }

  override fun updateTeamSchedule(
    senderId: Int,
    id: Int,
    title: String,
    content: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
    textColor: String,
    backgroundColor: String,
  ) {
    val oldTeamSchedule = teamScheduleMapper.selectById(id) ?: throw ResponseException("日程不存在")
    val team = teamMapper.selectById(oldTeamSchedule.teamId) ?: throw ResponseException("团队不存在")
    val teamMember = teamMemberMapper.selectOne(
      KtQueryWrapper(TeamMemberEntity::class.java)
        .eq(TeamMemberEntity::teamId, team.teamId)
        .eq(TeamMemberEntity::userId, senderId)
    )
    if (teamMember == null || teamMember.role > TeamRole.Manager) throw ResponseException("非团队管理员")
    teamScheduleMapper.update(
      KtUpdateWrapper(TeamScheduleEntity::class.java)
        .eq(TeamScheduleEntity::teamScheduleId, id)
        .set(TeamScheduleEntity::title, title)
        .set(TeamScheduleEntity::content, content)
        .set(TeamScheduleEntity::startTimeStr, startTime.toString())
        .set(TeamScheduleEntity::minuteDuration, minuteDuration)
        .set(TeamScheduleEntity::repeatContent, Json.encodeToString(repeat))
        .set(TeamScheduleEntity::textColor, textColor)
        .set(TeamScheduleEntity::backgroundColor, backgroundColor)
    )
    // 发送通知
    val time = MinuteTimeDate.now()
    val senderName = getSenderName(senderId)
    teamMemberMapper.selectList(
      KtQueryWrapper(TeamMemberEntity::class.java)
        .eq(TeamMemberEntity::teamId, team.teamId)
        .eq(TeamMemberEntity::isConfirmed, true)
    ).forEach {
      notificationService.addNotification(
        userId = it.userId,
        time = time,
        content = NotificationServerContent.Normal(
          title = "团队日程更新：${title}",
          subtitle = "时间：${startTime}-${startTime.time.plusMinutes(minuteDuration)}",
          content = content,
          bottomEnd = "由${team.teamName}—${senderName}更新"
        )
      )
    }
  }

  override fun deleteTeamSchedule(senderId: Int, id: Int) {
    val oldTeamSchedule = teamScheduleMapper.selectById(id) ?: throw ResponseException("日程不存在")
    val team = teamMapper.selectById(oldTeamSchedule.teamId) ?: throw ResponseException("团队不存在")
    val teamMember = teamMemberMapper.selectOne(
      KtQueryWrapper(TeamMemberEntity::class.java)
        .eq(TeamMemberEntity::teamId, team.teamId)
        .eq(TeamMemberEntity::userId, senderId)
    )
    if (teamMember == null || teamMember.role > TeamRole.Manager) throw ResponseException("非团队管理员")
    teamScheduleMapper.deleteById(id)
    // 发送通知
    val time = MinuteTimeDate.now()
    val senderName = getSenderName(senderId)
    teamMemberMapper.selectList(
      KtQueryWrapper(TeamMemberEntity::class.java)
        .eq(TeamMemberEntity::teamId, team.teamId)
        .eq(TeamMemberEntity::isConfirmed, true)
    ).forEach {
      notificationService.addNotification(
        userId = it.userId,
        time = time,
        content = NotificationServerContent.Normal(
          title = "团队日程被移除：${oldTeamSchedule.title}",
          subtitle = "时间：${oldTeamSchedule.startTime}-${oldTeamSchedule.startTime.time.plusMinutes(oldTeamSchedule.minuteDuration)}",
          content = oldTeamSchedule.content,
          bottomEnd = "由${team.teamName}—${senderName}移除"
        )
      )
    }
  }

  private fun getSenderName(senderId: Int): String {
    return userMapper.selectById(senderId).let {
      when (it.type) {
        AccountType.Student -> studentMapper.selectOne(
          KtQueryWrapper(StudentEntity::class.java)
            .eq(StudentEntity::userId, it.userId)
        ).name

        AccountType.Teacher -> teacherMapper.selectOne(
          KtQueryWrapper(TeacherEntity::class.java)
            .eq(TeacherEntity::userId, it.userId)
        ).name
      }
    }
  }

  private fun inviteNotification(
    teamId: Int,
    time: MinuteTimeDate,
    senderName: String,
    senderId: Int,
    responseId: Int,
    responseName: String,
    teamName: String,
    content: String,
  ): Int {
    return notificationService.addNotification(
      userId = responseId,
      time = time,
      content = NotificationServerContent.Decision(
        clientContent = NotificationContent.Decision(
          title = "${senderName}邀请你加入${teamName}",
          content = "团队简介：$content",
          btn = DecisionBtn.Pending(
            positiveText = "已同意",
            negativeText = "已拒绝",
            negativeDialog = "确定拒绝该邀请吗？",
          ),
        ),
        responseUserId = senderId,
        positiveResponse = NotificationServerContent.Normal(
          title = "${responseName}已同意邀请",
          content = "${responseName}同意了加入${teamName}的邀请"
        ),
        negativeResponse = NotificationServerContent.Normal(
          title = "${responseName}拒绝了邀请",
          content = "${responseName}拒绝了加入${teamName}的邀请"
        ),
        expiredText = "",
        expiredTimestamp = Long.MAX_VALUE,
        decisionType = DecisionType.TeamInvite(
          teamId = teamId,
          userId = responseId,
        )
      )
    )
  }
}