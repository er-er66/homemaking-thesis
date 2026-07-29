package com.example.homemaking.controller.admin;

import com.example.homemaking.dto.LoginRequestDTO;
import com.example.homemaking.dto.ResetPasswordDTO;
import com.example.homemaking.entity.SysAdmin;
import com.example.homemaking.result.Result;
import com.example.homemaking.services.LoginService;
import com.example.homemaking.services.ResetPasswordService;
import com.example.homemaking.services.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class LoginController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private ResetPasswordService resetPasswordService;

    @RequestMapping("/admin/login")
    public Result<Object> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        Object flag = loginService.login(loginRequestDTO);
        if (flag != null) {
            log.info("loginService 返回数据：flag = {}", flag);
            return Result.success(flag);

        } else {
            return Result.error("登录失败");
        }

    }

    @RequestMapping("/admin/reset-password")
    public Result<String> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        log.info("resetPasswordDTO = {}", resetPasswordDTO);
        int count = resetPasswordService.resetPassword(resetPasswordDTO);
        if (count == 0) {
            return Result.error("重置密码失败");
        }
        return Result.success("重置密码成功");
    }
}