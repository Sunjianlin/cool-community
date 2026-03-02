package com.cool.server.controller.client;

import com.cool.common.Result;
import com.cool.server.service.UserPointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户积分控制器
 */
@Tag(name = "用户积分", description = "用户积分相关接口")
@RestController
@RequestMapping("/api/points")
public class UserPointsController {

    @Autowired
    private UserPointsService userPointsService;

    @Operation(summary = "获取用户积分", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/{userId}")
    public Result<Integer> getUserPoints(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        Integer points = userPointsService.getUserPoints(userId);
        return Result.success(points);
    }

    @Operation(summary = "增加用户积分", security = @SecurityRequirement(name = "Bearer"))
    @PostMapping("/add")
    public Result<Void> addPoints(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "积分数量") @RequestParam Integer points) {
        userPointsService.addPoints(userId, points);
        return Result.success();
    }

    @Operation(summary = "减少用户积分", security = @SecurityRequirement(name = "Bearer"))
    @PostMapping("/reduce")
    public Result<Boolean> reducePoints(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "积分数量") @RequestParam Integer points) {
        boolean success = userPointsService.reducePoints(userId, points);
        return Result.success(success);
    }

    @Operation(summary = "获取用户积分记录", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/{userId}/records")
    public Result<?> getPointsRecords(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        // 这里需要根据实际的UserPointsService实现来返回积分记录
        return Result.success(null);
    }
}
