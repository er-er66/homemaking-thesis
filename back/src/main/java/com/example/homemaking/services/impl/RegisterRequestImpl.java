package com.example.homemaking.services.impl;

import com.example.homemaking.dto.RegisterRequestDTO;
import com.example.homemaking.entity.SysStaff;
import com.example.homemaking.entity.SysUser;
import com.example.homemaking.mapper.RegisterRequestMapper;
import com.example.homemaking.services.RegisterRequestService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegisterRequestImpl implements RegisterRequestService {
    @Autowired
    private RegisterRequestMapper registerRequestMapper;

    @Override
    public Boolean register(RegisterRequestDTO registerRequestDTO) {

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
            SysUser user = new SysUser();
            BeanUtils.copyProperties(registerRequestDTO, user);
            user.setUsername(registerRequestDTO.getUsername());
            user.setCreateTime(now);
            user.setUpdateTime(now);
            if (gender.equals("0")){
                user.setGender(0);
            }else if (gender.equals("1")){
                user.setGender(1);
            }
            user.setStatus(1);
            user.setIsDeleted(0);
            return registerRequestMapper.insertSys_User(user) != null;
        } else if ("02".equals(role)) {
            SysStaff staff = new SysStaff();
            BeanUtils.copyProperties(registerRequestDTO, staff);
            staff.setAccount(registerRequestDTO.getAccount());
            staff.setUsername(registerRequestDTO.getUsername());
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
            return registerRequestMapper.insertSys_Staff(staff) != null;
        }
        return false;
    }
}