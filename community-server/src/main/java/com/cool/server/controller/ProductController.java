package com.cool.server.controller;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.ProductCreateDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.ProductVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.service.ProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {
    
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    @RequireLogin
    public Long createProduct(@RequestBody ProductCreateDTO dto) {
        return productService.createProduct(dto);
    }

    @PutMapping("/update")
    @RequireLogin
    public void updateProduct(@RequestBody ProductCreateDTO dto) {
        productService.updateProduct(dto);
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @GetMapping("/list")
    public PageVO<ProductVO> getProductList(PageQueryDTO dto) {
        return productService.getProductList(dto);
    }

    @GetMapping("/detail/{id}")
    public ProductVO getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }
}
