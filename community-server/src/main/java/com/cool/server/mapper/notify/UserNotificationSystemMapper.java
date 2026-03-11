package com.cool.server.mapper.notify;

import com.cool.pojo.entity.notify.UserNotificationSystem;
import com.cool.pojo.vo.SystemNotificationVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserNotificationSystemMapper {

    @Select("SELECT * FROM user_notification_system WHERE notification_id = #{notificationId}")
    UserNotificationSystem selectByNotificationId(@Param("notificationId") Long notificationId);

    @Insert("INSERT INTO user_notification_system (notification_id, system_msg_type, related_business_id, jump_url) " +
            "VALUES (#{notificationId}, #{systemMsgType}, #{relatedBusinessId}, #{jumpUrl})")
    int insert(UserNotificationSystem system);

    @Select("SELECT m.id, m.content, m.read_status as is_read, m.create_time, " +
            "s.system_msg_type as type " +
            "FROM user_notification_main m " +
            "INNER JOIN user_notification_system s ON m.id = s.notification_id " +
            "WHERE m.receiver_id = #{receiverId} AND m.notify_type = 'SYSTEM' AND m.is_deleted = 0 " +
            "ORDER BY m.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<SystemNotificationVO> selectSystemNotifications(
            @Param("receiverId") Long receiverId, 
            @Param("offset") Integer offset, 
            @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM user_notification_main m " +
            "INNER JOIN user_notification_system s ON m.id = s.notification_id " +
            "WHERE m.receiver_id = #{receiverId} AND m.notify_type = 'SYSTEM' " +
            "AND m.read_status = 0 AND m.is_deleted = 0")
    Long countUnread(@Param("receiverId") Long receiverId);

    @Update("UPDATE user_notification_main m " +
            "INNER JOIN user_notification_system s ON m.id = s.notification_id " +
            "SET m.read_status = 1, m.update_time = NOW() " +
            "WHERE m.id = #{id} AND m.receiver_id = #{receiverId}")
    int markAsRead(@Param("id") Long id, @Param("receiverId") Long receiverId);

    @Update("UPDATE user_notification_main m " +
            "INNER JOIN user_notification_system s ON m.id = s.notification_id " +
            "SET m.read_status = 1, m.update_time = NOW() " +
            "WHERE m.receiver_id = #{receiverId} AND m.read_status = 0")
    int markAllAsRead(@Param("receiverId") Long receiverId);
}
