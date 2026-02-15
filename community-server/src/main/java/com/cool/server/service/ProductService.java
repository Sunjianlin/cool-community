package com.cool.server.service;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.ProductCreateDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.ProductVO;

public interface ProductService {
    
    Long createProduct(ProductCreateDTO dto);

    void updateProduct(ProductCreateDTO dto);

    void deleteProduct(Long id);

    PageVO<ProductVO> getProductList(PageQueryDTO dto);

    ProductVO getProductDetail(Long id);
}
