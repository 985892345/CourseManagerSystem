package com.course.server.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.course.server.entity.StudentEntity
import com.course.server.entity.TeacherEntity
import com.course.server.entity.UserEntity
import com.course.server.mapper.StudentMapper
import com.course.server.mapper.TeacherMapper
import com.course.server.mapper.UserMapper
import com.course.server.service.AccountService
import com.course.server.utils.NumUtils
import com.course.server.utils.ResponseException
import com.course.server.utils.TokenUtils
import com.course.source.app.account.AccountBean
import com.course.source.app.account.AccountType
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 18:49
 */
@Service
class AccountServiceImpl(
  private val userMapper: UserMapper,
  private val studentMapper: StudentMapper,
  private val teacherMapper: TeacherMapper,
) : AccountService {

  override fun login(num: String, password: String): String? {
    val userId: Int
    val account = if (NumUtils.isStudent(num)) {
      val stu = studentMapper.selectById(num) ?: return null
      userId = stu.userId
      AccountBean(
        num = stu.stuNum,
        name = stu.name,
        type = AccountType.Student,
      )
    } else if (NumUtils.isTeacher(num)) {
      val tea = teacherMapper.selectById(num) ?: return null
      userId = tea.userId
      AccountBean(
        num = tea.teaNum,
        name = tea.name,
        type = AccountType.Teacher,
      )
    } else return null
    val user = userMapper.selectById(userId) ?: return null
    if (user.password == sha(password)) {
      val token = TokenUtils.generateToken(userId, num, account.type)
      userMapper.update(
        KtUpdateWrapper(UserEntity::class.java)
          .eq(UserEntity::userId, user.userId)
          .set(UserEntity::token, token)
      )
      return token
    }
    return null
  }

  override fun logout(info: TokenUtils.TokenInfo) {
    userMapper.update(
      KtUpdateWrapper(UserEntity::class.java)
        .eq(UserEntity::userId, info.userId)
        .set(UserEntity::token, null)
    )
  }

  override fun register(num: String, password: String, name: String, major: String, entryYear: Int) {
    if (NumUtils.isStudent(num)) {
      val stu = studentMapper.selectById(num)
      if (stu != null) throw ResponseException("已被注册")
      val user = UserEntity(
        userId = 0,
        password = sha(password),
        type = AccountType.Student.name,
        token = null,
      )
      userMapper.insert(user)
      studentMapper.insert(
        StudentEntity(
          stuNum = num,
          name = name,
          userId = user.userId,
          major = major,
          entryYear = entryYear,
        )
      )
    } else if (NumUtils.isTeacher(num)) {
      val tea = teacherMapper.selectById(num)
      if (tea != null) throw ResponseException("已被注册")
      val user = UserEntity(
        userId = 0,
        password = sha(password),
        type = AccountType.Teacher.name,
        token = null,
      )
      userMapper.insert(user)
      teacherMapper.insert(
        TeacherEntity(
          teaNum = num,
          name = name,
          userId = user.userId,
          major = major,
        )
      )
    } else throw ResponseException("num不符合格式")
  }

  override fun getAccount(info: TokenUtils.TokenInfo): AccountBean {
    return when (info.type) {
      AccountType.Student -> {
        val stu = studentMapper.selectById(info.num)
        AccountBean(
          num = stu.stuNum,
          name = stu.name,
          type = AccountType.Student,
        )
      }
      AccountType.Teacher -> {
        val tea = teacherMapper.selectById(info.num)
        AccountBean(
          num = tea.teaNum,
          name = tea.name,
          type = AccountType.Teacher,
        )
      }
    }
  }

  private fun sha(inStr: String): String {
    try {
      val sha = MessageDigest.getInstance("SHA-256")
      val byteArray = inStr.reversed().toByteArray(StandardCharsets.UTF_8)
      val md5Bytes = sha.digest(byteArray)
      val hexValue = StringBuilder()
      for (md5Byte in md5Bytes) {
        val `val` = (md5Byte.toInt()) and 0xff
        if (`val` < 16) {
          hexValue.append("0")
        }
        hexValue.append(Integer.toHexString(`val`))
      }
      return hexValue.toString()
    } catch (e: Exception) {
      throw RuntimeException(e)
    }
  }
}