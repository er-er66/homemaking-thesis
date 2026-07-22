package com.example.homemaking.services.impl;

import com.example.homemaking.dto.LoginRequestDTO;
import com.example.homemaking.entity.SysAdmin;
import com.example.homemaking.entity.SysStaff;
import com.example.homemaking.entity.SysUser;
import com.example.homemaking.mapper.LoginMapper;
import com.example.homemaking.services.LoginService;
import com.example.homemaking.vo.LoginResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {
        @Autowired
        private LoginMapper loginMapper;
        @Override
        public Object login(LoginRequestDTO loginRequestDTO) {
            //"000"管理员,"001"普通用户,"002"家政人员
            // vo.setRoleCode("10");返回10表示超级管理员，01普通管理员，02家政人员，03普通用户
            String phone = loginRequestDTO.getPhone();
            String account = loginRequestDTO.getAccount();
            String password = loginRequestDTO.getPassword();
            String role = loginRequestDTO.getRole();
            if ("000".equals(role)) {
                SysAdmin admin = loginMapper.selectSys_Admin(phone, account, password);
                if (admin != null) {
                    LoginResultVO vo = new LoginResultVO();
                    vo.setToken(admin.getAccount());
                    vo.setUsername(admin.getUsername());
                    vo.setAvatar(admin.getAvatar());
                    if ((admin.getRole().substring(0, 1)).equals("1")){
                        vo.setRoleCode("10");
                        vo.setAccount(admin.getAccount());
                        vo.setPhone(admin.getPhone());
                    }else {
                        vo.setRoleCode("01");
                        vo.setAccount(admin.getAccount());
                        vo.setPhone(admin.getPhone());
                    }
                    return vo;
                }
            } else if ("001".equals(role)) {
                SysUser user = loginMapper.selectSys_User(phone, account, password);
                if (user != null) {
                    LoginResultVO vo = new LoginResultVO();
                    vo.setToken(user.getAccount());
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
                    vo.setToken(staff.getAccount());
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

