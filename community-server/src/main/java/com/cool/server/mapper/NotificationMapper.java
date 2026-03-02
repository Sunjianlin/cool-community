package com.cool.server.mapper;

import com.cool.pojo.entity.Notification;
import com.cool.pojo.vo.NotificationVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {
    
    @Insert("INSERT INTO notification (user_id, type, content, is_read, related_id, create_time, update_time, deleted) " +
            "VALUES (#{userId}, #{type}, #{content}, #{isRead}, #{relatedId}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Notification notification);
    
    @Update("UPDATE notification SET is_read = 1, update_time = NOW() WHERE id = #{id}")
    void markAsRead(Long id);
    
    @Update("UPDATE notification SET is_read = 1, update_time = NOW() WHERE user_id = #{userId}")
    void markAllAsRead(Long userId);
    
    @Select("SELECT * FROM notification WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<Notification> getByUserId(Long userId);
    
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0")
    Integer countUnread(Long userId);
}
