package com.cool.server.mapper;

import com.cool.pojo.entity.CommentReplyNotification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentReplyNotificationMapper {

    @Select("SELECT * FROM comment_reply_notification WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<CommentReplyNotification> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM comment_reply_notification WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}")
    List<CommentReplyNotification> selectByUserIdWithPaging(@Param("userId") Long userId, 
            @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM comment_reply_notification WHERE user_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM comment_reply_notification WHERE user_id = #{userId} AND is_read = 0")
    Long countUnreadByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO comment_reply_notification (user_id, comment_id, reply_id, post_id, post_title, " +
            "commenter_id, commenter_name, commenter_avatar, content, is_read, create_time) " +
            "VALUES (#{userId}, #{commentId}, #{replyId}, #{postId}, #{postTitle}, " +
            "#{commenterId}, #{commenterName}, #{commenterAvatar}, #{content}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CommentReplyNotification notification);

    @Update("UPDATE comment_reply_notification SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE comment_reply_notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);

    @Select("SELECT * FROM comment_reply_notification WHERE id = #{id}")
    CommentReplyNotification selectById(@Param("id") Long id);
}
