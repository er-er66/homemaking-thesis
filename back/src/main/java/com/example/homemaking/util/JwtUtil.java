package com.example.homemaking.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    //@Value读取配置文件中的jwt.secret
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;

    //获取密钥
public SecretKey getSingleKey(){
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
}


public String generateToken(Long Id, String account, String role) {
    String userId = Id.toString();
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("account", account);
    claims.put("role", role);
    return Jwts.builder()
            .subject(userId)
            .claims(claims)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
            .signWith(getSingleKey())
            .compact();
}


    public Claims parseToken(String token){
    return Jwts.parser()
            .verifyWith(getSingleKey())//判断生成的签名是否一致
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }


    /**
     * 根据已解析的Claims判断是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    /**
     * 校验token是否有效（签名合法 + 未过期）
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}