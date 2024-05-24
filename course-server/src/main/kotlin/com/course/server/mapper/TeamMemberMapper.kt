package com.course.server.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.course.server.entity.TeamMemberEntity
import org.apache.ibatis.annotations.Mapper

/**
 * .
 *
 * @author 985892345
 * 2024/5/23 21:40
 */
@Mapper
interface TeamMemberMapper : BaseMapper<TeamMemberEntity> {
}