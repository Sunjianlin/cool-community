package com.cool.server.controller.admin;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.ProductCreateDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.ProductVO;
import com.cool.server.annotation.RequireAdmin;
import com.cool.server.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-产品管理", description = "产品管理相关接口，需要管理员权限")
@RestController
@RequestMapping("/admin/product")
public class AdminProductController {
    
    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "产品列表", description = "获取产品列表，支持分页", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @GetMapping("/list")
    public PageVO<ProductVO> getProductList(PageQueryDTO dto) {
        return productService.getProductList(dto);
    }

    @Operation(summary = "创建产品", description = "创建新产品", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @PostMapping("/create")
    public Long createProduct(@RequestBody ProductCreateDTO dto) {
        return productService.createProduct(dto);
    }

    @Operation(summary = "更新产品", description = "更新产品信息", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @PutMapping("/update")
    public void updateProduct(@RequestBody ProductCreateDTO dto) {
        productService.updateProduct(dto);
    }

    @Operation(summary = "删除产品", description = "删除产品", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @DeleteMapping("/{id}")
    public void deleteProduct(@Parameter(description = "产品ID") @PathVariable Long id) {
        productService.deleteProduct(id);
    }
}
