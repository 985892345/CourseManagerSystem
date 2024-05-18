package com.course.server.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.course.server.entity.CourseEntity
import org.apache.ibatis.annotations.Mapper

/**
 * .
 *
 * @author 985892345
 * 2024/5/15 20:30
 */
@Mapper
interface CourseMapper : BaseMapper<CourseEntity> {
}