package com.cool.server.controller.client;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.context.BaseContext;
import com.cool.server.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "关注接口", description = "关注用户、话题、产品")
@RestController
@RequestMapping("/follow")
public class FollowController {
    
    @Autowired
    private FollowService followService;
    
    @Operation(summary = "关注用户", description = "关注指定用户", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/user/{id}")
    public void followUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        followService.follow(id, FollowService.TYPE_USER);
    }
    
    @Operation(summary = "取消关注用户", description = "取消关注指定用户", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @DeleteMapping("/user/{id}")
    public void unfollowUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        followService.unfollow(id, FollowService.TYPE_USER);
    }
    
    @Operation(summary = "关注话题", description = "关注指定话题", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/topic/{id}")
    public void followTopic(@Parameter(description = "话题ID") @PathVariable Long id) {
        followService.follow(id, FollowService.TYPE_TOPIC);
    }
    
    @Operation(summary = "取消关注话题", description = "取消关注指定话题", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @DeleteMapping("/topic/{id}")
    public void unfollowTopic(@Parameter(description = "话题ID") @PathVariable Long id) {
        followService.unfollow(id, FollowService.TYPE_TOPIC);
    }
    
    @Operation(summary = "关注产品", description = "关注指定产品", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/product/{id}")
    public void followProduct(@Parameter(description = "产品ID") @PathVariable Long id) {
        followService.follow(id, FollowService.TYPE_PRODUCT);
    }
    
    @Operation(summary = "取消关注产品", description = "取消关注指定产品", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @DeleteMapping("/product/{id}")
    public void unfollowProduct(@Parameter(description = "产品ID") @PathVariable Long id) {
        followService.unfollow(id, FollowService.TYPE_PRODUCT);
    }
    
    @Operation(summary = "检查是否关注", description = "检查当前用户是否关注了指定对象")
    @RequireLogin
    @GetMapping("/check")
    public boolean checkFollow(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "目标类型：0-用户，1-话题，2-产品") @RequestParam int targetType) {
        Long userId = BaseContext.getCurrentId();
        return followService.isFollowing(userId, targetId, targetType);
    }
    
    @Operation(summary = "获取关注列表", description = "获取用户的关注列表")
    @GetMapping("/following/{userId}")
    public PageVO<?> getFollowingList(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "目标类型：0-用户，1-话题，2-产品") @RequestParam int targetType,
            PageQueryDTO dto) {
        return followService.getFollowingList(userId, targetType, dto);
    }
    
    @Operation(summary = "获取粉丝/关注者列表", description = "获取对象的粉丝/关注者列表")
    @GetMapping("/followers/{targetId}")
    public PageVO<?> getFollowerList(
            @Parameter(description = "目标ID") @PathVariable Long targetId,
            @Parameter(description = "目标类型：0-用户，1-话题，2-产品") @RequestParam int targetType,
            PageQueryDTO dto) {
        return followService.getFollowerList(targetId, targetType, dto);
    }
    
    @Operation(summary = "获取关注数量", description = "获取对象的关注数量")
    @GetMapping("/count/{targetId}")
    public Long getFollowCount(
            @Parameter(description = "目标ID") @PathVariable Long targetId,
            @Parameter(description = "目标类型：0-用户，1-话题，2-产品") @RequestParam int targetType) {
        return followService.getFollowCount(targetId, targetType);
    }
    
    @Operation(summary = "获取被关注数量", description = "获取用户的被关注数量")
    @GetMapping("/follower/count/{userId}")
    public Long getFollowerCount(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "目标类型：0-用户，1-话题，2-产品") @RequestParam int targetType) {
        return followService.getFollowerCount(userId, targetType);
    }
}
