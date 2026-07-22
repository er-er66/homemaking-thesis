package com.example.homemaking.controller.registerRequest;

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
public class RegisterRequest {
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

        log.info("用户注册：{}", registerRequestDTO.getRole());
        log.info("用户注册：{}", registerRequestDTO.getUsername());
        log.info("用户注册：{}", phone);
        log.info("用户注册Code：{}", code);
        log.info("用户注册getAccount：{}", registerRequestDTO.getAccount());
        log.info("用户注册：{}", registerRequestDTO.getPassword());
        log.info("用户注册：{}", registerRequestDTO.getConfirmPassword());
        log.info("用户注册Skills：{}", registerRequestDTO.getSkills());
        log.info("用户注册Gender：{}", registerRequestDTO.getGender());
        log.info("用户注册Avatar：{}", registerRequestDTO.getAvatar());
        Boolean msg = registerRequestService.register(registerRequestDTO);

        if (msg) {
            return Result.success("注册成功");
        } else {
            return Result.error("注册失败");
        }
    }
}