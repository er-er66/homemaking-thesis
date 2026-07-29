package com.example.homemaking.services.impl;

import com.example.homemaking.services.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final String REDIS_KEY_PREFIX = "verify:code:";
    private static final long CODE_EXPIRE_SECONDS = 60;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void save(String phone, String code) {
        stringRedisTemplate.opsForValue().set(
                REDIS_KEY_PREFIX + phone,
                code,
                CODE_EXPIRE_SECONDS,
                TimeUnit.SECONDS
        );
        log.info("验证码已存入Redis: phone={}, code={}, 过期时间={}秒", phone, code, CODE_EXPIRE_SECONDS);
    }

    /**
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果
     */
    public boolean verify(String phone, String code) {
        String saved = stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + phone);
        return saved != null && saved.equals(code);
    }

    /**
     * 删除Redis中的验证码
     * @param phone 手机号
     */
    public void remove(String phone) {
        stringRedisTemplate.delete(REDIS_KEY_PREFIX + phone);
        log.info("验证码已从Redis删除: phone={}", phone);
    }
}