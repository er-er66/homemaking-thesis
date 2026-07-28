package com.example.homemaking.config;


import com.example.homemaking.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/admin/login",
            "/admin/register",
            "/admin/verification",
            "/admin/send-code",
            "/admin/package/list",
            "/upload-avatar",
            "/upload-package-cover",
            "/ws"
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
        log.info("JWT过滤器拦截: {}", requestURI);
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                filterChain.doFilter(httpRequest, httpResponse);
                return;
            }
        }
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.setContentType("application/json;charset=UTF-8");
        httpResponse.getWriter().write("{\"code\":401,\"message\":\"未登录或令牌已过期\"}");

    }
}