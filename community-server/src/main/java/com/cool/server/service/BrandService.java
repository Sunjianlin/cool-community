package com.cool.server.service;

import com.cool.pojo.dto.BrandCreateDTO;
import com.cool.pojo.vo.BrandVO;
import java.util.List;

public interface BrandService {
    
    Long createBrand(BrandCreateDTO dto);

    void updateBrand(BrandCreateDTO dto);

    void deleteBrand(Long id);

    List<BrandVO> getAllBrands();

    BrandVO getBrandById(Long id);
}
