package com.cool.server.service.impl;

import com.cool.pojo.entity.User;
import com.cool.server.context.BaseContext;
import com.cool.server.mapper.UserMapper;
import com.cool.server.security.user.SecurityUserDetails;
import com.cool.server.service.UserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserMapper userMapper;
    @Override
    public UserDetails loadUserByUsername(String username) {
        // 1. 校验用户名非空
        if (username == null || username.isBlank()) {
            log.error("加载用户信息失败：用户名为空");
            throw new UsernameNotFoundException("用户名不能为空");
        }

        // 2. 从数据库查询用户（根据你的业务调整查询条件：用户名/手机号/邮箱）
        User user = userMapper.getByUsername(username); // 核心：替换为你的查询方法
//        // 备用：如果支持手机号登录，可加兜底查询
//        if (user == null) {
//            user = userMapper.selectByPhone(username);
//        }
        if (user == null) {
            log.error("加载用户信息失败：用户不存在，username={}", username);
            throw new UsernameNotFoundException("用户不存在：" + username);
        }

        // 3. 校验用户状态（可选：禁用/锁定/过期）
        if (user.getStatus() == 0) { // 假设 0=禁用，1=正常
            log.error("加载用户信息失败：用户已禁用，userId={}", user.getId());
            throw new UsernameNotFoundException("用户已禁用，无法登录");
        }

        // 4. 封装用户角色为 Spring Security 权限对象
        // 注意：Spring Security 权限命名规范：ROLE_ + 角色值（如 ROLE_1、ROLE_ADMIN）
        String role = "ROLE_" + user.getRole(); // 你的 user.getRole() 是 Integer 类型（0/1/2/3）
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(role)
        );

        // 5. 封装为自定义的 SecurityUserDetails
        SecurityUserDetails userDetails = new SecurityUserDetails();
        userDetails.setUserId(user.getId()); // 用户ID
        userDetails.setUsername(user.getUsername()); // 用户名
        userDetails.setPassword(user.getPassword()); // 加密后的密码（必须是BCrypt加密）
        userDetails.setRole(user.getRole()); // 原始角色值（Integer）
        userDetails.setAuthorities(authorities); // 权限列表
        // 可选：设置用户状态（默认都是true，若有锁定逻辑可调整）
        userDetails.setEnabled(true); // 是否启用
        userDetails.setAccountNonExpired(true); // 账号未过期
        userDetails.setAccountNonLocked(true); // 账号未锁定
        userDetails.setCredentialsNonExpired(true); // 密码未过期

        // 可选：将用户信息存入 BaseContext（方便业务层获取）
        BaseContext.setCurrentUser(user.getId(), user.getUsername(), user.getRole());

        log.info("加载用户信息成功，userId={}, username={}, role={}",
                user.getId(), user.getUsername(), user.getRole());
        return userDetails;
    }
}
