package com.cool.server.context;

import com.cool.common.utils.JwtUtil;
import com.cool.server.security.user.SecurityUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class BaseContext {
    // JWT请求头名称（需和你的JwtAuthenticationFilter中一致）
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final ThreadLocal<Long> currentId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUsername = new ThreadLocal<>();
    private static final ThreadLocal<Integer> currentRole = new ThreadLocal<>();

    // 静态注入JwtUtil（Spring环境下需确保JwtUtil已初始化）
    private static JwtUtil jwtUtil;

    // 提供setter方法，让Spring注入JwtUtil（解决静态工具类注入Bean问题）
    public static void setJwtUtil(JwtUtil jwtUtil) {
        BaseContext.jwtUtil = jwtUtil;
    }

    /**
     * 从请求头获取JWT令牌
     */
    private static String getTokenFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                // 去掉Bearer前缀，获取纯令牌
                return authHeader.substring(BEARER_PREFIX.length());
            }
        } catch (Exception e) {
            // 非Web环境（如定时任务）时，RequestContextHolder可能为空，直接返回null
            return null;
        }
        return null;
    }

    /**
     * 从JWT令牌解析用户ID
     */
    private static Long getUserIdFromToken() {
        if (jwtUtil == null) {
            return null;
        }
        String token = getTokenFromRequest();
        if (token == null) {
            return null;
        }
        // 解析令牌，JwtUtil已做异常处理，失败返回null
        return jwtUtil.getUserId(token);
    }

    /**
     * 从JWT令牌解析用户名
     */
    private static String getUsernameFromToken() {
        if (jwtUtil == null) {
            return null;
        }
        String token = getTokenFromRequest();
        if (token == null) {
            return null;
        }
        return jwtUtil.getUsername(token);
    }

    /**
     * 从JWT令牌解析用户角色
     */
    private static Integer getRoleFromToken() {
        if (jwtUtil == null) {
            return null;
        }
        String token = getTokenFromRequest();
        if (token == null) {
            return null;
        }
        // 注意：JwtUtil中getRole返回String，需转换为Integer
        String roleStr = jwtUtil.getRole(token);
        try {
            return roleStr != null ? Integer.parseInt(roleStr) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ===================== 原有方法优化 =====================
    public static void setCurrentId(Long id) {
        currentId.set(id);
    }

    public static Long getCurrentId() {
        // 1. 优先从ThreadLocal获取
        Long id = currentId.get();
        if (id != null) {
            return id;
        }
        // 2. 其次从SecurityContext获取
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUserDetails) {
            return ((SecurityUserDetails) authentication.getPrincipal()).getUserId();
        }
        // 3. 最后从JWT令牌解析
        return getUserIdFromToken();
    }

    public static void setCurrentUsername(String username) {
        currentUsername.set(username);
    }

    public static String getCurrentUsername() {
        // 1. 优先从ThreadLocal获取
        String username = currentUsername.get();
        if (username != null) {
            return username;
        }
        // 2. 其次从SecurityContext获取
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUserDetails) {
            return ((SecurityUserDetails) authentication.getPrincipal()).getUsername();
        }
        // 3. 最后从JWT令牌解析
        return getUsernameFromToken();
    }

    public static void setCurrentRole(Integer role) {
        currentRole.set(role);
    }

    public static Integer getCurrentRole() {
        // 1. 优先从ThreadLocal获取
        Integer role = currentRole.get();
        if (role != null) {
            return role;
        }
        // 2. 其次从SecurityContext获取
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUserDetails) {
            return ((SecurityUserDetails) authentication.getPrincipal()).getRole();
        }
        // 3. 最后从JWT令牌解析
        return getRoleFromToken();
    }

    public static void setCurrentUser(Long id, String username, Integer role) {
        currentId.set(id);
        currentUsername.set(username);
        currentRole.set(role);
    }

    public static void clear() {
        currentId.remove();
        currentUsername.remove();
        currentRole.remove();
    }

    public static void removeCurrentId() {
        currentId.remove();
    }
}