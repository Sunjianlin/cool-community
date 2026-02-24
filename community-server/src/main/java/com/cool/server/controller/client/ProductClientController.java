package com.cool.server.controller.client;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.ProductVO;
import com.cool.server.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "产品接口", description = "产品列表、详情")
@RestController
@RequestMapping("/product")
@Slf4j
public class ProductClientController {
    
    private final ProductService productService;

    public ProductClientController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "产品列表", description = "获取产品列表，支持分页和筛选")
    @GetMapping("/list")
    public PageVO<ProductVO> getProductList(PageQueryDTO dto) {
        return productService.getProductList(dto);
    }

    @Operation(summary = "产品详情", description = "获取产品详细信息")
    @GetMapping("/detail/{id}")
    public ProductVO getProductDetail(@Parameter(description = "产品ID") @PathVariable Long id) {
        log.info("请求产品详情信息，id为{}",id);
        return productService.getProductDetail(id);
    }
}
