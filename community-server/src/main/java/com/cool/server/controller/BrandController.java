package com.cool.server.controller;

import com.cool.pojo.dto.BrandCreateDTO;
import com.cool.pojo.vo.BrandVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.service.BrandService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {
    
    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping("/create")
    @RequireLogin
    public Long createBrand(@RequestBody BrandCreateDTO dto) {
        return brandService.createBrand(dto);
    }

    @PutMapping("/update")
    @RequireLogin
    public void updateBrand(@RequestBody BrandCreateDTO dto) {
        brandService.updateBrand(dto);
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    public void deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
    }

    @GetMapping("/list")
    public List<BrandVO> getAllBrands() {
        return brandService.getAllBrands();
    }

    @GetMapping("/{id}")
    public BrandVO getBrandById(@PathVariable Long id) {
        return brandService.getBrandById(id);
    }
}
