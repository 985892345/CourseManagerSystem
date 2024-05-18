package com.course.server.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.course.server.entity.NotificationEntity
import org.apache.ibatis.annotations.Mapper

/**
 * .
 *
 * @author 985892345
 * 2024/5/16 11:36
 */
@Mapper
interface NotificationMapper: BaseMapper<NotificationEntity> {
}