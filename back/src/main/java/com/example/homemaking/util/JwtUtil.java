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
    @Value("${jwt.expiration}")//单位ms
    private Long expiration;

    //获取密钥
public SecretKey getSingleKey(){
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
}

    /**
     * 生成token
     * @param Id
     * @param account
     * @param role
     * @return
     */
    public String generateToken(Long Id, String account, String role) {
    String userId = Id.toString();
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("account", account);
    claims.put("role", role);
    return Jwts.builder()
            .subject(userId)//1主题，这里使用userId
            .claims(claims)//2. 放用户信息
            .issuedAt(new Date())   // 3. 签发时间
            .expiration(new Date(System.currentTimeMillis() + expiration * 1000))// 4. 过期时间
            .signWith(getSingleKey())     // 5. 签名 ★ 核心
            .compact(); // 6. 输出字符串 ★ 核心
}

    /**
     * 解析token
     * 仅在token签名合法且未过期时返回Claims，否则返回null
     * @param token
     * @return
     */
    public Claims parseToken(String token){
    return Jwts.parser()
            .verifyWith(getSingleKey())//判断生成的签名是否一致
            .build()//构建解析器
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

    /**
     * 从token中解析Claims，用于获取userId、role等信息
     * 仅在token签名合法且未过期时返回Claims，否则返回null
     */
    public Claims getClaimsFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims.getExpiration().before(new Date())) {
                return null;
            }
            return claims;
        } catch (Exception e) {
            return null;
        }
    }
}