package com.cool.server.controller;

import com.cool.pojo.dto.UserLoginDTO;
import com.cool.pojo.dto.UserRegisterDTO;
import com.cool.pojo.dto.PageQueryDTO;
import com.cool.pojo.vo.UserLoginVO;
import com.cool.pojo.vo.UserVO;
import com.cool.pojo.vo.PageVO;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public void register(@RequestBody UserRegisterDTO dto) {
        userService.register(dto);
    }

    @PostMapping("/login")
    public UserLoginVO login(@RequestBody UserLoginDTO dto) {
        return userService.login(dto);
    }

    @GetMapping("/info")
    @RequireLogin
    public UserVO getUserInfo() {
        return userService.getUserInfo();
    }

    @GetMapping("/info/{id}")
    public UserVO getUserInfoById(@PathVariable Long id) {
        return userService.getUserInfoById(id);
    }

    @PutMapping("/update")
    @RequireLogin
    public void updateUserInfo(@RequestBody UserVO vo) {
        userService.updateUserInfo(vo);
    }

    @PostMapping("/avatar")
    @RequireLogin
    public String uploadAvatar(@RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(file);
    }

    @PostMapping("/follow/{id}")
    @RequireLogin
    public void followUser(@PathVariable Long id) {
        userService.followUser(id);
    }

    @DeleteMapping("/unfollow/{id}")
    @RequireLogin
    public void unfollowUser(@PathVariable Long id) {
        userService.unfollowUser(id);
    }

    @GetMapping("/list")
    @RequireLogin
    public PageVO<UserVO> getUserList(PageQueryDTO dto) {
        return userService.getUserList(dto);
    }

    @PostMapping("/ban/{id}")
    @RequireLogin
    public void banUser(@PathVariable Long id) {
        userService.banUser(id);
    }

    @PostMapping("/unban/{id}")
    @RequireLogin
    public void unbanUser(@PathVariable Long id) {
        userService.unbanUser(id);
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
