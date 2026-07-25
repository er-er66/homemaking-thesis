package com.example.homemaking.mapper;

import com.example.homemaking.entity.SysUser;
import com.example.homemaking.entity.UserAddress;
import com.example.homemaking.entity.UserImg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 获取所有用户信息
     *
     * @return
     */
    List<SysUser> selectAll();

    /**
     * 根据id查询用户账号
     *
     * @return
     */
    SysUser getUserById(Long id);

    /**
     * 获取所有用户信息
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
    List<SysUser> searchUsers(@Param("name") String name, @Param("phone") String phone,
                              @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);


    /**
     * 根据账号查询用户信息
     *
     * @param account 账号
     * @return 用户信息
     */
    SysUser getUserByAccount(String account);

    /**
     * 修改密码
     *
     * @param sysUser 用户信息
     * @return 修改结果
     */
    int updateUserPassword(SysUser sysUser);

    /**
     * 修改手机号
     *
     * @param sysUser 用户信息
     * @return 修改结果
     */
    int updateUserPhone(SysUser sysUser);

    /**
     * 保存地址
     *
     * @param userAddress
     * @return
     */
    int saveAddress(UserAddress userAddress);

    /**
     * 获取地址列表
     *
     * @param userAccount
     * @return
     */
    List<UserAddress> getAddressList(Long userAccount);

    /**
     * 支付时要获取密码
     *
     * @param payPassword
     * @return
     */
    String getAccountPassword(String payPassword);


}