package com.cool.server.mapper;

import com.cool.pojo.entity.FollowNotification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FollowNotificationMapper {

    @Select("SELECT * FROM follow_notification WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}")
    List<FollowNotification> selectByUserIdWithPaging(@Param("userId") Long userId, 
            @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Select("SELECT COUNT(*) FROM follow_notification WHERE user_id = #{userId} AND is_read = 0")
    Long countUnreadByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO follow_notification (user_id, follower_id, follower_name, follower_avatar, is_read, create_time) " +
            "VALUES (#{userId}, #{followerId}, #{followerName}, #{followerAvatar}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FollowNotification notification);

    @Update("UPDATE follow_notification SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE follow_notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);
}
