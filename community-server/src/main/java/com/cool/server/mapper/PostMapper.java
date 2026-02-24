package com.cool.server.mapper;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.entity.Post;
import com.cool.pojo.vo.PostVO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PostMapper {
    
    @Select("SELECT * FROM post WHERE id = #{id} AND deleted = 0")
    Post getById(Long id);

    @Insert("INSERT INTO post (user_id, topic_id, product_id, title, content, images, type, like_count, comment_count, collect_count, view_count, status, is_top, is_essence, create_time, update_time, deleted) " +
            "VALUES (#{userId}, #{topicId}, #{productId}, #{title}, #{content}, #{images}, #{type}, #{likeCount}, #{commentCount}, #{collectCount}, #{viewCount}, #{status}, #{isTop}, #{isEssence}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Post post);

    @Delete("UPDATE post SET deleted = 1, update_time = NOW() WHERE id = #{id}")
    void deleteById(Long id);

    @Update("<script>" +
            "UPDATE post SET update_time = NOW() " +
            "<if test='title != null'>, title = #{title}</if>" +
            "<if test='content != null'>, content = #{content}</if>" +
            "<if test='images != null'>, images = #{images}</if>" +
            "<if test='type != null'>, type = #{type}</if>" +
            "<if test='productId != null'>, product_id = #{productId}</if>" +
            "<if test='status != null'>, status = #{status}</if>" +
            "<if test='isTop != null'>, is_top = #{isTop}</if>" +
            "<if test='isEssence != null'>, is_essence = #{isEssence}</if>" +
            " WHERE id = #{id}" +
            "</script>")
    void update(Post post);

    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);

    @Update("UPDATE post SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(Long id);

    @Update("UPDATE post SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
    void decrementLikeCount(Long id);

    @Update("UPDATE post SET collect_count = collect_count + 1 WHERE id = #{id}")
    void incrementCollectCount(Long id);

    @Update("UPDATE post SET collect_count = collect_count - 1 WHERE id = #{id} AND collect_count > 0")
    void decrementCollectCount(Long id);

    Long count(PageQueryDTO dto);

    List<PostVO> list(PageQueryDTO dto);

    PostVO getDetailById(Long id);
}
