package com.example.homemaking.mapper;

import com.example.homemaking.entity.SysStaff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EmpMapper {
    /**
     * 获取所有员工信息
     * @return
     */
    List<SysStaff> getAllEmp();
    /**
     * 根据id禁用或启用员工账号
     * @return 受影响的行数
     */
    int setEmpStatus(Long id);
    /**
     * 根据id查询员工信息
     * @return
     */
    SysStaff getEmpById(Long id);

    /**
     * 模糊查询员工列表
     * @param name 用户名（模糊匹配）
     * @param phone 手机号（模糊匹配）
     * @param startTime 创建时间开始
     * @param endTime 创建时间结束
     * @return 员工列表
     */
    List<SysStaff> searchEmps(@Param("name") String name, @Param("phone") String phone,
                              @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}