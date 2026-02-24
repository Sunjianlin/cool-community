package com.cool.server.controller.client;

import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.dto.UserLoginDTO;
import com.cool.pojo.dto.UserRegisterDTO;
import com.cool.pojo.vo.PageVO;
import com.cool.pojo.vo.UserLoginVO;
import com.cool.pojo.vo.UserVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "用户接口", description = "用户登录、注册、个人信息管理")
@RestController
@RequestMapping("/user")
public class UserClientController {
    
    private final UserService userService;

    public UserClientController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户登录", description = "通过用户名和密码登录，返回用户信息和Token")
    @PostMapping("/login")
    public UserLoginVO login(@RequestBody UserLoginDTO dto) {
        return userService.login(dto);
    }

    @Operation(summary = "用户注册", description = "注册新用户账号")
    @PostMapping("/register")
    public void register(@RequestBody UserRegisterDTO dto) {
        userService.register(dto);
    }

    @Operation(summary = "退出登录", description = "退出登录，清除Token", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/logout")
    public void logout() {
        userService.logout();
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @GetMapping("/info")
    public UserVO getUserInfo() {
        return userService.getUserInfo();
    }

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户公开信息")
    @GetMapping("/info/{id}")
    public UserVO getUserInfoById(@Parameter(description = "用户ID") @PathVariable Long id) {
        return userService.getUserInfoById(id);
    }

    @Operation(summary = "更新用户信息", description = "更新当前用户的个人信息", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PutMapping("/update")
    public void updateUserInfo(@RequestBody UserVO vo) {
        userService.updateUserInfo(vo);
    }

    @Operation(summary = "上传头像", description = "上传用户头像图片", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/avatar")
    public String uploadAvatar(@Parameter(description = "头像文件") @RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(file);
    }

    @Operation(summary = "关注用户", description = "关注指定用户", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @PostMapping("/follow/{id}")
    public void followUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.followUser(id);
    }

    @Operation(summary = "取消关注用户", description = "取消关注指定用户", security = @SecurityRequirement(name = "Bearer"))
    @RequireLogin
    @DeleteMapping("/unfollow/{id}")
    public void unfollowUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.unfollowUser(id);
    }

    @Operation(summary = "获取关注列表", description = "获取指定用户的关注列表")
    @GetMapping("/following/{id}")
    public PageVO<UserVO> getFollowingList(
            @Parameter(description = "用户ID") @PathVariable Long id, 
            PageQueryDTO dto) {
        return userService.getFollowingList(id, dto);
    }

    @Operation(summary = "获取粉丝列表", description = "获取指定用户的粉丝列表")
    @GetMapping("/followers/{id}")
    public PageVO<UserVO> getFollowerList(
            @Parameter(description = "用户ID") @PathVariable Long id, 
            PageQueryDTO dto) {
        return userService.getFollowerList(id, dto);
    }
}
