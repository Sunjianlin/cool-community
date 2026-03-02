package com.cool.server.mapper;

import com.cool.pojo.entity.UserSeckillRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户秒杀记录Mapper
 */
@Mapper
public interface UserSeckillRecordMapper {
    
    /**
     * 插入记录
     */
    void insert(UserSeckillRecord record);
    
    /**
     * 查询用户是否参与过活动
     */
    Integer countByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId);
}
