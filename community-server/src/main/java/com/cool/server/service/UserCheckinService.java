package com.cool.server.service;

import java.time.LocalDate;

/**
 * 用户签到服务接口
 */
public interface UserCheckinService {

    /**
     * 检查用户今天是否已签到
     */
    boolean hasCheckedInToday();

    /**
     * 用户签到
     */
    int checkin();

    /**
     * 获取用户连续签到天数
     */
    int getConsecutiveCheckinDays();

    /**
     * 获取用户在指定日期是否签到
     */
    boolean hasCheckedInOnDate(LocalDate date);
}
