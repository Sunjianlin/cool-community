package com.cool.server.mapper;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.MessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MessageMapper {
    
    void insert(com.cool.pojo.entity.Message message);
    
    MessageVO getById(Long id);
    
    List<MessageVO> list(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    Long count(@Param("userId") Long userId);
    
    void markAsRead(Long id);
    
    void markAllAsRead(Long userId);
    
    void deleteById(Long id);
    
    Long countUnread(Long userId);
    
    List<MessageVO> getConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2, 
                                     @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    Long countConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    List<MessageVO> getConversationList(@Param("userId") Long userId);
    
    void incrementUnreadCount(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
    
    void clearUnreadCount(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
}
