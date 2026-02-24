package com.cool.server.interceptor;

import com.cool.common.constant.MessageConstant;
import com.cool.common.constant.RedisConstant;
import com.cool.common.properties.JwtProperties;
import com.cool.common.utils.HttpContextUtil;
import com.cool.common.utils.JwtUtil;
import com.cool.server.context.BaseContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public JwtInterceptor(JwtProperties jwtProperties, StringRedisTemplate stringRedisTemplate) {
        this.jwtProperties = jwtProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = HttpContextUtil.getToken(request, jwtProperties.getTokenName());
        
        if (token != null) {
            try {
                if (JwtUtil.validateToken(jwtProperties.getSecretKey(), token)) {
                    Long userId = JwtUtil.getUserId(jwtProperties.getSecretKey(), token);
                    String username = JwtUtil.getUsername(jwtProperties.getSecretKey(), token);
                    Integer role = JwtUtil.getRole(jwtProperties.getSecretKey(), token);
                    
                    String redisToken = stringRedisTemplate.opsForValue().get(RedisConstant.USER_TOKEN_KEY + userId);
                    if (redisToken != null && redisToken.equals(token)) {
                        BaseContext.setCurrentUser(userId, username, role);
                    } else {
                        log.warn("Token已被踢下线: {}", token);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=utf-8");
                        response.getWriter().write("{\"code\":401,\"message\":\"Token已失效\"}");
                        return false;
                    }
                } else {
                    log.warn("Token已过期: {}", token);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"Token已过期\"}");
                    return false;
                }
            } catch (Exception e) {
                log.error("Token验证失败: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token无效\"}");
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.clear();
    }
}
