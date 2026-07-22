package com.example.homemaking.controller.adminController;

import com.example.homemaking.entity.SysAdmin;
import com.example.homemaking.result.Result;
import com.example.homemaking.services.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {
    @Autowired
    private AdminService adminService;

    /**
     * 获取管理员列表（支持模糊查询）
     * @param name 用户名（模糊匹配）
     * @param phone 手机号（模糊匹配）
     * @param startTime 创建时间开始
     * @param endTime 创建时间结束
     * @return 管理员列表
     */
    @GetMapping("/admins")
    public Result<List<SysAdmin>> getAdminList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime) {
        LocalDateTime startDateTime = startTime != null ? startTime.atStartOfDay() : null;
        LocalDateTime endDateTime = endTime != null ? endTime.atTime(LocalTime.MAX) : null;
        log.info("查询管理员列表，name={}, phone={}, startTime={}, endTime={}", name, phone, startDateTime, endDateTime);
        List<SysAdmin> adminList = adminService.searchAdmins(name, phone, startDateTime, endDateTime);
        return Result.success(adminList);
    }

    /**
     * 根据id查询管理员详情
     * @param id 管理员ID
     * @return 管理员详情
     */
    @GetMapping("/admin/{id}")
    public Result<SysAdmin> getAdminDetail(@PathVariable Long id) {
        log.info("查询管理员详情，id={}", id);
        SysAdmin admin = adminService.getAdminById(id);
        if (admin != null) {
            return Result.success(admin);
        } else {
            return Result.error("管理员不存在");
        }
    }

    /**
     * 根据id启用/禁用管理员
     * @param id 管理员ID
     * @return 操作结果
     */
    @PutMapping("/admin/{id}/status")
    public Result<String> toggleAdminStatus(@PathVariable Long id) {
        log.info("启用/禁用管理员，id={}", id);
        int count = adminService.toggleAdminStatus(id);
        if (count > 0) {
            return Result.success("状态切换成功");
        } else {
            return Result.error("状态切换失败，管理员不存在");
        }
    }
}