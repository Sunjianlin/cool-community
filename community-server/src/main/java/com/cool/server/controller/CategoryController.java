package com.cool.server.controller;

import com.cool.pojo.vo.CategoryVO;
import com.cool.server.service.CategoryService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/list")
    public List<CategoryVO> getAllCategories() {
        return categoryService.getAllCategories();
    }
}
