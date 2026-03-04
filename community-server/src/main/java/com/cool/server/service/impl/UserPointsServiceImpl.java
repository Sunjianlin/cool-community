package com.cool.server.service.impl;

import com.cool.pojo.entity.UserPoints;
import com.cool.pojo.entity.PointsTransaction;
import com.cool.server.context.BaseContext;
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
    public Integer getUserPoints() {
        Long userId = BaseContext.getCurrentId();
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        if (userPoints == null) {
            initUserPoints();
            return 0;
        }
        return userPoints.getPoints();
    }

    @Override
    @Transactional
    public void addPoints(Integer points, Integer type) {
        Long userId = BaseContext.getCurrentId();
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        if (userPoints == null) {
            initUserPoints();
            userPoints = userPointsMapper.selectByUserId(userId);
        }
        
        // 乐观锁重试机制
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;
        
        while (!success && retryCount < maxRetries) {
            int updated = userPointsMapper.addPoints(userId, points, userPoints.getVersion());
            if (updated > 0) {
                success = true;
                // 记录积分变动
                PointsTransaction transaction = new PointsTransaction();
                transaction.setUserId(userId);
                transaction.setPoints(points);
                transaction.setType(type);
                userPointsMapper.insertPointsTransaction(transaction);
            } else {
                // 重试前重新获取最新数据
                userPoints = userPointsMapper.selectByUserId(userId);
                retryCount++;
            }
        }
    }

    @Override
    @Transactional
    public boolean reducePoints(Integer points, Integer type) {
        Long userId = BaseContext.getCurrentId();
        UserPoints userPoints = userPointsMapper.selectByUserId(userId);
        if (userPoints == null || userPoints.getPoints() < points) {
            return false;
        }
        
        // 乐观锁重试机制
        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;
        
        while (!success && retryCount < maxRetries) {
            int updated = userPointsMapper.reducePoints(userId, points, userPoints.getVersion());
            if (updated > 0) {
                success = true;
                // 记录积分变动
                PointsTransaction transaction = new PointsTransaction();
                transaction.setUserId(userId);
                transaction.setPoints(-points); // 负值表示减少
                transaction.setType(type);
                userPointsMapper.insertPointsTransaction(transaction);
            } else {
                // 重试前重新获取最新数据
                userPoints = userPointsMapper.selectByUserId(userId);
                if (userPoints == null || userPoints.getPoints() < points) {
                    return false;
                }
                retryCount++;
            }
        }
        
        return success;
    }

    @Override
    @Transactional
    public void initUserPoints() {
        Long userId = BaseContext.getCurrentId();
        UserPoints userPoints = new UserPoints();
        userPoints.setUserId(userId);
        userPoints.setPoints(0);
        userPoints.setVersion(0);
        userPointsMapper.insert(userPoints);
    }
}
