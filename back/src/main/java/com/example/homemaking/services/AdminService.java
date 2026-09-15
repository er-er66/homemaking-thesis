package com.example.homemaking.services;

import com.example.homemaking.dto.PageResult;
import com.example.homemaking.entity.SysAdmin;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminService {
    /**
     * 获取管理员列表（支持模糊查询）
     * @param name 用户名（模糊匹配）
     * @param phone 手机号（模糊匹配）
     * @param startDateTime 创建时间开始
     * @param endDateTime 创建时间结束
     * @return 管理员列表
     */
    List<SysAdmin> searchAdmins(String name, String phone, LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * 分页查询管理员列表
     *
     * @param name          用户名（模糊匹配）
     * @param phone         手机号（模糊匹配）
     * @param startDateTime 创建时间开始
     * @param endDateTime   创建时间结束
     * @param pageNum       页码，从 1 开始
     * @param pageSize      每页条数
     * @return 分页结果
     */
    PageResult<SysAdmin> searchAdminsPage(String name, String phone, LocalDateTime startDateTime, LocalDateTime endDateTime,
                                          Integer pageNum, Integer pageSize);

    /**
     * 根据id查询管理员详情
     * @param id 管理员ID
     * @return 管理员详情
     */
    SysAdmin getAdminById(Long id);

    /**
     * 根据id启用/禁用管理员
     * @param id 管理员ID
     * @return 操作结果
     */
    int toggleAdminStatus(Long id);

    /**
     * 修改管理员名称
     * @param account 管理员账号
     * @param newName 新名称
     * @return 操作结果
     */
    int changeName(String account, String newName);
}
