package com.course.server.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.course.server.entity.CourseClassEntity
import org.apache.ibatis.annotations.Mapper

/**
 * .
 *
 * @author 985892345
 * 2024/5/18 11:11
 */
@Mapper
interface CourseClassMapper : BaseMapper<CourseClassEntity> {
}