package com.cool.server.mapper.notify;

import com.cool.pojo.entity.notify.UserNotificationMain;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserNotificationMainMapper {

    @Select("SELECT * FROM user_notification_main WHERE receiver_id = #{receiverId} AND is_deleted = 0 ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}")
    List<UserNotificationMain> selectByReceiverId(@Param("receiverId") Long receiverId, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Select("SELECT * FROM user_notification_main WHERE receiver_id = #{receiverId} AND notify_type = #{notifyType} AND is_deleted = 0 ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}")
    List<UserNotificationMain> selectByReceiverIdAndType(@Param("receiverId") Long receiverId, @Param("notifyType") String notifyType, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM user_notification_main WHERE receiver_id = #{receiverId} AND read_status = 0 AND is_deleted = 0")
    Long countUnreadByReceiverId(@Param("receiverId") Long receiverId);

    @Select("SELECT COUNT(*) FROM user_notification_main WHERE receiver_id = #{receiverId} AND notify_type = #{notifyType} AND read_status = 0 AND is_deleted = 0")
    Long countUnreadByReceiverIdAndType(@Param("receiverId") Long receiverId, @Param("notifyType") String notifyType);

    @Insert("INSERT INTO user_notification_main (message_id, receiver_id, sender_id, notify_type, content, read_status, is_deleted, create_time, update_time) " +
            "VALUES (#{messageId}, #{receiverId}, #{senderId}, #{notifyType}, #{content}, 0, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserNotificationMain notification);

    @Update("UPDATE user_notification_main SET read_status = 1, update_time = NOW() WHERE id = #{id} AND receiver_id = #{receiverId}")
    int markAsRead(@Param("id") Long id, @Param("receiverId") Long receiverId);

    @Update("UPDATE user_notification_main SET read_status = 1, update_time = NOW() WHERE receiver_id = #{receiverId} AND read_status = 0 AND is_deleted = 0")
    int markAllAsRead(@Param("receiverId") Long receiverId);

    @Update("UPDATE user_notification_main SET read_status = 1, update_time = NOW() WHERE receiver_id = #{receiverId} AND notify_type = #{notifyType} AND read_status = 0 AND is_deleted = 0")
    int markAllAsReadByType(@Param("receiverId") Long receiverId, @Param("notifyType") String notifyType);

    @Update("UPDATE user_notification_main SET is_deleted = 1, update_time = NOW() WHERE id = #{id} AND receiver_id = #{receiverId}")
    int softDelete(@Param("id") Long id, @Param("receiverId") Long receiverId);

    @Select("SELECT * FROM user_notification_main WHERE message_id = #{messageId}")
    UserNotificationMain selectByMessageId(@Param("messageId") String messageId);

    @Select("SELECT * FROM user_notification_main WHERE id = #{id}")
    UserNotificationMain selectById(@Param("id") Long id);
}
