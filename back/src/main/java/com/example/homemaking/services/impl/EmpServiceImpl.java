package com.example.homemaking.services.impl;

import com.example.homemaking.dto.PageResult;
import com.example.homemaking.entity.SysStaff;
import com.example.homemaking.mapper.EmpMapper;
import com.example.homemaking.services.EmpService;
import com.example.homemaking.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmpServiceImpl implements EmpService {
    @Autowired//自动依赖注入
    private EmpMapper empMapper;
    /**
     * 获取所有员工信息
     * @return
     */
    @Override
    public List<SysStaff> getAllEmp() {
        return empMapper.getAllEmp();
    }
    /**
     * 根据id禁用或启用员工账号
     * @return 受影响的行数
     */
    @Override
    public int setEmpStatus(Long id) {
      return empMapper.setEmpStatus(id);
    }
    /**
     * 根据id查询员工信息
     * @return
     */
    @Override
    public SysStaff getEmpById(Long id) {
        return empMapper.getEmpById(id);
    }

    @Override
    public List<SysStaff> searchEmps(String name, String account, String phone, LocalDateTime startTime, LocalDateTime endTime) {
        return empMapper.searchEmps(name, account, phone, startTime, endTime);
    }

    /**
     * 分页查询员工列表
     */
    @Override
    public PageResult<SysStaff> searchEmpsPage(String name, String account, String phone, LocalDateTime startTime, LocalDateTime endTime,
                                               Integer pageNum, Integer pageSize) {
        int num = PageUtil.normalizePageNum(pageNum);
        int size = PageUtil.normalizePageSize(pageSize);
        long total = empMapper.countEmps(name, account, phone, startTime, endTime);
        List<SysStaff> records = total == 0 ? List.of()
                : empMapper.searchEmpsPage(name, account, phone, startTime, endTime, PageUtil.offset(num, size), size);
        return PageResult.of(total, num, size, records);
    }
}