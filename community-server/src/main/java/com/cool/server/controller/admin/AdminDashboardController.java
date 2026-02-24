package com.cool.server.controller.admin;

import com.cool.server.annotation.RequireAdmin;
import com.cool.server.service.DashboardService;
import com.cool.pojo.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-仪表盘", description = "系统统计数据接口，需要管理员权限")
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {
    
    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "获取统计数据", description = "获取系统统计数据（用户数、帖子数、话题数等）", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @GetMapping("/stats")
    public DashboardVO getStats() {
        return dashboardService.getStats();
    }
}
