package com.example.homemaking.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码工具
 * <p>统一收敛密码 hash / 校验 / 明文迁移逻辑，避免各处自建 encoder。</p>
 * <p>BCrypt hash 固定 60 字符，形如 {@code $2a$10$...}；数据库列 varchar(100) 容量充足。</p>
 */
public final class PasswordUtil {

    /**
     * 静态实例，供非 Spring 管理的场景（如启动时刷存量密码）使用
     */
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(10);

    /** BCrypt hash 的最小长度，用于识别库里存的到底是不是 hash */
    private static final int HASH_MIN_LENGTH = 60;

    private PasswordUtil() {
    }

    /**
     * 加密（每次调用产生不同 salt，同一明文两次结果不同，属正常）
     *
     * @param rawPassword 明文密码，为空时原样返回
     * @return BCrypt hash
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return rawPassword;
        }
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验密码，自动兼容库中尚未迁移的明文
     * <p>先走 BCrypt matches；若库里仍是明文（长度不足 60 或 matches 失败），
     * 再退化为常量时间字符串比较。全部存量数据刷成 hash 后，明文分支自然失效。</p>
     *
     * @param rawPassword 用户提交的明文
     * @param storedValue 库中存储的值，可能是 hash 也可能是历史明文
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String storedValue) {
        if (rawPassword == null || storedValue == null || storedValue.isEmpty()) {
            return false;
        }
        // 库里存的像 BCrypt hash：只走 BCrypt 校验，不给明文回退留后门
        if (storedValue.length() >= HASH_MIN_LENGTH && storedValue.startsWith("$2")) {
            return ENCODER.matches(rawPassword, storedValue);
        }
        // 历史明文数据：常量时间比较，避免逐字符提前返回带来的时序差异
        return constantTimeEquals(rawPassword, storedValue);
    }

    /**
     * 判断库中存储的值是否已经是 BCrypt hash
     *
     * @param storedValue 库中存储的值
     * @return true=已是 hash
     */
    public static boolean isEncoded(String storedValue) {
        return storedValue != null
                && storedValue.length() >= HASH_MIN_LENGTH
                && storedValue.startsWith("$2");
    }

    /**
     * 常量时间字符串比较
     */
    private static boolean constantTimeEquals(String a, String b) {
        byte[] x = a.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] y = b.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (x.length != y.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < x.length; i++) {
            diff |= x[i] ^ y[i];
        }
        return diff == 0;
    }
}
