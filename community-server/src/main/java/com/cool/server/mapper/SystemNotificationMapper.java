package com.cool.server.mapper;

import com.cool.pojo.entity.SystemNotification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SystemNotificationMapper {

    @Select("<script>SELECT * FROM system_notification WHERE (user_id = #{userId} OR user_id = 0) ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}</script>")
    List<SystemNotification> selectByUserIdWithPaging(@Param("userId") Long userId, 
            @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Select("<script>SELECT COUNT(*) FROM system_notification WHERE (user_id = #{userId} OR user_id = 0)</script>")
    Long countByUserId(@Param("userId") Long userId);

    @Select("<script>SELECT COUNT(*) FROM system_notification WHERE (user_id = #{userId} OR user_id = 0) AND is_read = 0</script>")
    Long countUnreadByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO system_notification (user_id, title, content, type, is_read, create_time) " +
            "VALUES (#{userId}, #{title}, #{content}, #{type}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SystemNotification notification);

    @Update("UPDATE system_notification SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE system_notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);

    @Select("SELECT * FROM system_notification WHERE id = #{id}")
    SystemNotification selectById(@Param("id") Long id);
}
