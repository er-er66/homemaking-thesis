package com.example.homemaking.mapper;

import com.example.homemaking.dto.RegisterRequestDTO;
import com.example.homemaking.entity.SysAdmin;
import com.example.homemaking.entity.SysStaff;
import com.example.homemaking.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RegisterRequestMapper {
    int insertSys_User(SysUser sysUser);

    int insertSys_Staff(SysStaff sysStaff);

    int countByPhoneUser(String phone);

    int countByPhoneStaff(String phone);
}