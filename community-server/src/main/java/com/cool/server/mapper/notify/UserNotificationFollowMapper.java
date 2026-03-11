package com.cool.server.mapper.notify;

import com.cool.pojo.entity.notify.UserNotificationFollow;
import com.cool.pojo.vo.FollowNotificationVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserNotificationFollowMapper {

    @Select("SELECT * FROM user_notification_follow WHERE notification_id = #{notificationId}")
    UserNotificationFollow selectByNotificationId(@Param("notificationId") Long notificationId);

    @Insert("INSERT INTO user_notification_follow (notification_id, followed_user_id, follower_user_id, jump_url) " +
            "VALUES (#{notificationId}, #{followedUserId}, #{followerUserId}, #{jumpUrl})")
    int insert(UserNotificationFollow follow);

    @Select("SELECT m.id, f.follower_user_id as follower_id, " +
            "u.nickname as follower_name, u.avatar as follower_avatar, " +
            "m.read_status as is_read, m.create_time " +
            "FROM user_notification_main m " +
            "INNER JOIN user_notification_follow f ON m.id = f.notification_id " +
            "LEFT JOIN user u ON f.follower_user_id = u.id " +
            "WHERE m.receiver_id = #{receiverId} AND m.notify_type = 'FOLLOW' AND m.is_deleted = 0 " +
            "ORDER BY m.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<FollowNotificationVO> selectFollowNotifications(
            @Param("receiverId") Long receiverId, 
            @Param("offset") Integer offset, 
            @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM user_notification_main m " +
            "INNER JOIN user_notification_follow f ON m.id = f.notification_id " +
            "WHERE m.receiver_id = #{receiverId} AND m.notify_type = 'FOLLOW' " +
            "AND m.read_status = 0 AND m.is_deleted = 0")
    Long countUnread(@Param("receiverId") Long receiverId);

    @Update("UPDATE user_notification_main m " +
            "INNER JOIN user_notification_follow f ON m.id = f.notification_id " +
            "SET m.read_status = 1, m.update_time = NOW() " +
            "WHERE m.id = #{id} AND m.receiver_id = #{receiverId}")
    int markAsRead(@Param("id") Long id, @Param("receiverId") Long receiverId);

    @Update("UPDATE user_notification_main m " +
            "INNER JOIN user_notification_follow f ON m.id = f.notification_id " +
            "SET m.read_status = 1, m.update_time = NOW() " +
            "WHERE m.receiver_id = #{receiverId} AND m.read_status = 0")
    int markAllAsRead(@Param("receiverId") Long receiverId);
}
