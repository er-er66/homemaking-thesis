package com.example.homemaking.services.impl;

import com.example.homemaking.dto.PageResult;
import com.example.homemaking.entity.SysAdmin;
import com.example.homemaking.mapper.AdminMapper;
import com.example.homemaking.services.AdminService;
import com.example.homemaking.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private AdminMapper adminMapper;

    /**
     * 根据id查询管理员详情
     * @param id
     * @return
     */
    @Override
    public SysAdmin getAdminById(Long id) {
        return adminMapper.getAdminById(id);
    }

    /**
     * 获取管理员列表（支持模糊查询）
     * @param name
     * @param phone
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public List<SysAdmin> searchAdmins(String name, String phone, LocalDateTime startTime, LocalDateTime endTime) {
        return adminMapper.searchAdmins(name, phone, startTime, endTime);
    }

    /**
     * 分页查询管理员列表
     */
    @Override
    public PageResult<SysAdmin> searchAdminsPage(String name, String phone, LocalDateTime startTime, LocalDateTime endTime,
                                                 Integer pageNum, Integer pageSize) {
        int num = PageUtil.normalizePageNum(pageNum);
        int size = PageUtil.normalizePageSize(pageSize);
        long total = adminMapper.countAdmins(name, phone, startTime, endTime);
        List<SysAdmin> records = total == 0 ? List.of()
                : adminMapper.searchAdminsPage(name, phone, startTime, endTime, PageUtil.offset(num, size), size);
        return PageResult.of(total, num, size, records);
    }
/**
 * 启用/禁用管理员
 * @param id
 * @return
 */
    @Override
    public int toggleAdminStatus(Long id) {
        return adminMapper.toggleAdminStatus(id);
    }

    /**
     * 修改管理员名称
     * @param account 管理员账号
     * @param newName 新名称
     * @return 操作结果
     */
    @Override
    public int changeName(String account, String newName) {
        return adminMapper.changeName(account, newName);
    }
}