package com.example.homemaking.mapper;

import com.example.homemaking.entity.SysAdmin;
import com.example.homemaking.entity.SysStaff;
import com.example.homemaking.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 登录查询
 * <p>密码不参与 SQL 条件（BCrypt 带 salt，无法等值匹配），
 * 只按 phone/account 定位记录，由 Service 层做密码校验。</p>
 */
@Mapper
public interface LoginMapper {

    SysAdmin selectSys_Admin(@Param("phone") String phone, @Param("account") String account);

    SysUser selectSys_User(@Param("phone") String phone, @Param("account") String account);

    SysStaff selectSys_Staff(@Param("phone") String phone, @Param("account") String account);
}
