package com.example.homemaking.controller.registerRequestController;

import com.example.homemaking.dto.RegisterRequestDTO;
import com.example.homemaking.result.Result;
import com.example.homemaking.services.RegisterRequestService;
import com.example.homemaking.services.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RegisterRequestController {
    @Autowired
    private RegisterRequestService registerRequestService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @RequestMapping("/admin/register")
    public Result<String> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        String phone = registerRequestDTO.getPhone();
        String code = registerRequestDTO.getCode();

        if (!verificationCodeService.verify(phone, code)) {
            return Result.error("验证码错误");
        }
        verificationCodeService.remove(phone);

     String token = registerRequestService.register(registerRequestDTO);

        if ("PHONE_EXISTS".equals(token)) {
            return Result.error("手机号已存在");
        } else if (token != null) {
            return Result.success(token);
        } else {
            return Result.error("注册失败");
        }
    }
}