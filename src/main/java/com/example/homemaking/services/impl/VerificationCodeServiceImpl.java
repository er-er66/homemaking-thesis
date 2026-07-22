package com.example.homemaking.services.impl;

import com.example.homemaking.services.VerificationCodeService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final Map<String, String> codeMap = new ConcurrentHashMap<>();

    public void save(String phone, String code) {
        codeMap.put(phone, code);
    }

    public boolean verify(String phone, String code) {
        String saved = codeMap.get(phone);
        return saved != null && saved.equals(code);
    }

    public void remove(String phone) {
        codeMap.remove(phone);
    }
}
