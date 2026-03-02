package com.cool.server.mapper;

import com.cool.pojo.entity.UserPoints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户积分Mapper
 */
@Mapper
public interface UserPointsMapper {

    /**
     * 根据用户ID查询积分
     */
    UserPoints selectByUserId(@Param("userId") Long userId);

    /**
     * 插入积分记录
     */
    void insert(UserPoints userPoints);

    /**
     * 更新用户积分
     */
    void updatePoints(@Param("userId") Long userId, @Param("points") Integer points);

    /**
     * 增加用户积分
     */
    void addPoints(@Param("userId") Long userId, @Param("points") Integer points);

    /**
     * 减少用户积分
     */
    void reducePoints(@Param("userId") Long userId, @Param("points") Integer points);

    /**
     * 插入积分变动记录
     */
    void insertPointsRecord(UserPoints userPoints);
}
