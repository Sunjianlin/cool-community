package com.cool.server.controller.client;

import com.cool.pojo.vo.CategoryVO;
import com.cool.server.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "分类接口", description = "分类列表")
@RestController
@RequestMapping("/category")
public class CategoryController {
    
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "分类列表", description = "获取所有分类列表")
    @GetMapping("/list")
    public List<CategoryVO> getAllCategories() {
        return categoryService.getAllCategories();
    }
}
