package com.cool.server.mapper;

import com.cool.pojo.entity.PostCommentNotification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostCommentNotificationMapper {

    @Select("SELECT * FROM post_comment_notification WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<PostCommentNotification> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM post_comment_notification WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}")
    List<PostCommentNotification> selectByUserIdWithPaging(@Param("userId") Long userId, 
            @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM post_comment_notification WHERE user_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM post_comment_notification WHERE user_id = #{userId} AND is_read = 0")
    Long countUnreadByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO post_comment_notification (user_id, post_id, post_title, commenter_id, " +
            "commenter_name, commenter_avatar, content, is_read, create_time) " +
            "VALUES (#{userId}, #{postId}, #{postTitle}, #{commenterId}, " +
            "#{commenterName}, #{commenterAvatar}, #{content}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PostCommentNotification notification);

    @Update("UPDATE post_comment_notification SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE post_comment_notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);

    @Select("SELECT * FROM post_comment_notification WHERE id = #{id}")
    PostCommentNotification selectById(@Param("id") Long id);
}
