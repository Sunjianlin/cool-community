package com.cool.server.controller.client;

import com.cool.pojo.entity.SeckillActivity;
import com.cool.server.context.BaseContext;
import com.cool.server.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

/**
 * 秒杀活动控制器
 */
@Tag(name = "秒杀接口", description = "秒杀活动相关接口")
@RestController
@RequestMapping("/api/seckill")
public class SeckillController {
    
    @Autowired
    private SeckillService seckillService;
    
    @Operation(summary = "获取活动详情", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/detail/{id}")
    public Map<String, Object> getActivityDetail(
            @Parameter(description = "活动ID") @PathVariable Long id) {
        SeckillActivity activity = seckillService.getActivityById(id);
        return Map.of("code", 200, "data", activity);
    }
    
    @Operation(summary = "执行秒杀", security = @SecurityRequirement(name = "Bearer"))
    @PostMapping("/do/{id}")
    public Map<String, Object> doSeckill(
            @Parameter(description = "活动ID") @PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        boolean result = seckillService.doSeckill(userId, id);
        return Map.of("code", 200, "data", result);
    }
    
    @Operation(summary = "获取次日活动", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/next-day")
    public Map<String, Object> getNextDayActivity() {
        SeckillActivity activity = seckillService.getNextDayActivity();
        return Map.of("code", 200, "data", activity);
    }
    
    @Operation(summary = "检查是否有次日活动", security = @SecurityRequirement(name = "Bearer"))
    @GetMapping("/has-next-day")
    public Map<String, Object> hasNextDayActivity() {
        boolean hasActivity = seckillService.hasNextDayActivity();
        return Map.of("code", 200, "data", hasActivity);
    }
}
