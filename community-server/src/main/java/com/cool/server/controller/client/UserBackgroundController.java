package com.cool.server.controller.client;

import com.cool.pojo.Result;
import com.cool.pojo.entity.UserBackground;
import com.cool.server.context.BaseContext;
import com.cool.server.service.UserBackgroundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;

/**
 * 用户背景图控制器
 */
@Tag(name = "用户背景图接口", description = "用户背景图相关接口")
@RestController
@RequestMapping("/background")
public class UserBackgroundController {
    
    @Autowired
    private UserBackgroundService backgroundService;
    
    @Operation(summary = "获取用户背景图列表", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/list")
    public Result<List<UserBackground>> getBackgroundList() {
        Long userId = BaseContext.getCurrentId();
        List<UserBackground> backgrounds = backgroundService.getBackgroundList(userId);
        return Result.success(backgrounds);

    }
    
    @Operation(summary = "获取用户当前背景图", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/current")
    public Result<UserBackground> getCurrentBackground() {
        Long userId = BaseContext.getCurrentId();
        UserBackground background = backgroundService.getCurrentBackground(userId);
        return Result.success(background);
    }
    
    @Operation(summary = "设置当前背景图", security = @SecurityRequirement(name = "Bearer"))
    @PostMapping("/set-current")
    public Result<String> setCurrentBackground(
            @Parameter(description = "背景图ID") @RequestParam Long backgroundId) {
        Long userId = BaseContext.getCurrentId();
        backgroundService.setCurrentBackground(userId, backgroundId);
        return Result.success("设置成功");
    }
}
