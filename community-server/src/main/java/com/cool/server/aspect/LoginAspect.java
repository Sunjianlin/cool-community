package com.cool.server.aspect;

import com.cool.server.annotation.RequireLogin;
import com.cool.server.context.BaseContext;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LoginAspect {
    
    @Before("@annotation(requireLogin)")
    public void checkLogin(RequireLogin requireLogin) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
    }
}
