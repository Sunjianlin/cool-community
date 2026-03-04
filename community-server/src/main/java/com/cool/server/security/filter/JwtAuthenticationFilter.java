package com.cool.server.security.filter;


import com.cool.common.utils.JwtUtil;
import com.cool.server.service.UserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
/*
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = token.replace("Bearer ", "");
        String username;
        Long userId;
        String role;

        try {
            username = jwtUtil.getUsername(token);
            userId = jwtUtil.getUserId(token);
            role = jwtUtil.getRole(token);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId, null,
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

*/
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");
    String accessToken = null;

    // 1. 提取 Access Token（仅处理 Bearer 开头的 Token）
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        accessToken = authHeader.substring(7);
    }

    // 2. 校验 Token 有效性（仅处理 Access Token）
    if (accessToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        // 解析 Token 并校验类型
        String tokenType = jwtUtil.getTokenType(accessToken);
        if (!"access".equals(tokenType)) {
            // 拒绝 Refresh Token 访问业务接口，返回 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token type: only access token is allowed");
            return;
        }

        // 校验 Token 是否过期
        if (jwtUtil.isExpired(accessToken)) {
            // 返回 401，提示前端刷新 Token
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Access token expired, please refresh");
            return;
        }

        // 3. 解析用户信息，设置 SecurityContext
        String username = jwtUtil.getUsername(accessToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    filterChain.doFilter(request, response);
}
}
