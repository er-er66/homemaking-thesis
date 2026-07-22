package com.example.homemaking.controller.admin;

import com.example.homemaking.result.Result;
import com.example.homemaking.services.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@Slf4j
public class VerificationCode {

    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 发送验证码（通过URL参数传递手机号）
     * @param phone 手机号
     * @return
     */
    @PostMapping("/user/send-code")
    public Result<String> sendCodeByParam(@RequestParam String phone) {
        log.info("发送验证码，phone={}", phone);
        String code = "";
        for (int i = 0; i < 6; i++) {
            code += (int) (Math.random() * 10);
        }
        verificationCodeService.save(phone, code);
        log.info("向手机{}发送验证码：{}", phone, code);
        return Result.success(code);
    }

    /**
     * 发送验证码（通过请求体传递参数）
     * @param params 包含phone的参数Map
     * @return
     */
    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        log.info("发送验证码，phone={}", phone);
        String code = "";
        for (int i = 0; i < 6; i++) {
            code += (int) (Math.random() * 10);
        }
        verificationCodeService.save(phone, code);
        log.info("向手机{}发送验证码：{}", phone, code);
        return Result.success(code);
    }

    /**
     * 验证验证码
     * @param params 包含phone和code的参数Map
     * @return
     */
    @PostMapping("/verify-code")
    public Result<Boolean> verifyCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");
        log.info("验证验证码，phone={}, code={}", phone, code);
        boolean verified = verificationCodeService.verify(phone, code);
        if (verified) {
            verificationCodeService.remove(phone);
            return Result.success(true);
        }
        return Result.error("验证码错误");
    }
}