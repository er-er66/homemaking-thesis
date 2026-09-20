package com.example.homemaking.services.impl;

import com.example.homemaking.config.ChatWebSocketHandler;
import com.example.homemaking.dto.LoginRequestDTO;
import com.example.homemaking.entity.SysAdmin;
import com.example.homemaking.entity.SysStaff;
import com.example.homemaking.entity.SysUser;
import com.example.homemaking.mapper.LoginMapper;
import com.example.homemaking.services.LoginService;
import com.example.homemaking.util.JwtUtil;
import com.example.homemaking.util.PasswordUtil;
import com.example.homemaking.vo.LoginResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {
        @Autowired
        private LoginMapper loginMapper;
        @Autowired
        private JwtUtil jwtUtil;
        @Autowired
        private StringRedisTemplate stringRedisTemplate;
        @Autowired
        private ChatWebSocketHandler chatWebSocketHandler;
        @Value("${jwt.expiration}")
        private Long jwtExpiration;
        @Override
        public Object login(LoginRequestDTO loginRequestDTO) {
            //"000"管理员,"001"普通用户,"002"家政人员
            // vo.setRoleCode("10");返回10表示超级管理员，01普通管理员，02家政人员，03普通用户
            String phone = loginRequestDTO.getPhone();
            String account = loginRequestDTO.getAccount();
            String password = loginRequestDTO.getPassword();
            String role = loginRequestDTO.getRole();
            log.info("登录请求 -> phone={}, account={}, role={}", phone, account, role);
            if (password == null || password.isEmpty()) {
                log.warn("登录失败：密码为空，account={}, role={}", account, role);
                return null;
            }
            if (!hasIdentifier(phone, account)) {
                log.warn("登录失败：手机号和账号都为空，role={}", role);
                return null;
            }
            if ("000".equals(role)) {
                SysAdmin admin = loginMapper.selectSys_Admin(phone, account);
                //密码在内存中做 BCrypt 校验，不再作为 SQL 条件
                if (!PasswordUtil.matches(password, admin == null ? null : admin.getPassword())) {
                    log.warn("管理员登录失败：账号或密码错误，account={}, phone={}", account, phone);
                    return null;
                }
                LoginResultVO vo = new LoginResultVO();
                vo.setToken(jwtUtil.generateToken(admin.getId(), admin.getAccount(), role));
                vo.setUsername(admin.getUsername());
                vo.setAvatar(admin.getAvatar());
                String adminRole = admin.getRole();
                if (adminRole != null && adminRole.startsWith("1")){
                    vo.setRoleCode("10");
                    vo.setAccount(admin.getAccount());
                    vo.setPhone(admin.getPhone());
                }else {
                    vo.setRoleCode("01");
                    vo.setAccount(admin.getAccount());
                    vo.setPhone(admin.getPhone());
                }
                log.info("管理员登录成功，account={}, roleCode={}", admin.getAccount(), vo.getRoleCode());
                saveTokenToRedis(admin.getId(), role, vo.getToken());
                closeOldWebSocket(admin.getAccount());
                return vo;
            } else if ("001".equals(role)) {
                SysUser user = loginMapper.selectSys_User(phone, account);
                if (!PasswordUtil.matches(password, user == null ? null : user.getPassword())) {
                    log.warn("普通用户登录失败：账号或密码错误，account={}, phone={}", account, phone);
                    return null;
                }
                LoginResultVO vo = new LoginResultVO();
                vo.setToken(jwtUtil.generateToken(user.getId(), user.getAccount(), role));
                vo.setUsername(user.getUsername());
                vo.setAvatar(user.getAvatar());
                vo.setRoleCode("03");
                vo.setAccount(user.getAccount());
                vo.setPhone(user.getPhone());
                log.info("普通用户登录成功，account={}", user.getAccount());
                saveTokenToRedis(user.getId(), role, vo.getToken());
                closeOldWebSocket(user.getAccount());
                return vo;
            } else if ("002".equals(role)) {
                SysStaff staff = loginMapper.selectSys_Staff(phone, account);
                if (!PasswordUtil.matches(password, staff == null ? null : staff.getPassword())) {
                    log.warn("家政人员登录失败：账号或密码错误，account={}, phone={}", account, phone);
                    return null;
                }
                LoginResultVO vo = new LoginResultVO();
                vo.setToken(jwtUtil.generateToken(staff.getId(), staff.getAccount(), role));
                vo.setUsername(staff.getUsername());
                vo.setAvatar(staff.getAvatar());
                vo.setRoleCode("02");
                vo.setAccount(staff.getAccount());
                vo.setPhone(staff.getPhone());
                log.info("家政人员登录成功，account={}", staff.getAccount());
                saveTokenToRedis(staff.getId(), role, vo.getToken());
                closeOldWebSocket(staff.getAccount());
                return vo;
            }
            log.warn("登录失败：未知角色 role={}", role);
            return null;
        }

        /**
         * 手机号与账号至少有一个非空，否则 SQL 会退化成全表扫描
         */
        private boolean hasIdentifier(String phone, String account) {
            return (phone != null && !phone.isEmpty()) || (account != null && !account.isEmpty());
        }

        private void saveTokenToRedis(Long userId, String role, String token) {
            try {
                String redisKey = "login:token:" + role + ":" + userId;
                stringRedisTemplate.opsForValue().set(redisKey, token, jwtExpiration, TimeUnit.SECONDS);
                log.info("Token已存入Redis, key={}", redisKey);
            } catch (Exception e) {
                log.error("Token存入Redis失败: {}", e.getMessage());
            }
        }

        private void closeOldWebSocket(String account) {
            try {
                chatWebSocketHandler.closeSessionByUserId(account);
            } catch (Exception e) {
                log.error("关闭旧WebSocket连接失败: account={}, error={}", account, e.getMessage());
            }
        }
    }
