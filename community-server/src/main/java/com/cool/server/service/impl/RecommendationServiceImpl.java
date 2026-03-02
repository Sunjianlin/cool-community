package com.cool.server.service.impl;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.ProductVO;
import com.cool.pojo.vo.TopicVO;
import com.cool.pojo.vo.UserVO;
import com.cool.server.context.BaseContext;
import com.cool.server.mapper.FollowMapper;
import com.cool.server.mapper.ProductMapper;
import com.cool.server.mapper.TopicMapper;
import com.cool.server.mapper.UserMapper;
import com.cool.server.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    
    @Autowired
    private FollowMapper followMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TopicMapper topicMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    public PageVO<?> recommendUsers(PageQueryDTO dto) {
        Long userId = BaseContext.getCurrentId();
        int offset = (dto.getPage() - 1) * dto.getPageSize();
        
        // 简单实现：推荐关注数较多的用户，排除已关注的
        List<UserVO> users = userMapper.getRecommendedUsers(userId, offset, dto.getPageSize());
        Long total = userMapper.countRecommendedUsers(userId);
        
        return PageVO.of(users, total, dto.getPage(), dto.getPageSize());
    }
    
    @Override
    public PageVO<?> recommendTopics(PageQueryDTO dto) {
        Long userId = BaseContext.getCurrentId();
        int offset = (dto.getPage() - 1) * dto.getPageSize();
        
        // 简单实现：推荐关注数较多的话题，排除已关注的
        List<TopicVO> topics = topicMapper.getRecommendedTopics(userId, offset, dto.getPageSize());
        Long total = topicMapper.countRecommendedTopics(userId);
        
        return PageVO.of(topics, total, dto.getPage(), dto.getPageSize());
    }
    
    @Override
    public PageVO<?> recommendProducts(PageQueryDTO dto) {
        Long userId = BaseContext.getCurrentId();
        int offset = (dto.getPage() - 1) * dto.getPageSize();
        
        // 简单实现：推荐评论数较多的产品
        List<ProductVO> products = productMapper.getRecommendedProducts(offset, dto.getPageSize());
        Long total = productMapper.countRecommendedProducts();
        
        return PageVO.of(products, total, dto.getPage(), dto.getPageSize());
    }
}
