package com.example.homemaking.config;


import com.example.homemaking.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

   @Autowired
   private JwtUtil jwtUtil;
   @Autowired
   private StringRedisTemplate stringRedisTemplate;
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/admin/login",//登录
            "/admin/register",// 注册
            "/admin/verification",// 验证
            "/admin/send-code",// 发送验证码
            "/admin/package/list",// 获取套餐列表
            "/upload-avatar",// 上传头像
            "/upload-package-cover",// 上传套餐封面
            "/ws",// WebSocket
            "/admin/reset-password/send-code",// 重置密码发送验证码
            "/admin/reset-password",// 重置密码
            "/admin/package/list"// 获取套餐列表
    );
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        String requestURI = httpRequest.getRequestURI();
        for (String excludeUrl : EXCLUDE_URLS){
            if (requestURI.contains(excludeUrl)){
                log.info("JWT过滤器放行: {}", requestURI);
                filterChain.doFilter(servletRequest,servletResponse);
                return;
            }
        }

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                if (isTokenActive(token)) {
                    filterChain.doFilter(httpRequest, httpResponse);
                    return;
                } else {
                    log.warn("Token已被新登录覆盖（被踢下线），请求URI: {}", requestURI);
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.getWriter().write("{\"code\":401,\"message\":\"账号已在其他设备登录，请重新登录\"}");
                    return;
                }
            }
        }
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.setContentType("application/json;charset=UTF-8");
        httpResponse.getWriter().write("{\"code\":401,\"message\":\"未登录或令牌已过期\"}");

    }

    /**
     * 检查token是否为当前活跃token（与Redis中存储的一致）
     * 若Redis中不存在或token不匹配，说明已被新登录踢下线
     */
    private boolean isTokenActive(String token) {
        try {
            Claims claims = jwtUtil.getClaimsFromToken(token);
            if (claims == null) {
                return false;
            }
            String userId = (String) claims.get("userId");
            String role = (String) claims.get("role");
            String redisKey = "login:token:" + role + ":" + userId;
            String activeToken = stringRedisTemplate.opsForValue().get(redisKey);
            return token.equals(activeToken);
        } catch (Exception e) {
            log.error("Redis校验token失败，放行请求: {}", e.getMessage());
            return true;
        }
    }
}