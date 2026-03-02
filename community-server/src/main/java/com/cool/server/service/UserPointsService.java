package com.cool.server.service;

/**
 * 用户积分服务接口
 */
public interface UserPointsService {

    /**
     * 获取用户积分
     */
    Integer getUserPoints(Long userId);

    /**
     * 增加用户积分
     */
    void addPoints(Long userId, Integer points);

    /**
     * 增加用户积分并记录类型和描述
     */
    void addPoints(Long userId, Integer points, String type, String description);

    /**
     * 减少用户积分
     */
    boolean reducePoints(Long userId, Integer points);

    /**
     * 减少用户积分并记录类型和描述
     */
    boolean reducePoints(Long userId, Integer points, String type, String description);

    /**
     * 初始化用户积分
     */
    void initUserPoints(Long userId);
}
