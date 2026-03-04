package com.cool.server.mapper;

import com.cool.pojo.entity.UserPoints;
import com.cool.pojo.entity.PointsTransaction;
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
     * 初始化用户积分
     */
    void insert(UserPoints userPoints);

    /**
     * 增加用户积分（使用乐观锁）
     */
    int addPoints(@Param("userId") Long userId, @Param("points") Integer points, @Param("version") Integer version);

    /**
     * 减少用户积分（使用乐观锁）
     */
    int reducePoints(@Param("userId") Long userId, @Param("points") Integer points, @Param("version") Integer version);

    /**
     * 插入积分变动记录
     */
    void insertPointsTransaction(PointsTransaction transaction);
}
