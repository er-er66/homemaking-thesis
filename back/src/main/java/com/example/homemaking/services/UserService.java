package com.example.homemaking.services;

import com.example.homemaking.dto.AddressSaveDTO;
import com.example.homemaking.entity.SysUser;
import com.example.homemaking.entity.UserAddress;

import java.time.LocalDateTime;
import java.util.List;

public interface UserService {
    /**
     * 获取所有用户信息
     *
     * @return
     */
    List<SysUser> getAllUser();

    /**
     * 根据id查询用户账号
     *
     * @return
     */
    SysUser getUserById(Long id);

    /**
     * 根据id禁用或启用员工账号
     *
     * @return
     */
    int setUserStatus(Long id);

    /**
     * 模糊查询用户列表
     *
     * @param name      用户名（模糊匹配）
     * @param phone     手机号（模糊匹配）
     * @param startTime 创建时间开始
     * @param endTime   创建时间结束
     * @return 用户列表
     */
    List<SysUser> searchUsers(String name, String phone, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据账号查询用户
     *
     * @param account 账号
     * @return 用户
     */
    SysUser getUserByAccount(String account);

    /**
     * 设置支付密码
     *
     * @param sysUser 用户实体
     * @return
     */
    int updateUserPassword(SysUser sysUser);

    /**
     * 修改手机号
     *
     * @param sysUser 用户实体
     * @return
     */
    int updateUserPhone(SysUser sysUser);

    /**
     * 保存地址
     *
     * @param addressSaveDTO 地址保存DTO
     * @return
     */
    int saveAddress(AddressSaveDTO addressSaveDTO);
    /**
     * 获取地址列表
     * @param userAccount 用户账号
     * @return
     */
    List<UserAddress> getAddressList(Long userAccount);
    /**
     * 修改地址
     * @param addressSaveDTO 地址保存DTO
     * @return
     */
    int updateAddress(AddressSaveDTO addressSaveDTO);
/**
 * 删除地址
 * @param id 地址id
 * @return
 */
    int deleteAddress(Long id);
}