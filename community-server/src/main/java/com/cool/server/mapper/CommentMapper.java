package com.cool.server.mapper;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.entity.Comment;
import com.cool.pojo.vo.CommentVO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CommentMapper {
    
    @Select("SELECT * FROM comment WHERE id = #{id} AND deleted = 0")
    Comment getById(Long id);

    @Insert("INSERT INTO comment (post_id, user_id, parent_id, reply_user_id, content, like_count, status, create_time, update_time, deleted) " +
            "VALUES (#{postId}, #{userId}, #{parentId}, #{replyUserId}, #{content}, #{likeCount}, #{status}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Comment comment);

    @Delete("UPDATE comment SET deleted = 1, update_time = NOW() WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT COUNT(*) FROM comment WHERE post_id = #{postId} AND deleted = 0")
    Long countByPostId(Long postId);

    List<CommentVO> listByPostId(@Param("postId") Long postId, PageQueryDTO pageQueryDTO);

    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(Long id);

    @Update("UPDATE comment SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
    void decrementLikeCount(Long id);
}
