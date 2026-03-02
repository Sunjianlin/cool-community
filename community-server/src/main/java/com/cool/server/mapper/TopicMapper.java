package com.cool.server.mapper;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.entity.Topic;
import com.cool.pojo.vo.TopicVO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface TopicMapper {
    
    @Select("SELECT * FROM topic WHERE id = #{id} AND deleted = 0")
    Topic getById(Long id);

    @Insert("INSERT INTO topic (name, description, icon, cover, category, follow_count, post_count, status, is_hot, create_time, update_time, deleted) " +
            "VALUES (#{name}, #{description}, #{icon}, #{cover}, #{category}, #{followCount}, #{postCount}, #{status}, #{isHot}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Topic topic);

    @Update("<script>" +
            "UPDATE topic SET update_time = NOW() " +
            "<if test='name != null'>, name = #{name}</if>" +
            "<if test='description != null'>, description = #{description}</if>" +
            "<if test='icon != null'>, icon = #{icon}</if>" +
            "<if test='cover != null'>, cover = #{cover}</if>" +
            "<if test='category != null'>, category = #{category}</if>" +
            "<if test='isHot != null'>, is_hot = #{isHot}</if>" +
            " WHERE id = #{id}" +
            "</script>")
    void update(Topic topic);

    @Delete("UPDATE topic SET deleted = 1, update_time = NOW() WHERE id = #{id}")
    void deleteById(Long id);

    Long count(PageQueryDTO dto);

    List<TopicVO> list(PageQueryDTO dto);

    List<TopicVO> listHot(PageQueryDTO dto);

    @Select("SELECT COUNT(*) FROM topic WHERE is_hot = 1 AND deleted = 0")
    Long countHot();

    TopicVO getDetailById(Long id);

    @Update("UPDATE topic SET follow_count = follow_count + 1 WHERE id = #{id}")
    void incrementFollowCount(Long id);

    @Update("UPDATE topic SET follow_count = follow_count - 1 WHERE id = #{id} AND follow_count > 0")
    void decrementFollowCount(Long id);

    List<TopicVO> getRecommendedTopics(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM topic WHERE deleted = 0 AND id NOT IN (SELECT follow_id FROM follow WHERE user_id = #{userId} AND type = 1 AND deleted = 0)")
    Long countRecommendedTopics(@Param("userId") Long userId);
}
