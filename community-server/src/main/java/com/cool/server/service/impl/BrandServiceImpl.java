package com.cool.server.service.impl;

import com.cool.pojo.dto.BrandCreateDTO;
import com.cool.pojo.entity.Brand;
import com.cool.pojo.vo.BrandVO;
import com.cool.server.mapper.BrandMapper;
import com.cool.server.service.BrandService;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {
    
    private final BrandMapper brandMapper;

    public BrandServiceImpl(BrandMapper brandMapper) {
        this.brandMapper = brandMapper;
    }

    @Override
    public Long createBrand(BrandCreateDTO dto) {
        Brand brand = new Brand();
        BeanUtil.copyProperties(dto, brand);
        brandMapper.insert(brand);
        return brand.getId();
    }

    @Override
    public void updateBrand(BrandCreateDTO dto) {
        Brand brand = new Brand();
        BeanUtil.copyProperties(dto, brand);
        brandMapper.update(brand);
    }

    @Override
    public void deleteBrand(Long id) {
        brandMapper.deleteById(id);
    }

    @Override
    public List<BrandVO> getAllBrands() {
        return brandMapper.listAll();
    }

    @Override
    public BrandVO getBrandById(Long id) {
        return brandMapper.getById(id);
    }
}
