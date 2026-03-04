package com.cool.server.service.impl;

import com.cool.common.constant.PointsConstant;
import com.cool.pojo.entity.UserCheckin;
import com.cool.server.context.BaseContext;
import com.cool.server.mapper.UserCheckinMapper;
import com.cool.server.service.UserCheckinService;
import com.cool.server.service.UserPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 用户签到服务实现类
 */
@Service
public class UserCheckinServiceImpl implements UserCheckinService {

    @Autowired
    private UserCheckinMapper userCheckinMapper;

    @Autowired
    private UserPointsService userPointsService;

    // 签到积分规则
    private static final int BASE_CHECKIN_POINTS = 10; // 基础签到积分
    private static final int CONSECUTIVE_BONUS_3_DAYS = 5; // 连续3天额外奖励
    private static final int CONSECUTIVE_BONUS_7_DAYS = 10; // 连续7天额外奖励
    private static final int CONSECUTIVE_BONUS_30_DAYS = 20; // 连续30天额外奖励

    @Override
    public boolean hasCheckedInToday() {
        LocalDate today = LocalDate.now();
        return hasCheckedInOnDate(today);
    }

    @Override
    @Transactional
    public int checkin() {
        Long userId = BaseContext.getCurrentId();
        LocalDate today = LocalDate.now();
        
        // 检查今天是否已签到
        if (hasCheckedInOnDate(today)) {
            return 0; // 已签到，返回0积分
        }

        // 计算连续签到天数
        int consecutiveDays = getConsecutiveCheckinDays();
        int pointsEarned = calculateCheckinPoints(consecutiveDays + 1);

        // 保存签到记录
        UserCheckin checkin = new UserCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setPointsEarned(pointsEarned);
        userCheckinMapper.insert(checkin);

        // 增加用户积分
        userPointsService.addPoints(pointsEarned, PointsConstant.TYPE_CHECKIN);

        return pointsEarned;
    }

    @Override
    public int getConsecutiveCheckinDays() {
        Long userId = BaseContext.getCurrentId();
        Integer count = userCheckinMapper.countConsecutiveCheckins(userId);
        return count != null ? count : 0;
    }

    @Override
    public boolean hasCheckedInOnDate( LocalDate date) {
        Long userId = BaseContext.getCurrentId();
        UserCheckin checkin = userCheckinMapper.selectByUserIdAndDate(userId, date);
        return checkin != null;
    }

    /**
     * 计算签到积分
     */
    private int calculateCheckinPoints(int consecutiveDays) {
        int points = BASE_CHECKIN_POINTS;
        
        if (consecutiveDays % 30 == 0) {
            points += CONSECUTIVE_BONUS_30_DAYS;
        } else if (consecutiveDays % 7 == 0) {
            points += CONSECUTIVE_BONUS_7_DAYS;
        } else if (consecutiveDays % 3 == 0) {
            points += CONSECUTIVE_BONUS_3_DAYS;
        }
        
        return points;
    }
}
