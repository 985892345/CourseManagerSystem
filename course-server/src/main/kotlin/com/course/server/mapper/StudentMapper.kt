package com.course.server.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.course.server.entity.StudentEntity
import org.apache.ibatis.annotations.Mapper

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 19:21
 */
@Mapper
interface StudentMapper : BaseMapper<StudentEntity> {
}