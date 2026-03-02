package com.cool.server.mapper;

import com.cool.pojo.vo.ProductVO;
import com.cool.pojo.vo.TopicVO;
import com.cool.pojo.vo.UserVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FollowMapper {
    
    /**
     * 插入关注关系
     */
    @Insert("INSERT INTO follow (user_id, follow_id, type, create_time, update_time, deleted) " +
            "VALUES (#{userId}, #{followId}, #{type}, NOW(), NOW(), 0)")
    void insertFollow(@Param("userId") Long userId, @Param("followId") Long followId, @Param("type") Integer type);
    
    /**
     * 删除关注关系
     */
    @Delete("DELETE FROM follow WHERE user_id = #{userId} AND follow_id = #{followId} AND type = #{type}")
    void deleteFollow(@Param("userId") Long userId, @Param("followId") Long followId, @Param("type") Integer type);
    
    /**
     * 检查是否已关注
     */
    @Select("SELECT COUNT(*) FROM follow WHERE user_id = #{userId} AND follow_id = #{followId} AND type = #{type} AND deleted = 0")
    Integer checkFollow(@Param("userId") Long userId, @Param("followId") Long followId, @Param("type") Integer type);
    
    /**
     * 统计关注数量
     */
    @Select("SELECT COUNT(*) FROM follow WHERE user_id = #{userId} AND type = #{type} AND deleted = 0")
    Long countFollows(@Param("userId") Long userId, @Param("type") Integer type);
    
    /**
     * 统计被关注数量
     */
    @Select("SELECT COUNT(*) FROM follow WHERE follow_id = #{targetId} AND type = #{type} AND deleted = 0")
    Long countFollowers(@Param("targetId") Long targetId, @Param("type") Integer type);
    
    /**
     * 获取关注用户列表
     */
    List<UserVO> getFollowingUserList(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    /**
     * 获取粉丝列表
     */
    List<UserVO> getFollowerList(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    /**
     * 获取关注话题列表
     */
    List<TopicVO> getFollowingTopicList(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    /**
     * 获取话题关注者列表
     */
    List<UserVO> getTopicFollowerList(@Param("topicId") Long topicId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    /**
     * 获取关注产品列表
     */
    List<ProductVO> getFollowingProductList(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    /**
     * 获取产品关注者列表
     */
    List<UserVO> getProductFollowerList(@Param("productId") Long productId, @Param("offset") int offset, @Param("pageSize") int pageSize);
}
