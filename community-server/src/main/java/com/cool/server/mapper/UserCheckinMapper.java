package com.cool.server.mapper;

import com.cool.pojo.entity.UserCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * 用户签到Mapper
 */
@Mapper
public interface UserCheckinMapper {

    /**
     * 根据用户ID和日期查询签到记录
     */
    UserCheckin selectByUserIdAndDate(@Param("userId") Long userId, @Param("checkinDate") LocalDate checkinDate);

    /**
     * 插入签到记录
     */
    void insert(UserCheckin userCheckin);

    /**
     * 查询用户连续签到天数
     */
    Integer countConsecutiveCheckins(@Param("userId") Long userId);
}
