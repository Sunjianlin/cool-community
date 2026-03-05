package com.cool.server.aspect;

import com.cool.server.annotation.RequireAdmin;
import com.cool.server.annotation.RequireLogin;
import com.cool.server.context.BaseContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AdminAspect {

    @Before("@annotation(requireLogin)")
    public void checkLogin(RequireLogin requireLogin) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
    }

    @Before("@annotation(requireAdmin)")
    public void checkAdmin(RequireAdmin requireAdmin) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Integer role = BaseContext.getCurrentRole();
        if (role == null || role < 2) {
            throw new RuntimeException("权限不足，需要管理员权限");
        }
    }
}
