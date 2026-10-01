package com.example.homemaking.services;

public interface VerificationCodeService {
    void save(String phone, String code);
    /**
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果
     */
    boolean verify(String phone, String code);
    void remove(String phone);
}
