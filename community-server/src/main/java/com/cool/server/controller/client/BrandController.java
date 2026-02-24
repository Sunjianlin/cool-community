package com.cool.server.controller.client;

import com.cool.pojo.vo.BrandVO;
import com.cool.server.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "品牌接口", description = "品牌列表、详情")
@RestController
@RequestMapping("/brand")
public class BrandController {
    
    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @Operation(summary = "品牌列表", description = "获取所有品牌列表")
    @GetMapping("/list")
    public List<BrandVO> getAllBrands() {
        return brandService.getAllBrands();
    }

    @Operation(summary = "品牌详情", description = "获取品牌详细信息")
    @GetMapping("/{id}")
    public BrandVO getBrandById(@Parameter(description = "品牌ID") @PathVariable Long id) {
        return brandService.getBrandById(id);
    }
}
