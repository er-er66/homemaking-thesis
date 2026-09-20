package com.example.homemaking.services.impl;

import com.example.homemaking.dto.ResetPasswordDTO;
import com.example.homemaking.entity.SysAdmin;
import com.example.homemaking.entity.SysStaff;
import com.example.homemaking.mapper.AdminMapper;
import com.example.homemaking.mapper.EmpMapper;
import com.example.homemaking.mapper.UserMapper;
import com.example.homemaking.services.ResetPasswordService;
import com.example.homemaking.services.VerificationCodeService;
import com.example.homemaking.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class resetPasswordServiceImpl implements ResetPasswordService {
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private EmpMapper empMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 重置密码
     *
     * @param resetPasswordDTO
     * @return
     */
    @Override
    public int resetPassword(ResetPasswordDTO resetPasswordDTO) {
        //1.判断当前验证吗是否正确或过期
        boolean verified = verificationCodeService.verify(resetPasswordDTO.getPhone(), resetPasswordDTO.getCode());
        if (!verified) {
            return 0;//验证码错误或过期
        }

        //2.根据重置身份类型来判断手机号是否存在
        if (resetPasswordDTO.getRole().equals("001")) {
            //管理员
            int count = adminMapper.selectCountByPhone(resetPasswordDTO.getPhone());
            if (count == 0) {
                return 0;//管理员不存在
            }
            //3.判断密码长度是否小于6或为空
            String newPassword = resetPasswordDTO.getNewPassword();
            if (newPassword == null||newPassword.length()<6) {
                return 0;//密码长度小于6或为空
            }
            //4.修改密码（BCrypt hash 入库）
            return adminMapper.updatePassword(resetPasswordDTO.getPhone(), PasswordUtil.encode(newPassword));
        } else if (resetPasswordDTO.getRole().equals("002")) {
            //员工
            int count = empMapper.selectCountByPhone(resetPasswordDTO.getPhone());
            if (count == 0) {
                return 0;//员工不存在
            }
            //3.判断密码长度是否小于6或为空
            String newPassword = resetPasswordDTO.getNewPassword();
            if (newPassword == null||newPassword.length()<6) {
                return 0;//密码长度小于6或为空
            }
            //4.修改密码（BCrypt hash 入库）
            return empMapper.updatePassword(resetPasswordDTO.getPhone(), PasswordUtil.encode(newPassword));
        } else if (resetPasswordDTO.getRole().equals("003")) {
            //用户
            int count = userMapper.selectCountByPhone(resetPasswordDTO.getPhone());
            if (count == 0) {
                return 0;//用户不存在
            }
            //3.判断密码长度是否小于6或为空
            String newPassword = resetPasswordDTO.getNewPassword();
            if (newPassword == null||newPassword.length()<6) {
                return 0;//密码长度小于6或为空
            }
            //4.修改密码（BCrypt hash 入库）
            return userMapper.updatePassword(resetPasswordDTO.getPhone(), PasswordUtil.encode(newPassword));
        }else {
            return 0;//重置身份类型错误
        }

    }

}