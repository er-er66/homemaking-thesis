package com.example.homemaking.mapper;

import com.example.homemaking.entity.SysAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminMapper {
    /**
     * 根据id查询管理员信息
     * @param id 管理员ID
     * @return 管理员信息
     */
    SysAdmin getAdminById(Long id);

    /**
     * 模糊查询管理员列表
     * @param name 用户名（模糊匹配）
     * @param phone 手机号（模糊匹配）
     * @param startTime 创建时间开始
     * @param endTime 创建时间结束
     * @return 管理员列表
     */
    List<SysAdmin> searchAdmins(@Param("name") String name, @Param("phone") String phone,
                                @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据id启用/禁用管理员
     * @param id 管理员ID
     * @return 受影响的行数
     */
    int toggleAdminStatus(Long id);
}