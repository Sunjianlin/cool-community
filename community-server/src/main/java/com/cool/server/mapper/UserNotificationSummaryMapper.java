package com.cool.server.mapper;

import com.cool.pojo.entity.UserNotificationSummary;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserNotificationSummaryMapper {

    @Select("SELECT * FROM user_notification_summary WHERE user_id = #{userId}")
    UserNotificationSummary selectByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO user_notification_summary (user_id, unread_private, unread_comment, " +
            "unread_like, unread_follow, unread_system, total_unread, update_time) " +
            "VALUES (#{userId}, 0, 0, 0, 0, 0, 0, NOW()) " +
            "ON DUPLICATE KEY UPDATE user_id = user_id")
    int insertOrIgnore(@Param("userId") Long userId);

    @Update("UPDATE user_notification_summary SET " +
            "unread_private = #{unreadPrivate}, " +
            "unread_comment = #{unreadComment}, " +
            "unread_like = #{unreadLike}, " +
            "unread_follow = #{unreadFollow}, " +
            "unread_system = #{unreadSystem}, " +
            "total_unread = #{totalUnread}, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int updateByUserId(UserNotificationSummary summary);

    @Update("UPDATE user_notification_summary SET " +
            "unread_comment = unread_comment + 1, " +
            "total_unread = total_unread + 1, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int incrementComment(@Param("userId") Long userId);

    @Update("UPDATE user_notification_summary SET " +
            "unread_like = unread_like + 1, " +
            "total_unread = total_unread + 1, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int incrementLike(@Param("userId") Long userId);

    @Update("UPDATE user_notification_summary SET " +
            "unread_follow = unread_follow + 1, " +
            "total_unread = total_unread + 1, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int incrementFollow(@Param("userId") Long userId);

    @Update("UPDATE user_notification_summary SET " +
            "unread_system = unread_system + 1, " +
            "total_unread = total_unread + 1, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int incrementSystem(@Param("userId") Long userId);
}
