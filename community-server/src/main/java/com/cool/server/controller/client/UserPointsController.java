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
@RequestMapping("/points")
public class UserPointsController {

    @Autowired
    private UserPointsService userPointsService;

    @Operation(summary = "获取用户积分", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/")
    public Result<Integer> getUserPoints() {
        Integer points = userPointsService.getUserPoints();
        return Result.success(points);
    }

    @Operation(summary = "增加用户积分", security = @SecurityRequirement(name = "Bearer"))
    @PostMapping("/add")
    public Result<Void> addPoints(
            @Parameter(description = "积分数量") @RequestParam Integer points,
            @Parameter(description = "积分类型") @RequestParam Integer type) {
        userPointsService.addPoints(points, type);
        return Result.success();
    }

    @Operation(summary = "减少用户积分", security = @SecurityRequirement(name = "Bearer"))
    @PostMapping("/reduce")
    public Result<Boolean> reducePoints(
            @Parameter(description = "积分数量") @RequestParam Integer points,
            @Parameter(description = "积分类型") @RequestParam Integer type) {
        boolean success = userPointsService.reducePoints(points, type);
        return Result.success(success);
    }
}
