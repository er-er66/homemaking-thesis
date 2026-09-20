package com.example.homemaking.controller.empController;

import com.example.homemaking.dto.PageResult;
import com.example.homemaking.entity.SysStaff;
import com.example.homemaking.result.Result;
import com.example.homemaking.services.EmpService;
import com.example.homemaking.util.PageUtil;
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
     * 获取员工列表（支持模糊查询）
     * <p><b>不传 pageNum/pageSize 时返回全量数组</b> —— 派单弹窗的员工下拉依赖该行为，不能删</p>
     * <p>传 pageNum/pageSize 时返回 {total,pageNum,pageSize,records} 分页体</p>
     *
     * @param name      用户名（模糊匹配）
     * @param phone     手机号（模糊匹配）
     * @param startTime 创建时间开始
     * @param endTime   创建时间结束
     * @param pageNum   页码，从 1 开始
     * @param pageSize  每页条数
     * @return
     */
    @GetMapping()
    public Result<?> emp(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        LocalDateTime startDateTime = startTime != null ? startTime.atStartOfDay() : null;
        LocalDateTime endDateTime = endTime != null ? endTime.atTime(LocalTime.MAX) : null;
        log.info("查询员工列表，name={}, phone={}, startTime={}, endTime={}, pageNum={}, pageSize={}",
                name, phone, startDateTime, endDateTime, pageNum, pageSize);

        if (!PageUtil.enabled(pageNum, pageSize)) {
            List<SysStaff> list = empService.searchEmps(name, phone, startDateTime, endDateTime);
            maskPasswords(list);
            return Result.success(list);
        }
        PageResult<SysStaff> page = empService.searchEmpsPage(name, phone, startDateTime, endDateTime, pageNum, pageSize);
        maskPasswords(page.getRecords());
        return Result.success(page);
    }

    /**
     * 列表里的密码字段一律清空后再下发
     */
    private void maskPasswords(List<SysStaff> staffList) {
        if (staffList == null || staffList.isEmpty()) {
            return;
        }
        for (SysStaff s : staffList) {
            if (s != null) {
                s.setPassword(null);
            }
        }
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
        log.info("查询员工信息，id={}", id);
        if (emp != null) {
            emp.setPassword(null);
        }
        return Result.success(emp);
    }

}