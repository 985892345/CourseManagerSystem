package com.course.source.server.team

import com.course.shared.time.MinuteTimeDate
import com.course.source.app.response.ResponseWrapper
import com.course.source.app.schedule.ScheduleRepeat
import com.course.source.app.team.*
import com.course.source.server.AppHttpClient
import com.g985892345.provider.api.annotation.ImplProvider
import io.github.seiko.ktorfit.annotation.generator.GenerateApi
import io.github.seiko.ktorfit.annotation.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * .
 *
 * @author 985892345
 * 2024/5/24 11:41
 */
@ImplProvider
object TeamApiImpl : TeamApi by TeamApiProxy.create(AppHttpClient)

@GenerateApi
interface TeamApiProxy : TeamApi {

  @GET("/team/list")
  override suspend fun getTeamList(): ResponseWrapper<List<TeamBean>>

  @GET("/team/detail")
  override suspend fun getTeamDetail(
    @Query("teamId")
    teamId: Int,
  ): ResponseWrapper<TeamDetail>

  override suspend fun updateTeam(
    teamId: Int,
    name: String,
    identity: String,
    description: String,
    members: List<TeamMember>,
  ): ResponseWrapper<Unit> {
    return updateTeam(
      teamId = teamId,
      name = name,
      identity = identity,
      description = description,
      members = Json.encodeToString<List<TeamMember>>(members)
    )
  }

  @POST("/team/update")
  @FormUrlEncoded
  suspend fun updateTeam(
    @Field("teamId")
    teamId: Int,
    @Field("name")
    name: String,
    @Field("identity")
    identity: String,
    @Field("description")
    description: String,
    @Field("members")
    members: String,
  ): ResponseWrapper<Unit>

  override suspend fun createTeam(
    name: String,
    identity: String,
    description: String,
    members: List<TeamMember>,
  ): ResponseWrapper<Unit> {
    return createTeam(
      name = name,
      identity = identity,
      description = description,
      members = Json.encodeToString<List<TeamMember>>(members)
    )
  }

  @POST("/team/create")
  @FormUrlEncoded
  suspend fun createTeam(
    @Field("name")
    name: String,
    @Field("identity")
    identity: String,
    @Field("description")
    description: String,
    @Field("members")
    members: String,
  ): ResponseWrapper<Unit>

  @POST("/team/delete")
  @FormUrlEncoded
  override suspend fun deleteTeam(
    @Field("teamId")
    teamId: Int,
  ): ResponseWrapper<Unit>

  @GET("/team/search")
  override suspend fun searchMember(
    @Query("key")
    key: String,
  ): ResponseWrapper<List<SearchMember>>

  @POST("/team/notification")
  @FormUrlEncoded
  override suspend fun sendTeamNotification(
    @Field("teamId")
    teamId: Int,
    @Field("title")
    title: String,
    @Field("content")
    content: String,
  ): ResponseWrapper<Unit>

  @GET("/team/schedule/all")
  override suspend fun getTeamAllSchedule(): ResponseWrapper<List<TeamScheduleBean>>

  @GET("/team/schedule/get")
  override suspend fun getTeamSchedule(
    @Query("teamId")
    teamId: Int,
  ): ResponseWrapper<List<TeamScheduleBean>>

  override suspend fun createTeamSchedule(
    teamId: Int,
    title: String,
    content: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
    textColor: String,
    backgroundColor: String,
  ): ResponseWrapper<Int> {
    return createTeamSchedule(
      teamId = teamId,
      title = title,
      content = content,
      startTime = startTime.toString(),
      minuteDuration = minuteDuration,
      repeat = Json.encodeToString<ScheduleRepeat>(repeat),
      textColor = textColor,
      backgroundColor = backgroundColor,
    )
  }

  @POST("/team/schedule/create")
  @FormUrlEncoded
  suspend fun createTeamSchedule(
    @Field("teamId")
    teamId: Int,
    @Field("title")
    title: String,
    @Field("content")
    content: String,
    @Field("startTime")
    startTime: String,
    @Field("minuteDuration")
    minuteDuration: Int,
    @Field("repeat")
    repeat: String,
    @Field("textColor")
    textColor: String,
    @Field("backgroundColor")
    backgroundColor: String,
  ): ResponseWrapper<Int>

  override suspend fun updateTeamSchedule(
    id: Int,
    title: String,
    content: String,
    startTime: MinuteTimeDate,
    minuteDuration: Int,
    repeat: ScheduleRepeat,
    textColor: String,
    backgroundColor: String,
  ): ResponseWrapper<Unit> {
    return updateTeamSchedule(
      id = id,
      title = title,
      content = content,
      startTime = startTime.toString(),
      minuteDuration = minuteDuration,
      repeat = Json.encodeToString<ScheduleRepeat>(repeat),
      textColor = textColor,
      backgroundColor = backgroundColor,
    )
  }

  @POST("/team/schedule/update")
  @FormUrlEncoded
  suspend fun updateTeamSchedule(
    @Field("id")
    id: Int,
    @Field("title")
    title: String,
    @Field("content")
    content: String,
    @Field("startTime")
    startTime: String,
    @Field("minuteDuration")
    minuteDuration: Int,
    @Field("repeat")
    repeat: String,
    @Field("textColor")
    textColor: String,
    @Field("backgroundColor")
    backgroundColor: String,
  ): ResponseWrapper<Unit>

  @POST("/team/schedule/delete")
  @FormUrlEncoded
  override suspend fun deleteTeamSchedule(
    @Field("id")
    id: Int,
  ): ResponseWrapper<Unit>
}