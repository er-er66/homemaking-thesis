package com.example.homemaking.services.impl;

import com.example.homemaking.entity.SysAdmin;
import com.example.homemaking.mapper.AdminMapper;
import com.example.homemaking.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private AdminMapper adminMapper;

    @Override
    public SysAdmin getAdminById(Long id) {
        return adminMapper.getAdminById(id);
    }

    @Override
    public List<SysAdmin> searchAdmins(String name, String phone, LocalDateTime startTime, LocalDateTime endTime) {
        return adminMapper.searchAdmins(name, phone, startTime, endTime);
    }

    @Override
    public int toggleAdminStatus(Long id) {
        return adminMapper.toggleAdminStatus(id);
    }
}