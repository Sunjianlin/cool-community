package com.cool.server.mapper;

import com.cool.pojo.vo.CategoryVO;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CategoryMapper {
    
    @Select("SELECT id, name, parent_id, sort FROM product_category WHERE deleted = 0 ORDER BY sort, id")
    List<CategoryVO> listAll();
}
