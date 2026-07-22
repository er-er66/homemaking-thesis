package com.example.homemaking.services;

public interface VerificationCodeService {
    void save(String phone, String code);
    boolean verify(String phone, String code);
    void remove(String phone);
}
