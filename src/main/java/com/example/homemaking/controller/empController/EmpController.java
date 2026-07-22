package com.example.homemaking.controller.empController;

import com.example.homemaking.entity.SysStaff;
import com.example.homemaking.result.Result;
import com.example.homemaking.services.EmpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/admin/emp")
@Slf4j
public class EmpController {
    @Autowired
    private EmpService empService;

    /**
     * 获取所有员工信息（支持模糊查询）
     * @param name 用户名（模糊匹配）
     * @param phone 手机号（模糊匹配）
     * @param startTime 创建时间开始
     * @param endTime 创建时间结束
     * @return
     */
    @GetMapping()
    public Result<List<SysStaff>> emp(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime) {
        LocalDateTime startDateTime = startTime != null ? startTime.atStartOfDay() : null;
        LocalDateTime endDateTime = endTime != null ? endTime.atTime(LocalTime.MAX) : null;
        log.info("模糊查询员工列表，name={}, phone={}, startTime={}, endTime={}", name, phone, startDateTime, endDateTime);
        List<SysStaff> empList = empService.searchEmps(name, phone, startDateTime, endDateTime);
        return Result.success(empList);
    }

    /**
     * 根据id禁用或启用员工账号
     * @return
     */
    @PutMapping("/{id}/status")
    public Result<String> setEmpStatus(@PathVariable Long id) {
        log.info("禁用或启用员工账号，id={}", id);
        int count = empService.setEmpStatus(id);
        log.info("受影响的行数：{}", count);
        if (count > 0) {
            return Result.success("状态切换成功");
        } else {
            return Result.error("状态切换失败，员工不存在");
        }
    }
    //http://localhost:5173/admin/emp/6
    //请求方法
    //GET
    /**
     * 根据id查询员工信息
     * @return
     */

    @GetMapping("/{id}")
    public Result<SysStaff> empById(@PathVariable Long id) {
        log.info("查询员工信息，id={}", id);
        SysStaff emp = empService.getEmpById(id);
        log.info("员工信息：{}", emp);
        return Result.success(emp);
    }

}