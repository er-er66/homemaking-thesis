package com.example.homemaking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置
 * <p>只引入 spring-security-crypto 这一个 jar，不会触发 Spring Security 的
 * 全站过滤器链（未引入 spring-boot-starter-security）。</p>
 */
@Configuration
public class PasswordConfig {

    /**
     * BCrypt 强度 10（默认），单次 hash 约 50~100ms，登录场景可接受
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
