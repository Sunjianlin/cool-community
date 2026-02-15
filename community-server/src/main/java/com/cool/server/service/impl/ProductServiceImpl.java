package com.cool.server.service.impl;

import com.cool.common.constant.MessageConstant;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.ProductCreateDTO;
import com.cool.pojo.entity.Product;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.ProductVO;
import com.cool.server.mapper.ProductMapper;
import com.cool.server.service.ProductService;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public Long createProduct(ProductCreateDTO dto) {
        Product product = new Product();
        BeanUtil.copyProperties(dto, product);
        product.setReviewCount(0);
        product.setStatus(1);
        
        productMapper.insert(product);
        return product.getId();
    }

    @Override
    public void updateProduct(ProductCreateDTO dto) {
        Product product = new Product();
        BeanUtil.copyProperties(dto, product);
        productMapper.update(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
    }

    @Override
    public PageVO<ProductVO> getProductList(PageQueryDTO dto) {
        Long total = productMapper.count(dto);
        List<ProductVO> products = productMapper.list(dto);
        return PageVO.of(products, total, dto.getPage(), dto.getPageSize());
    }

    @Override
    public ProductVO getProductDetail(Long id) {
        ProductVO product = productMapper.getDetailById(id);
        if (product == null) {
            throw new RuntimeException(MessageConstant.DATA_NOT_FOUND);
        }
        return product;
    }
}
