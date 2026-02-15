package com.cool.server.mapper;

import com.cool.pojo.entity.Brand;
import com.cool.pojo.vo.BrandVO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BrandMapper {
    
    @Select("SELECT * FROM brand WHERE id = #{id} AND deleted = 0")
    Brand getEntityById(Long id);

    @Select("SELECT id, name, logo, description FROM brand WHERE id = #{id} AND deleted = 0")
    BrandVO getById(Long id);

    @Insert("INSERT INTO brand (name, logo, description, create_time, update_time, deleted) " +
            "VALUES (#{name}, #{logo}, #{description}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Brand brand);

    @Update("<script>" +
            "UPDATE brand SET update_time = NOW() " +
            "<if test='name != null'>, name = #{name}</if>" +
            "<if test='logo != null'>, logo = #{logo}</if>" +
            "<if test='description != null'>, description = #{description}</if>" +
            " WHERE id = #{id}" +
            "</script>")
    void update(Brand brand);

    @Delete("UPDATE brand SET deleted = 1, update_time = NOW() WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT id, name, logo, description FROM brand WHERE deleted = 0 ORDER BY name")
    List<BrandVO> listAll();
}
