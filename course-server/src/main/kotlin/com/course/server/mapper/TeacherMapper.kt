package com.course.server.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.course.server.entity.TeacherEntity
import org.apache.ibatis.annotations.Mapper

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 19:40
 */
@Mapper
interface TeacherMapper : BaseMapper<TeacherEntity> {
}