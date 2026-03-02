package com.cool.server.controller.client;

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
@RequestMapping("/api/background")
public class UserBackgroundController {
    
    @Autowired
    private UserBackgroundService backgroundService;
    
    @Operation(summary = "获取用户背景图列表", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/list")
    public Map<String, Object> getBackgroundList() {
        Long userId = BaseContext.getCurrentId();
        List<UserBackground> backgrounds = backgroundService.getBackgroundList(userId);
        return Map.of("code", 200, "data", backgrounds);
    }
    
    @Operation(summary = "获取用户当前背景图", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/current")
    public Map<String, Object> getCurrentBackground() {
        Long userId = BaseContext.getCurrentId();
        UserBackground background = backgroundService.getCurrentBackground(userId);
        return Map.of("code", 200, "data", background);
    }
    
    @Operation(summary = "设置当前背景图", security = @SecurityRequirement(name = "Bearer"))
    @PostMapping("/set-current")
    public Map<String, Object> setCurrentBackground(
            @Parameter(description = "背景图ID") @RequestParam Long backgroundId) {
        Long userId = BaseContext.getCurrentId();
        backgroundService.setCurrentBackground(userId, backgroundId);
        return Map.of("code", 200, "message", "设置成功");
    }
}
