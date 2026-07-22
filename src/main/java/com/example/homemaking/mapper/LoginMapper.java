package com.example.homemaking.mapper;

import com.example.homemaking.entity.SysAdmin;
import com.example.homemaking.entity.SysStaff;
import com.example.homemaking.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginMapper {

    SysAdmin selectSys_Admin(String phone,String account, String password);// 查询sys_admin表


    SysUser selectSys_User(String phone,String account, String password);// 查询sys_user表

    SysStaff selectSys_Staff(String phone,String account, String password);
}
