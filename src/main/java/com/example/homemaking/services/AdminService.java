package com.example.homemaking.services;

import com.example.homemaking.entity.SysAdmin;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminService {
    List<SysAdmin> searchAdmins(String name, String phone, LocalDateTime startDateTime, LocalDateTime endDateTime);

    SysAdmin getAdminById(Long id);

    int toggleAdminStatus(Long id);
}
