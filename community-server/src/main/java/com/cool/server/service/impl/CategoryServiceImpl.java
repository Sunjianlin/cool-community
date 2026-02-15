package com.cool.server.service.impl;

import com.cool.pojo.vo.CategoryVO;
import com.cool.server.mapper.CategoryMapper;
import com.cool.server.service.CategoryService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryVO> getAllCategories() {
        return categoryMapper.listAll();
    }
}
