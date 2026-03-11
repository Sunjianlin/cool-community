package com.cool.server.mapper.notify;

import com.cool.pojo.entity.notify.UserNotificationLike;
import com.cool.pojo.vo.LikeNotificationVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserNotificationLikeMapper {

    @Select("SELECT * FROM user_notification_like WHERE notification_id = #{notificationId}")
    UserNotificationLike selectByNotificationId(@Param("notificationId") Long notificationId);

    @Insert("INSERT INTO user_notification_like (notification_id, target_id, like_type, jump_url) " +
            "VALUES (#{notificationId}, #{targetId}, #{likeType}, #{jumpUrl})")
    int insert(UserNotificationLike like);

    @Select("SELECT m.id, l.target_id as post_id, m.sender_id as liker_id, " +
            "u.nickname as liker_name, u.avatar as liker_avatar, " +
            "m.read_status as is_read, m.create_time, l.like_type as type, " +
            "p.title as post_title " +
            "FROM user_notification_main m " +
            "INNER JOIN user_notification_like l ON m.id = l.notification_id " +
            "LEFT JOIN user u ON m.sender_id = u.id " +
            "LEFT JOIN post p ON l.target_id = p.id " +
            "WHERE m.receiver_id = #{receiverId} AND m.notify_type = 'LIKE' AND m.is_deleted = 0 " +
            "ORDER BY m.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<LikeNotificationVO> selectLikeNotifications(
            @Param("receiverId") Long receiverId, 
            @Param("offset") Integer offset, 
            @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM user_notification_main m " +
            "INNER JOIN user_notification_like l ON m.id = l.notification_id " +
            "WHERE m.receiver_id = #{receiverId} AND m.notify_type = 'LIKE' " +
            "AND m.read_status = 0 AND m.is_deleted = 0")
    Long countUnread(@Param("receiverId") Long receiverId);

    @Update("UPDATE user_notification_main m " +
            "INNER JOIN user_notification_like l ON m.id = l.notification_id " +
            "SET m.read_status = 1, m.update_time = NOW() " +
            "WHERE m.id = #{id} AND m.receiver_id = #{receiverId}")
    int markAsRead(@Param("id") Long id, @Param("receiverId") Long receiverId);

    @Update("UPDATE user_notification_main m " +
            "INNER JOIN user_notification_like l ON m.id = l.notification_id " +
            "SET m.read_status = 1, m.update_time = NOW() " +
            "WHERE m.receiver_id = #{receiverId} AND m.read_status = 0")
    int markAllAsRead(@Param("receiverId") Long receiverId);
}
