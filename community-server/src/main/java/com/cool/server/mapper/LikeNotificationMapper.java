package com.cool.server.mapper;

import com.cool.pojo.entity.LikeNotification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LikeNotificationMapper {

    @Select("SELECT * FROM like_notification WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}")
    List<LikeNotification> selectByUserIdWithPaging(@Param("userId") Long userId, 
            @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM like_notification WHERE user_id = #{userId} AND is_read = 0")
    Long countUnreadByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO like_notification (user_id, post_id, post_title, liker_id, liker_name, liker_avatar, is_read, create_time) " +
            "VALUES (#{userId}, #{postId}, #{postTitle}, #{likerId}, #{likerName}, #{likerAvatar}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LikeNotification notification);

    @Update("UPDATE like_notification SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE like_notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);
}
