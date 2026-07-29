package com.example.homemaking.services;

import com.example.homemaking.dto.ResetPasswordDTO;

public interface ResetPasswordService {
    /**
     * 重置密码
     *
     * @param resetPasswordDTO 重置密码信息
     * @return 0: 验证码错误或过期 1: 重置成功
     */
    int resetPassword(ResetPasswordDTO resetPasswordDTO);
}
