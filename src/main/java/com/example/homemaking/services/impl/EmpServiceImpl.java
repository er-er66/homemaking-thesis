package com.example.homemaking.services.impl;

import com.example.homemaking.entity.SysStaff;
import com.example.homemaking.mapper.EmpMapper;
import com.example.homemaking.services.EmpService;
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
    public List<SysStaff> searchEmps(String name, String phone, LocalDateTime startTime, LocalDateTime endTime) {
        return empMapper.searchEmps(name, phone, startTime, endTime);
    }
}