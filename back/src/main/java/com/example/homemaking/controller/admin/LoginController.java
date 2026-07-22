package com.example.homemaking.controller.admin;

import com.example.homemaking.dto.LoginRequestDTO;
import com.example.homemaking.entity.SysAdmin;
import com.example.homemaking.result.Result;
import com.example.homemaking.services.LoginService;
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
    @RequestMapping("/admin/login")
    public Result<Object> login(@RequestBody LoginRequestDTO loginRequestDTO) {
       Object flag = loginService.login(loginRequestDTO);
       if(flag!=null){
           log.info("loginService 返回数据：flag = {}", flag);
           return Result.success(flag);

       }else {
           return Result.error("登录失败");
       }

    }
}