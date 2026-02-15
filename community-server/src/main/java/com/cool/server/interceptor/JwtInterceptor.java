package com.cool.server.interceptor;

import com.cool.common.properties.JwtProperties;
import com.cool.common.utils.HttpContextUtil;
import com.cool.common.utils.JwtUtil;
import com.cool.server.context.BaseContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    private final JwtProperties jwtProperties;

    public JwtInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = HttpContextUtil.getToken(request, jwtProperties.getTokenName());
        if (token != null && JwtUtil.validateToken(jwtProperties.getSecretKey(), token)) {
            Long userId = JwtUtil.getUserId(jwtProperties.getSecretKey(), token);
            BaseContext.setCurrentId(userId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}
