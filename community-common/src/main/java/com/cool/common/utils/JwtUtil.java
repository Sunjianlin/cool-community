package com.cool.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

public class JwtUtil {
    
    public static String createToken(String secretKey, long expireTime, Map<String, Object> claims) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireTime * 1000))
                .signWith(key)
                .compact();
    }

    public static Claims parseToken(String secretKey, String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean validateToken(String secretKey, String token) {
        try {
            parseToken(secretKey, token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Long getUserId(String secretKey, String token) {
        Claims claims = parseToken(secretKey, token);
        return claims.get("userId", Long.class);
    }

    public static String getUsername(String secretKey, String token) {
        Claims claims = parseToken(secretKey, token);
        return claims.get("username", String.class);
    }
}
