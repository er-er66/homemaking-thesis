package com.example.homemaking.services.impl;

import com.example.homemaking.dto.AddressSaveDTO;
import com.example.homemaking.dto.PageResult;
import com.example.homemaking.entity.SysUser;
import com.example.homemaking.entity.UserAddress;
import com.example.homemaking.mapper.SysUserAddressMapper;
import com.example.homemaking.mapper.UserMapper;
import com.example.homemaking.services.UserService;
import com.example.homemaking.util.PageUtil;
import com.example.homemaking.util.PasswordUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysUserAddressMapper sysUserAddressMapper;

    /**
     * 获取所有用户信息
     * @return
     */
    @Override
    public List<SysUser> getAllUser() {
        List<SysUser> sysUserList = userMapper.selectAll();
        return sysUserList;
    }

    /**
     * 根据id查询用户账号
     * @return
     */
    @Override
    public SysUser getUserById(Long id) {
        return userMapper.getUserById(id);
    }

    /**
     * 根据id禁用或启用员工账号
     * @return
     */
    @Override
    public int setUserStatus(Long id) {
        return userMapper.setUserStatus(id);
    }

    /**
     * 模糊查询用户列表
     * @param name 用户名（模糊匹配）
     * @param account 账号（模糊匹配）
     * @param phone 手机号（模糊匹配）
     * @param startTime 创建时间开始
     * @param endTime 创建时间结束
     * @return 用户列表
     */
    @Override
    public List<SysUser> searchUsers(String name, String account, String phone, LocalDateTime startTime, LocalDateTime endTime) {
        return userMapper.searchUsers(name, account, phone, startTime, endTime);
    }

    /**
     * 分页查询用户列表
     */
    @Override
    public PageResult<SysUser> searchUsersPage(String name, String account, String phone, LocalDateTime startTime, LocalDateTime endTime,
                                               Integer pageNum, Integer pageSize) {
        int num = PageUtil.normalizePageNum(pageNum);
        int size = PageUtil.normalizePageSize(pageSize);
        long total = userMapper.countUsers(name, account, phone, startTime, endTime);
        List<SysUser> records = total == 0 ? List.of()
                : userMapper.searchUsersPage(name, account, phone, startTime, endTime, PageUtil.offset(num, size), size);
        return PageResult.of(total, num, size, records);
    }

    /**
     * 根据账号查询用户
     * @param account
     * @return
     */
    @Override
    public SysUser getUserByAccount(String account) {
        return userMapper.getUserByAccount(account);
    }

    /**
     * 修改支付密码
     * <p>统一在 Service 层做 BCrypt 加密，调用方传明文即可，
     * 防止某个调用方漏了 encode 把明文写进库。</p>
     *
     * @param sysUser
     * @return
     */
    @Override
    public int updateUserPassword(SysUser sysUser) {
        if (sysUser == null || sysUser.getPayPassword() == null || sysUser.getPayPassword().isEmpty()) {
            return 0;
        }
        //已经是 BCrypt hash 的（如内部二次调用）不重复加密
        if (!PasswordUtil.isEncoded(sysUser.getPayPassword())) {
            sysUser.setPayPassword(PasswordUtil.encode(sysUser.getPayPassword()));
        }
        return userMapper.updateUserPassword(sysUser);
    }
    /**
     * 修改手机号
     * @param sysUser
     * @return
     */

    @Override
    public int updateUserPhone(SysUser sysUser) {
        return userMapper.updateUserPhone(sysUser);
    }

    /**
     * 修改用户名
     * @param sysUser
     * @return
     */
    @Override
    public int updateUserName(SysUser sysUser) {
        return userMapper.updateUserName(sysUser);
    }

    /**
     * 保存地址
     * @param addressSaveDTO 地址保存DTO
     * @return
     */
    @Override
    public int saveAddress(AddressSaveDTO addressSaveDTO) {
        UserAddress userAddress = new UserAddress();
        BeanUtils.copyProperties(addressSaveDTO,userAddress);//属性复制
        userAddress.setCreateTime(LocalDateTime.now());
        userAddress.setUpdateTime(LocalDateTime.now());
        userAddress.setIsDeleted(0);
        return userMapper.saveAddress(userAddress);
    }
    /**
     * 获取地址列表
     * @param userAccount 用户账号
     * @return
     */
    @Override
    public List<UserAddress> getAddressList(Long userAccount) {
        return userMapper.getAddressList(userAccount);
    }
    /**
     * 修改地址
     * @param addressSaveDTO 地址保存DTO
     * @return
     */
    @Override
    public int updateAddress(AddressSaveDTO addressSaveDTO) {
        UserAddress userAddress = new UserAddress();
        BeanUtils.copyProperties(addressSaveDTO,userAddress);
        userAddress.setUpdateTime(LocalDateTime.now());
        userAddress.setIsDeleted(0);
        userAddress.setId(addressSaveDTO.getId());
        return sysUserAddressMapper.updateAddress(userAddress);
    }
    /**
     * 删除地址
     * @param id 地址id
     * @return
     */
    @Override
    public int deleteAddress(Long id) {
        return sysUserAddressMapper.deleteAddress(id);
    }
}