package com.cool.server.controller.client;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "推荐接口", description = "推荐相关操作")
@RestController
@RequestMapping("/recommendation")
public class RecommendationController {
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Operation(summary = "推荐用户", description = "推荐用户关注", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @GetMapping("/users")
    public PageVO<?> recommendUsers(PageQueryDTO dto) {
        return recommendationService.recommendUsers(dto);
    }
    
    @Operation(summary = "推荐话题", description = "推荐话题关注", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @GetMapping("/topics")
    public PageVO<?> recommendTopics(PageQueryDTO dto) {
        return recommendationService.recommendTopics(dto);
    }
    
    @Operation(summary = "推荐产品", description = "推荐产品关注", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @GetMapping("/products")
    public PageVO<?> recommendProducts(PageQueryDTO dto) {
        return recommendationService.recommendProducts(dto);
    }
}
