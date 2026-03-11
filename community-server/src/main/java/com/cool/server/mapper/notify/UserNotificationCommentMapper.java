package com.cool.server.mapper.notify;

import com.cool.pojo.entity.notify.UserNotificationComment;
import com.cool.pojo.vo.CommentReplyNotificationVO;
import com.cool.pojo.vo.PostCommentNotificationVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserNotificationCommentMapper {

    @Select("SELECT * FROM user_notification_comment WHERE notification_id = #{notificationId}")
    UserNotificationComment selectByNotificationId(@Param("notificationId") Long notificationId);

    @Insert("INSERT INTO user_notification_comment (notification_id, post_id, comment_id, comment_type, jump_url) " +
            "VALUES (#{notificationId}, #{postId}, #{commentId}, #{commentType}, #{jumpUrl})")
    int insert(UserNotificationComment comment);

    @Select("SELECT m.id, c.post_id, c.comment_id, m.sender_id as commenter_id, " +
            "u.nickname as commenter_name, u.avatar as commenter_avatar, " +
            "m.content, m.read_status as is_read, m.create_time, c.comment_type as type, " +
            "p.title as post_title " +
            "FROM user_notification_main m " +
            "INNER JOIN user_notification_comment c ON m.id = c.notification_id " +
            "LEFT JOIN user u ON m.sender_id = u.id " +
            "LEFT JOIN post p ON c.post_id = p.id " +
            "WHERE m.receiver_id = #{receiverId} AND m.notify_type = 'COMMENT' " +
            "AND c.comment_type = 'COMMENT_REPLY' AND m.is_deleted = 0 " +
            "ORDER BY m.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<CommentReplyNotificationVO> selectCommentReplyNotifications(
            @Param("receiverId") Long receiverId, 
            @Param("offset") Integer offset, 
            @Param("pageSize") Integer pageSize);

    @Select("SELECT m.id, c.post_id, m.sender_id as commenter_id, " +
            "u.nickname as commenter_name, u.avatar as commenter_avatar, " +
            "m.content, m.read_status as is_read, m.create_time, c.comment_type as type, " +
            "p.title as post_title " +
            "FROM user_notification_main m " +
            "INNER JOIN user_notification_comment c ON m.id = c.notification_id " +
            "LEFT JOIN user u ON m.sender_id = u.id " +
            "LEFT JOIN post p ON c.post_id = p.id " +
            "WHERE m.receiver_id = #{receiverId} AND m.notify_type = 'COMMENT' " +
            "AND c.comment_type = 'POST_COMMENT' AND m.is_deleted = 0 " +
            "ORDER BY m.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<PostCommentNotificationVO> selectPostCommentNotifications(
            @Param("receiverId") Long receiverId, 
            @Param("offset") Integer offset, 
            @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM user_notification_main m " +
            "INNER JOIN user_notification_comment c ON m.id = c.notification_id " +
            "WHERE m.receiver_id = #{receiverId} AND m.notify_type = 'COMMENT' " +
            "AND c.comment_type = 'COMMENT_REPLY' AND m.read_status = 0 AND m.is_deleted = 0")
    Long countUnreadCommentReply(@Param("receiverId") Long receiverId);

    @Select("SELECT COUNT(*) FROM user_notification_main m " +
            "INNER JOIN user_notification_comment c ON m.id = c.notification_id " +
            "WHERE m.receiver_id = #{receiverId} AND m.notify_type = 'COMMENT' " +
            "AND c.comment_type = 'POST_COMMENT' AND m.read_status = 0 AND m.is_deleted = 0")
    Long countUnreadPostComment(@Param("receiverId") Long receiverId);

    @Update("UPDATE user_notification_main m " +
            "INNER JOIN user_notification_comment c ON m.id = c.notification_id " +
            "SET m.read_status = 1, m.update_time = NOW() " +
            "WHERE m.id = #{id} AND m.receiver_id = #{receiverId} AND c.comment_type = 'COMMENT_REPLY'")
    int markCommentReplyAsRead(@Param("id") Long id, @Param("receiverId") Long receiverId);

    @Update("UPDATE user_notification_main m " +
            "INNER JOIN user_notification_comment c ON m.id = c.notification_id " +
            "SET m.read_status = 1, m.update_time = NOW() " +
            "WHERE m.id = #{id} AND m.receiver_id = #{receiverId} AND c.comment_type = 'POST_COMMENT'")
    int markPostCommentAsRead(@Param("id") Long id, @Param("receiverId") Long receiverId);

    @Update("UPDATE user_notification_main m " +
            "INNER JOIN user_notification_comment c ON m.id = c.notification_id " +
            "SET m.read_status = 1, m.update_time = NOW() " +
            "WHERE m.receiver_id = #{receiverId} AND c.comment_type = 'COMMENT_REPLY' AND m.read_status = 0")
    int markAllCommentReplyAsRead(@Param("receiverId") Long receiverId);

    @Update("UPDATE user_notification_main m " +
            "INNER JOIN user_notification_comment c ON m.id = c.notification_id " +
            "SET m.read_status = 1, m.update_time = NOW() " +
            "WHERE m.receiver_id = #{receiverId} AND c.comment_type = 'POST_COMMENT' AND m.read_status = 0")
    int markAllPostCommentAsRead(@Param("receiverId") Long receiverId);
}
