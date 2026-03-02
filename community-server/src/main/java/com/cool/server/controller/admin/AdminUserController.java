package com.cool.server.controller.admin;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.UserVO;
import com.cool.server.annotation.RequireAdmin;
import com.cool.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-用户管理", description = "用户管理相关接口，需要管理员权限")
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {
    
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户列表", description = "获取用户列表，支持分页", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @GetMapping("/list")
    public PageVO<UserVO> getUserList(PageQueryDTO dto) {
        return userService.getUserList(dto);
    }

    @Operation(summary = "禁用用户", description = "禁用指定用户", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @PostMapping("/ban/{id}")
    public void banUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.banUser(id);
    }

    @Operation(summary = "解禁用户", description = "解禁指定用户", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @PostMapping("/unban/{id}")
    public void unbanUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.unbanUser(id);
    }

    @Operation(summary = "删除用户", description = "删除指定用户", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @DeleteMapping("/{id}")
    public void deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
    }

    @Operation(summary = "踢人下线", description = "强制指定用户下线", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @PostMapping("/kick/{id}")
    public void kickUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.kickUser(id);
    }
    
    @Operation(summary = "在线用户统计", description = "获取在线用户统计信息", security = @SecurityRequirement(name = "Bearer"))
    @RequireAdmin
    @GetMapping("/online-stats")
    public java.util.Map<String, Object> getOnlineUserStats() {
        return userService.getOnlineUserStats();
    }
}
