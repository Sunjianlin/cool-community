package com.cool.server.service.impl;

import com.cool.pojo.entity.UserPoints;
import com.cool.server.mapper.UserPointsMapper;
import com.cool.server.service.UserPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户积分服务实现类
 */
@Service
public class UserPointsServiceImpl implements UserPointsService {

    @Autowired
    private UserPointsMapper userPointsMapper;

    @Override
    public Integer getUserPoints(Long userId) {
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        if (userPoints == null) {
            initUserPoints(userId);
            return 0;
        }
        return userPoints.getPoints();
    }

    @Override
    @Transactional
    public void addPoints(Long userId, Integer points) {
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        if (userPoints == null) {
            initUserPoints(userId);
        }
        userPointsMapper.addPoints(userId, points);
    }

    @Override
    @Transactional
    public void addPoints(Long userId, Integer points, String type, String description) {
        // 更新用户积分
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        if (userPoints == null) {
            initUserPoints(userId);
        }
        userPointsMapper.addPoints(userId, points);
        
        // 记录积分变动
        UserPoints pointsRecord = new UserPoints();
        pointsRecord.setUserId(userId);
        pointsRecord.setPoints(points);
        pointsRecord.setType(type);
        pointsRecord.setDescription(description);
        userPointsMapper.insertPointsRecord(pointsRecord);
    }

    @Override
    @Transactional
    public boolean reducePoints(Long userId, Integer points) {
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        if (userPoints == null || userPoints.getPoints() < points) {
            return false;
        }
        userPointsMapper.reducePoints(userId, points);
        return true;
    }

    @Override
    @Transactional
    public boolean reducePoints(Long userId, Integer points, String type, String description) {
        // 检查积分是否足够
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        if (userPoints == null || userPoints.getPoints() < points) {
            return false;
        }
        
        // 减少用户积分
        userPointsMapper.reducePoints(userId, points);
        
        // 记录积分变动
        UserPoints pointsRecord = new UserPoints();
        pointsRecord.setUserId(userId);
        pointsRecord.setPoints(-points); // 负值表示减少
        pointsRecord.setType(type);
        pointsRecord.setDescription(description);
        userPointsMapper.insertPointsRecord(pointsRecord);
        
        return true;
    }

    @Override
    @Transactional
    public void initUserPoints(Long userId) {
        UserPoints userPoints = new UserPoints();
        userPoints.setUserId(userId);
        userPoints.setPoints(0);
        userPoints.setType("INIT");
        userPoints.setDescription("初始化积分");
        userPointsMapper.insert(userPoints);
    }
}
