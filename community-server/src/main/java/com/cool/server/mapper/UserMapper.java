package com.cool.server.mapper;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.entity.User;
import com.cool.pojo.vo.UserVO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM user WHERE id = #{id} AND deleted = 0")
    User getById(Long id);

    @Select("SELECT * FROM user WHERE username = #{username} AND deleted = 0")
    User getByUsername(String username);

    @Insert("INSERT INTO user (username, password, nickname, avatar, email, phone, bio, gender, status, role, create_time, update_time, deleted) " +
            "VALUES (#{username}, #{password}, #{nickname}, #{avatar}, #{email}, #{phone}, #{bio}, #{gender}, #{status}, #{role}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Update("<script>" +
            "UPDATE user SET update_time = NOW() " +
            "<if test='nickname != null'>, nickname = #{nickname}</if>" +
            "<if test='avatar != null'>, avatar = #{avatar}</if>" +
            "<if test='email != null'>, email = #{email}</if>" +
            "<if test='phone != null'>, phone = #{phone}</if>" +
            "<if test='bio != null'>, bio = #{bio}</if>" +
            "<if test='gender != null'>, gender = #{gender}</if>" +
            "<if test='status != null'>, status = #{status}</if>" +
            "<if test='role != null'>, role = #{role}</if>" +
            " WHERE id = #{id}" +
            "</script>")
    void update(User user);

    @Insert("INSERT INTO follow (user_id, follow_id, type, create_time, update_time, deleted) " +
            "VALUES (#{userId}, #{followId}, #{type}, NOW(), NOW(), 0)")
    void insertFollow(@Param("userId") Long userId, @Param("followId") Long followId, @Param("type") Integer type);

    @Delete("DELETE FROM follow WHERE user_id = #{userId} AND follow_id = #{followId} AND type = #{type}")
    void deleteFollow(@Param("userId") Long userId, @Param("followId") Long followId, @Param("type") Integer type);

    Long count(PageQueryDTO dto);

    List<UserVO> list(PageQueryDTO dto);

    @Delete("UPDATE user SET deleted = 1, update_time = NOW() WHERE id = #{id}")
    void deleteById(Long id);
    
    @Select("SELECT COUNT(*) FROM follow WHERE user_id = #{userId} AND type = #{type} AND deleted = 0")
    Long countFollows(@Param("userId") Long userId, @Param("type") Integer type);
    
    @Select("SELECT COUNT(*) FROM follow WHERE user_id = #{userId} AND follow_id = #{followId} AND type = #{type} AND deleted = 0")
    Integer checkFollow(@Param("userId") Long userId, @Param("followId") Long followId, @Param("type") Integer type);
    
    List<UserVO> getFollowingList(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    List<UserVO> getFollowerList(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    @Update("UPDATE user SET following_count = following_count + 1 WHERE id = #{userId}")
    void incrementFollowingCount(Long userId);
    
    @Update("UPDATE user SET following_count = GREATEST(following_count - 1, 0) WHERE id = #{userId}")
    void decrementFollowingCount(Long userId);
    
    @Update("UPDATE user SET follower_count = follower_count + 1 WHERE id = #{userId}")
    void incrementFollowerCount(Long userId);
    
    @Update("UPDATE user SET follower_count = GREATEST(follower_count - 1, 0) WHERE id = #{userId}")
    void decrementFollowerCount(Long userId);
    
    @Select("SELECT following_count FROM user WHERE id = #{userId}")
    Long getFollowingCount(Long userId);
    
    @Select("SELECT follower_count FROM user WHERE id = #{userId}")
    Long getFollowerCount(Long userId);

    List<UserVO> getRecommendedUsers(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM user WHERE id != #{userId} AND deleted = 0 AND id NOT IN (SELECT follow_id FROM follow WHERE user_id = #{userId} AND type = 0 AND deleted = 0)")
    Long countRecommendedUsers(@Param("userId") Long userId);

    @Update("UPDATE user SET post_count=post_count+1 where id=#{userId}")
    void addPostCount(Long userId);


}
