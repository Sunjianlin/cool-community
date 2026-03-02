package com.cool.server.service;

import com.cool.pojo.vo.PageVO;
import com.cool.pojo.dto.PageQueryDTO;

public interface RecommendationService {
    
    PageVO<?> recommendUsers(PageQueryDTO dto);
    PageVO<?> recommendTopics(PageQueryDTO dto);
    PageVO<?> recommendProducts(PageQueryDTO dto);
}
