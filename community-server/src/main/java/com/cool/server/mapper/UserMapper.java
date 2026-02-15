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
}
