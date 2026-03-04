package com.cool.server.service;

/**
 * 用户积分服务接口
 */
public interface UserPointsService {

    /**
     * 获取用户积分
     */
    Integer getUserPoints();

    /**
     * 增加用户积分
     */
    void addPoints(Integer points, Integer type);

    /**
     * 减少用户积分
     */
    boolean reducePoints(Integer points, Integer type);

    /**
     * 初始化用户积分
     */
    void initUserPoints();
}
