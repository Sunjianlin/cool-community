package com.cool.server.mapper;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.entity.Product;
import com.cool.pojo.vo.ProductVO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ProductMapper {
    
    @Select("SELECT * FROM product WHERE id = #{id} AND deleted = 0")
    Product getEntityById(Long id);

    @Insert("INSERT INTO product (name, description, image, brand, category_id, price, specs, review_count, status, create_time, update_time, deleted) " +
            "VALUES (#{name}, #{description}, #{image}, #{brand}, #{categoryId}, #{price}, #{specs}, #{reviewCount}, #{status}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Product product);

    @Update("<script>" +
            "UPDATE product SET update_time = NOW() " +
            "<if test='name != null'>, name = #{name}</if>" +
            "<if test='description != null'>, description = #{description}</if>" +
            "<if test='image != null'>, image = #{image}</if>" +
            "<if test='brand != null'>, brand = #{brand}</if>" +
            "<if test='categoryId != null'>, category_id = #{categoryId}</if>" +
            "<if test='price != null'>, price = #{price}</if>" +
            "<if test='specs != null'>, specs = #{specs}</if>" +
            " WHERE id = #{id}" +
            "</script>")
    void update(Product product);

    @Delete("UPDATE product SET deleted = 1, update_time = NOW() WHERE id = #{id}")
    void deleteById(Long id);

    Long count(PageQueryDTO dto);

    List<ProductVO> list(PageQueryDTO dto);

    ProductVO getDetailById(Long id);
}
