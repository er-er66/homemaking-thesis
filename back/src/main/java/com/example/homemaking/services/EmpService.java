package com.example.homemaking.services;

import com.example.homemaking.dto.PageResult;
import com.example.homemaking.entity.SysStaff;

import java.time.LocalDateTime;
import java.util.List;

public interface EmpService {
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
    List<SysStaff> searchEmps(String name, String account, String phone, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分页查询员工列表
     *
     * @param name      用户名（模糊匹配）
     * @param phone     手机号（模糊匹配）
     * @param startTime 创建时间开始
     * @param endTime   创建时间结束
     * @param pageNum   页码，从 1 开始
     * @param pageSize  每页条数
     * @return 分页结果
     */
    PageResult<SysStaff> searchEmpsPage(String name, String account, String phone, LocalDateTime startTime, LocalDateTime endTime,
                                        Integer pageNum, Integer pageSize);
}