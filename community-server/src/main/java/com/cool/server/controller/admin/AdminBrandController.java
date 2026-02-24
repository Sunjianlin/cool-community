package com.cool.server.controller.admin;

import com.cool.pojo.dto.BrandCreateDTO;
import com.cool.pojo.vo.BrandVO;
import com.cool.server.annotation.RequireAdmin;
import com.cool.server.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "管理员-品牌管理", description = "品牌管理相关接口，需要管理员权限")
@RestController
@RequestMapping("/admin/brand")
public class AdminBrandController {
    
    private final BrandService brandService;

    public AdminBrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @Operation(summary = "创建品牌", description = "创建新品牌", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @PostMapping("/create")
    public Long createBrand(@RequestBody BrandCreateDTO dto) {
        return brandService.createBrand(dto);
    }

    @Operation(summary = "更新品牌", description = "更新品牌信息", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @PutMapping("/update")
    public void updateBrand(@RequestBody BrandCreateDTO dto) {
        brandService.updateBrand(dto);
    }

    @Operation(summary = "删除品牌", description = "删除品牌", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @DeleteMapping("/{id}")
    public void deleteBrand(@Parameter(description = "品牌ID") @PathVariable Long id) {
        brandService.deleteBrand(id);
    }

    @Operation(summary = "品牌列表", description = "获取所有品牌列表", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @GetMapping("/list")
    public List<BrandVO> getAllBrands() {
        return brandService.getAllBrands();
    }
}
