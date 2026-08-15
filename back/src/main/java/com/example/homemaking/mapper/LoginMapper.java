package com.example.homemaking.mapper;

import com.example.homemaking.entity.SysAdmin;
import com.example.homemaking.entity.SysStaff;
import com.example.homemaking.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginMapper {

    SysAdmin selectSys_Admin(@Param("phone") String phone, @Param("account") String account, @Param("password") String password);

    SysUser selectSys_User(@Param("phone") String phone, @Param("account") String account, @Param("password") String password);

    SysStaff selectSys_Staff(@Param("phone") String phone, @Param("account") String account, @Param("password") String password);
}