package com.example.homemaking.controller.userController;

import com.example.homemaking.dto.AddressSaveDTO;
import com.example.homemaking.dto.PageResult;
import com.example.homemaking.entity.SysUser;
import com.example.homemaking.entity.UserAddress;
import com.example.homemaking.result.Result;
import com.example.homemaking.services.UserService;
import com.example.homemaking.services.VerificationCodeService;
import com.example.homemaking.util.PageUtil;
import com.example.homemaking.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 获取用户列表（支持模糊查询）
     * <p>不传 pageNum/pageSize：返回全量数组（旧行为）</p>
     * <p>传 pageNum/pageSize：返回 {total,pageNum,pageSize,records} 分页体</p>
     *
     * @param name      用户名（模糊匹配）
     * @param phone     手机号（模糊匹配）
     * @param startTime 创建时间开始
     * @param endTime   创建时间结束
     * @param pageNum   页码，从 1 开始
     * @param pageSize  每页条数
     * @return
     */
    @GetMapping("/users")
    public Result<?> users(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {
        LocalDateTime startDateTime = startTime != null ? startTime.atStartOfDay() : null;
        LocalDateTime endDateTime = endTime != null ? endTime.atTime(LocalTime.MAX) : null;
        log.info("查询用户列表，name={}, phone={}, startTime={}, endTime={}, pageNum={}, pageSize={}",
                name, phone, startDateTime, endDateTime, pageNum, pageSize);

        if (!PageUtil.enabled(pageNum, pageSize)) {
            List<SysUser> list = userService.searchUsers(name, phone, startDateTime, endDateTime);
            maskPasswords(list);
            return Result.success(list);
        }
        PageResult<SysUser> page = userService.searchUsersPage(name, phone, startDateTime, endDateTime, pageNum, pageSize);
        maskPasswords(page.getRecords());
        return Result.success(page);
    }

    /**
     * 列表里的密码字段一律清空后再下发
     */
    private void maskPasswords(List<SysUser> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        for (SysUser u : users) {
            if (u != null) {
                u.setPassword(null);
                u.setPayPassword(null);
            }
        }
    }

    /**
     * 根据id查询用户账号
     *
     * @return
     */
    @GetMapping("/user/{id}")
    public Result<SysUser> UserById(@PathVariable Long id) {
        log.info("查询用户信息，id={}", id);
        SysUser sysUser = userService.getUserById(id);
        if (sysUser != null) {
            sysUser.setPassword(null);
            sysUser.setPayPassword(null);
        }
        return Result.success(sysUser);
    }

    /**
     * 根据id禁用或启用用户账号
     *
     * @return
     */
    @PutMapping("/user/{id}/status")
    public Result<String> setUserStatus(@PathVariable Long id) {
        log.info("禁用或启用用户账号，id={}", id);
        int count = userService.setUserStatus(id);
        log.info("受影响的行数：{}", count);
        if (count > 0) {
            return Result.success("状态切换成功");
        } else {
            return Result.error("状态切换失败，用户不存在");
        }
    }

    /**
     * 根据账号检查支付密码是否存在
     *
     * @param account 账号
     * @return
     */
    @GetMapping("/user/pay-password/check")
    public Result<Boolean> checkPayPassword(@RequestParam String account) {
        log.info("检查支付密码是否存在，account={}", account);
        SysUser sysUser = userService.getUserByAccount(account);
        if (sysUser != null && sysUser.getPayPassword() != null && !sysUser.getPayPassword().isEmpty()) {
            return Result.success(true);
        } else {
            return Result.error("支付密码未设置");
        }
    }

    /**
     * 设置支付密码
     *
     * @param account 账号
     * @param newPwd  新支付密码
     * @return
     */
    @PostMapping("/user/pay-password/set")
    public Result<String> setPayPassword(@RequestParam String account, @RequestParam String newPwd) {
        //不能把密码打进日志
        log.info("设置支付密码，account={}", account);
        if (newPwd == null || newPwd.length() < 6) {
            return Result.error("支付密码长度不能小于6位");
        }
        SysUser sysUser = userService.getUserByAccount(account);
        if (sysUser != null) {
            //BCrypt hash 入库，不存明文
            sysUser.setPayPassword(PasswordUtil.encode(newPwd));
            int count = userService.updateUserPassword(sysUser);
            log.info("设置支付密码受影响的行数：{}", count);
            if (count > 0) {
                return Result.success("支付密码设置成功");
            }
        }
        return Result.error("用户不存在");
    }

    /**
     * 修改支付密码（需校验旧密码）
     *
     * @param account 账号
     * @param oldPwd  旧支付密码（明文）
     * @param newPwd  新支付密码（明文）
     * @return
     */
    @PutMapping("/user/pay-password/update")
    public Result<String> updatePayPassword(@RequestParam String account,
                                            @RequestParam String oldPwd,
                                            @RequestParam String newPwd) {
        //不能把密码打进日志
        log.info("修改支付密码，account={}", account);
        if (newPwd == null || newPwd.length() < 6) {
            return Result.error("新支付密码长度不能小于6位");
        }
        SysUser sysUser = userService.getUserByAccount(account);
        if (sysUser == null) {
            return Result.error("用户不存在");
        }
        //旧密码走 BCrypt 校验，不比对明文
        if (!PasswordUtil.matches(oldPwd, sysUser.getPayPassword())) {
            return Result.error("原支付密码错误");
        }
        sysUser.setPayPassword(newPwd);
        int count = userService.updateUserPassword(sysUser);
        log.info("修改支付密码受影响的行数：{}", count);
        if (count > 0) {
            return Result.success("支付密码修改成功");
        }
        return Result.error("支付密码修改失败");
    }

    /**
     * 修改手机号
     *
     * @param account  账号
     * @param role     角色（user/staff）
     * @param newPhone 新手机号
     * @param code     验证码
     * @return
     */
    @PostMapping("/user/change-phone")
    public Result<String> changePhone(
            @RequestParam String account,
            @RequestParam String role,
            @RequestParam String newPhone,
            @RequestParam String code) {
        log.info("修改手机号，account={}, role={}, newPhone={}, code={}", account, role, newPhone, code);

        // 验证验证码
        boolean verified = verificationCodeService.verify(newPhone, code);
        if (!verified) {
            return Result.error("验证码错误");
        }

        // 验证成功后删除验证码
        verificationCodeService.remove(newPhone);

        // 根据角色修改手机号
        if ("03".equals(role)) {
            SysUser sysUser = userService.getUserByAccount(account);
            if (sysUser != null) {
                sysUser.setPhone(newPhone);
                int count = userService.updateUserPhone(sysUser);
                if (count > 0) {
                    return Result.success("手机号修改成功");
                }
            }
            return Result.error("用户不存在");
        }

        return Result.error("未知角色");
    }

    @PostMapping("/user/change-name")
    public Result<String> changeName(
            @RequestParam String account,
            @RequestParam String role,
            @RequestParam String newName) {
        log.info("修改用户名，account={}, role={}, newName={}", account, role, newName);

        // 根据角色修改用户名
        if ("03".equals(role)) {
            SysUser sysUser = userService.getUserByAccount(account);
            if (sysUser != null) {
                sysUser.setUsername(newName);
                int count = userService.updateUserName(sysUser);
                if (count > 0) {
                    return Result.success("用户名修改成功");
                } else {
                    return Result.error("用户名修改失败");
                }
            }
    }
        return Result.error("用户不存在");

    }

    /**
     * 保存地址
     *
     * @param addressSaveDTO 地址保存DTO
     * @return
     */
    @PostMapping("user/address/save")
    public Result<String> saveAddress(@RequestBody AddressSaveDTO addressSaveDTO) {
        log.info("保存地址，addressSaveDTO={}", addressSaveDTO);
        int count = userService.saveAddress(addressSaveDTO);
        if (count > 0) {
            return Result.success("地址保存成功");
        } else {
            return Result.error("地址保存失败");
        }
    }

    /**
     * 获取地址列表
     *
     * @param userAccount 用户账号
     * @return
     */
    @GetMapping("/user/address/list")
    public Result<List<UserAddress>> getAddressList(@RequestParam Long userAccount) {
        log.info("获取地址列表，userAccount={}", userAccount);
        List<UserAddress> addressList = userService.getAddressList(userAccount);
        return Result.success(addressList);
    }

    /**
     * 修改地址
     *
     * @param addressSaveDTO 地址保存DTO
     * @return
     */
    @PostMapping("user/address/update")
    public Result<String> updateAddress(@RequestBody AddressSaveDTO addressSaveDTO) {
        log.info("修改地址，addressSaveDTO={}", addressSaveDTO);
        int count = userService.updateAddress(addressSaveDTO);
        if (count > 0) {
            return Result.success("地址更新成功");
        } else {
            return Result.error("地址更新失败");
        }
    }

    /**
     * 删除地址
     *
     * @param id 地址id
     * @return
     */
    @PostMapping("user/address/delete")
    public Result<String> deleteAddress(@RequestParam Long id) {
        log.info("删除地址，id={}", id);
        int count = userService.deleteAddress(id);
        if (count > 0) {
            return Result.success("地址删除成功");
        } else {
            return Result.error("地址删除失败");
        }
    }

    @GetMapping("/user/account/{account}")
    public Result<SysUser> getUserByAccount(@PathVariable String account) {
        log.info("查询用户信息，account={}", account);
        SysUser sysUser = userService.getUserByAccount(account);
        if (sysUser == null) {
            return Result.error("用户不存在");
        }
        //密码字段一律不下发（BCrypt hash 也不能出服务器）
        sysUser.setPassword(null);
        sysUser.setPayPassword(null);
        return Result.success(sysUser);
    }

}