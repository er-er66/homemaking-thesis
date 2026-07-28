package com.example.homemaking.services.impl;

import com.example.homemaking.dto.LoginRequestDTO;
import com.example.homemaking.entity.SysAdmin;
import com.example.homemaking.entity.SysStaff;
import com.example.homemaking.entity.SysUser;
import com.example.homemaking.mapper.LoginMapper;
import com.example.homemaking.services.LoginService;
import com.example.homemaking.util.JwtUtil;
import com.example.homemaking.vo.LoginResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {
        @Autowired
        private LoginMapper loginMapper;
        @Autowired
        private JwtUtil jwtUtil;
        @Override
        public Object login(LoginRequestDTO loginRequestDTO) {
            //"000"管理员,"001"普通用户,"002"家政人员
            // vo.setRoleCode("10");返回10表示超级管理员，01普通管理员，02家政人员，03普通用户
            String phone = loginRequestDTO.getPhone();
            String account = loginRequestDTO.getAccount();
            String password = loginRequestDTO.getPassword();
            String role = loginRequestDTO.getRole();
            log.info("登录请求 -> phone={}, account={}, role={}", phone, account, role);
            if ("000".equals(role)) {
                SysAdmin admin = loginMapper.selectSys_Admin(phone, account, password);
                log.info("查询管理员结果: {}", admin);
                if (admin != null) {
                    LoginResultVO vo = new LoginResultVO();
                    vo.setToken(jwtUtil.generateToken(admin.getId(), admin.getAccount(), role));
                    vo.setUsername(admin.getUsername());
                    vo.setAvatar(admin.getAvatar());
                    String adminRole = admin.getRole();
                    if (adminRole != null && adminRole.startsWith("1")){
                        vo.setRoleCode("10");
                        vo.setAccount(admin.getAccount());
                        vo.setPhone(admin.getPhone());
                    }else {
                        vo.setRoleCode("01");
                        vo.setAccount(admin.getAccount());
                        vo.setPhone(admin.getPhone());
                    }
                    log.info("管理员登录成功，返回: {}", vo);
                    return vo;
                }
            } else if ("001".equals(role)) {
                SysUser user = loginMapper.selectSys_User(phone, account, password);
                if (user != null) {
                    LoginResultVO vo = new LoginResultVO();
                    vo.setToken(jwtUtil.generateToken(user.getId(), user.getAccount(), role));
                    vo.setUsername(user.getUsername());
                    vo.setAvatar(user.getAvatar());
                    vo.setRoleCode("03");
                    vo.setAccount(user.getAccount());
                    vo.setPhone(user.getPhone());
                    return vo;
                }
            } else if ("002".equals(role)) {
                SysStaff staff = loginMapper.selectSys_Staff(phone, account, password);
                if (staff != null) {
                    LoginResultVO vo = new LoginResultVO();
                    vo.setToken(jwtUtil.generateToken(staff.getId(), staff.getAccount(), role));
                    vo.setUsername(staff.getUsername());
                    vo.setAvatar(staff.getAvatar());
                    vo.setRoleCode("02");
                    vo.setAccount(staff.getAccount());
                    vo.setPhone(staff.getPhone());
                    return vo;
                }
            }
            return null;
        }
    }