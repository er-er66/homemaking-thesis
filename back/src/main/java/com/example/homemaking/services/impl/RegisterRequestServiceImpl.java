package com.example.homemaking.services.impl;

import com.example.homemaking.dto.RegisterRequestDTO;
import com.example.homemaking.entity.SysStaff;
import com.example.homemaking.entity.SysUser;
import com.example.homemaking.mapper.RegisterRequestMapper;
import com.example.homemaking.services.RegisterRequestService;
import com.example.homemaking.util.JwtUtil;
import com.example.homemaking.util.PasswordUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegisterRequestServiceImpl implements RegisterRequestService {
    @Autowired
    private RegisterRequestMapper registerRequestMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String register(RegisterRequestDTO registerRequestDTO) {

        String gender = registerRequestDTO.getGender();

        String role = registerRequestDTO.getRole();
        String src = "5";
        for (int i = 0; i < 7; i++) {
            src += (int) (Math.random() * 10);
        }
        registerRequestDTO.setAccount(src);
        LocalDateTime now = LocalDateTime.now();
        if ("00".equals(role)) {//注册管理员

        } else if ("01".equals(role)) {
            if (registerRequestMapper.countByPhoneUser(registerRequestDTO.getPhone()) > 0) {
                return "PHONE_EXISTS";
            }
            SysUser user = new SysUser();
            BeanUtils.copyProperties(registerRequestDTO, user);
            user.setUsername(registerRequestDTO.getUsername());
            //密码以 BCrypt hash 入库，不存明文
            user.setPassword(PasswordUtil.encode(registerRequestDTO.getPassword()));
            user.setCreateTime(now);
            user.setUpdateTime(now);
            if (gender.equals("0")){
                user.setGender(0);
            }else if (gender.equals("1")){
                user.setGender(1);
            }
            user.setStatus(1);
            user.setIsDeleted(0);
            int rows = registerRequestMapper.insertSys_User(user);
            if (rows > 0) {
                return jwtUtil.generateToken(user.getId(), user.getAccount(), role);
            }
            return null;
        } else if ("02".equals(role)) {
            if (registerRequestMapper.countByPhoneStaff(registerRequestDTO.getPhone()) > 0) {
                return "PHONE_EXISTS";
            }
            SysStaff staff = new SysStaff();
            BeanUtils.copyProperties(registerRequestDTO, staff);
            staff.setAccount(registerRequestDTO.getAccount());
            staff.setUsername(registerRequestDTO.getUsername());
            //密码以 BCrypt hash 入库，不存明文
            staff.setPassword(PasswordUtil.encode(registerRequestDTO.getPassword()));
            List<String> skills = registerRequestDTO.getSkills();
            if (skills != null && !skills.isEmpty()) {
                staff.setSkills(String.join(",", skills));
            }
            if (gender.equals("0")){
                staff.setGender(0);
            }else if (gender.equals("1")){
                staff.setGender(1);
            }
            staff.setCreateTime(now);
            staff.setUpdateTime(now);
            staff.setStatus(1);

            staff.setIsDeleted(0);
            if (staff.getGender() == null) {
                staff.setGender(0);
            }
            int rows = registerRequestMapper.insertSys_Staff(staff);
            if (rows > 0) {
                return jwtUtil.generateToken(staff.getId(), staff.getAccount(), role);
            }
            return null;
        }
        return null;
    }
}