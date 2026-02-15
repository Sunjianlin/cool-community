package com.cool.server.mapper;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.entity.Message;
import com.cool.pojo.vo.MessageVO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MessageMapper {
    
    @Insert("INSERT INTO message (from_user_id, to_user_id, type, content, related_id, is_read, create_time, update_time, deleted) " +
            "VALUES (#{fromUserId}, #{toUserId}, #{type}, #{content}, #{relatedId}, #{isRead}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Message message);

    @Select("SELECT COUNT(*) FROM message WHERE to_user_id = #{userId} AND deleted = 0")
    Long count(Long userId);

    List<MessageVO> list(@Param("userId") Long userId, PageQueryDTO pageQueryDTO);

    @Select("SELECT COUNT(*) FROM message WHERE to_user_id = #{userId} AND is_read = 0 AND deleted = 0")
    Long countUnread(Long userId);

    @Update("UPDATE message SET is_read = 1, update_time = NOW() WHERE id = #{id}")
    void markAsRead(Long id);

    @Update("UPDATE message SET is_read = 1, update_time = NOW() WHERE to_user_id = #{userId} AND is_read = 0")
    void markAllAsRead(Long userId);

    @Delete("UPDATE message SET deleted = 1, update_time = NOW() WHERE id = #{id}")
    void deleteById(Long id);
}
