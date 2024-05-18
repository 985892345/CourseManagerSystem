package com.course.server.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.course.server.entity.UserEntity
import org.apache.ibatis.annotations.Mapper

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 18:00
 */
@Mapper
interface UserMapper : BaseMapper<UserEntity> {
}