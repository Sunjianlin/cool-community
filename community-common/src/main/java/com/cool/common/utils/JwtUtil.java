package com.cool.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;


@Slf4j
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpiration;

    @PostConstruct
    public void init() {
        // 校验secretKey必须配置（核心密钥不能用默认值）
        if (secretKey == null || secretKey.isBlank()) {
            log.error("JWT配置错误：jwt.secret 未配置！");
            throw new IllegalArgumentException("jwt.secret 不能为空");
        }
        // 打印配置值，方便排查
        log.info("JWT配置加载成功：accessExpiration={}秒，refreshExpiration={}秒",
                accessExpiration, refreshExpiration);
    }

    /**
     * 获取JWT签名密钥（指定UTF-8编码，避免环境差异）
     */
    private SecretKey getKey() {
        // 强制指定UTF-8编码，保证密钥一致性
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Access Token（补充默认Claim：iat、jti）
     * @param claims 自定义载荷（需包含userId、username、role等）
     * @return 访问令牌
     */
    public String generateAccessToken(Map<String, Object> claims) {
        // 补充基础Claim，避免业务层重复设置
        Map<String, Object> baseClaims = new HashMap<>(claims);
        baseClaims.put("tokenType", "access"); // 标记令牌类型
        baseClaims.put("iat", new Date()); // 签发时间
        baseClaims.put("jti", UUID.randomUUID().toString()); // 唯一标识

        return Jwts.builder()
                .setClaims(baseClaims)
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration * 1000))
                .signWith(getKey(), SignatureAlgorithm.HS256) // 显式指定算法
                .compact();
    }

    /**
     * 生成Refresh Token（优化版）
            * @param username 用户名（允许null，兼容异常场景）
            * @param userId 用户ID（允许null，兼容异常场景）
            * @param deviceId 设备ID（允许null，避免空值导致JWT生成失败）
            * @return 有效的Refresh Token字符串
     */
    public String generateRefreshToken(String username, Long userId, String deviceId) {
        // 1. 空值兜底：避免null导致JWT claim异常
        String safeUsername = (username == null || username.isBlank()) ? "unknown" : username;
        Long safeUserId = (userId == null) ? 0L : userId;
        String safeDeviceId = (deviceId == null || deviceId.isBlank()) ? "unknown-device" : deviceId;

        // 2. 校验过期时间有效性（核心修复：避免Redis setex异常）
        long effectiveExpiration = refreshExpiration;
        if (effectiveExpiration <= 0) {
            effectiveExpiration = 604800; // 默认7天（604800秒）
            log.warn("Refresh Token过期时间配置无效（{}秒），使用默认值：7天", refreshExpiration);
        }

        // 3. 构建JWT（增强安全性+空值安全）
        try {
            return Jwts.builder()
                    .setSubject(safeUsername) // sub：用户名（兜底后非空）
                    .claim("userId", safeUserId) // 用户ID（兜底为0L，避免null）
                    .claim("tokenType", "refresh") // 标记令牌类型，用于校验
                    .claim("deviceId", safeDeviceId) // 设备ID（兜底后非空）
                    .claim("jti", UUID.randomUUID().toString()) // 唯一标识，用于精准注销
                    .setIssuedAt(new Date()) // 签发时间
                    // 过期时间：毫秒级计算（避免时间戳错误）
                    .setExpiration(new Date(System.currentTimeMillis() + effectiveExpiration * 1000))
                    .signWith(getKey(), SignatureAlgorithm.HS256) // 显式指定密钥+算法
                    .compact();
        } catch (Exception e) {
            // 4. 异常捕获：避免JWT生成失败导致服务中断
            log.error("生成Refresh Token失败，username={}, userId={}", username, userId, e);
            throw new RuntimeException("生成刷新令牌失败", e); // 抛自定义业务异常更佳
        }
    }

    /**
     * 解析Token（带完整异常处理）
     * @param token JWT令牌
     * @return Claims载荷（解析失败返回null）
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (MalformedJwtException e) {
            log.error("JWT格式错误：{}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT已过期：{}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT类型：{}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT参数为空或无效：{}", e.getMessage());
        } catch (SignatureException e) {
            log.error("JWT签名验证失败：{}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT解析异常：{}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取Token中的用户名（兼容Access/Refresh Token）
     * @param token Access/Refresh Token
     * @return 用户名（null则返回空字符串）
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        // 1. 优先读 Access Token 的 "username" claim
        String username = claims.get("username", String.class);
        // 2. 若为null，读 Refresh Token 的 "sub"（Subject）字段
        if (username == null || username.isBlank()) {
            username = claims.getSubject();
        }
        // 3. 兜底空值（避免返回null）
        return username == null ? "" : username;
    }

    /**
     * 获取用户ID（封装空指针保护）
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("userId", Long.class) : null;
    }

    /**
     * 获取角色（封装空指针保护）
     */
    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("role", String.class) : null;
    }

    /**
     * 获取令牌唯一标识jti（用于注销）
     */
    public String getJti(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("jti", String.class) : null;
    }

    /**
     * 获取令牌类型（access/refresh）
     */
    public String getTokenType(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("tokenType", String.class) : null;
    }

    /**
     * 验证令牌是否过期（封装空指针保护）
     */
    public boolean isExpired(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return true; // 解析失败视为过期
        }
        return claims.getExpiration().before(new Date());
    }

    /**
     * 获取Refresh Token剩余有效期（毫秒）
     * @param refreshToken 刷新令牌
     * @return 剩余时间（毫秒），过期返回0
     */
    public long getRemainingTime(String refreshToken) {
        try {
            Claims claims = parseToken(refreshToken);
            if (claims == null) return 0;
            Date expireDate = claims.getExpiration();
            return Math.max(0, expireDate.getTime() - System.currentTimeMillis());
        } catch (Exception e) {
            log.error("解析Refresh Token剩余时间失败", e);
            return 0;
        }
    }

    /**
     * 验证Refresh Token是否匹配用户和设备（增强安全校验）
     */
    public boolean validateRefreshToken(String token, Long userId, String deviceId) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        // 校验令牌类型为refresh
        if (!"refresh".equals(claims.get("tokenType"))) {
            return false;
        }
        // 校验用户ID匹配
        if (!userId.equals(claims.get("userId"))) {
            return false;
        }
        // 校验设备标识（若传入则校验）
        if (deviceId != null && !deviceId.equals(claims.get("deviceId"))) {
            return false;
        }
        // 校验是否过期
        return !isExpired(token);
    }
}