package com.cool.server.controller.admin;

import com.cool.pojo.vo.StatisticVO;
import com.cool.server.service.FollowStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "关注统计接口", description = "关注统计相关操作")
@RestController
@RequestMapping("/admin/follow/statistics")
public class FollowStatisticsController {
    
    @Autowired
    private FollowStatisticsService followStatisticsService;
    
    @Operation(summary = "关注增长趋势", description = "获取最近几天的关注增长趋势", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/growth")
    public List<StatisticVO> getFollowGrowthTrend(@RequestParam(defaultValue = "7") int days) {
        return followStatisticsService.getFollowGrowthTrend(days);
    }
    
    @Operation(summary = "关注类型分布", description = "获取不同类型的关注分布", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/distribution")
    public Map<String, Long> getFollowTypeDistribution() {
        return followStatisticsService.getFollowTypeDistribution();
    }
    
    @Operation(summary = "关注最多的用户", description = "获取关注最多的用户", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/top/users")
    public List<StatisticVO> getTopFollowedUsers(@RequestParam(defaultValue = "10") int limit) {
        return followStatisticsService.getTopFollowedUsers(limit);
    }
    
    @Operation(summary = "关注最多的话题", description = "获取关注最多的话题", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/top/topics")
    public List<StatisticVO> getTopFollowedTopics(@RequestParam(defaultValue = "10") int limit) {
        return followStatisticsService.getTopFollowedTopics(limit);
    }
    
    @Operation(summary = "关注最多的产品", description = "获取关注最多的产品", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/top/products")
    public List<StatisticVO> getTopFollowedProducts(@RequestParam(defaultValue = "10") int limit) {
        return followStatisticsService.getTopFollowedProducts(limit);
    }
}
