package com.example.homemaking.mapper;

import com.example.homemaking.entity.SysAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminMapper {
    /**
     * 根据id查询管理员信息
     * @param id 管理员ID
     * @return 管理员信息
     */
    SysAdmin getAdminById(Long id);

    /**
     * 模糊查询管理员列表
     * @param name 用户名（模糊匹配）
     * @param phone 手机号（模糊匹配）
     * @param startTime 创建时间开始
     * @param endTime 创建时间结束
     * @return 管理员列表
     */
    List<SysAdmin> searchAdmins(@Param("name") String name, @Param("phone") String phone,
                                @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 按条件统计管理员总数
     *
     * @param name      用户名（模糊匹配）
     * @param phone     手机号（模糊匹配）
     * @param startTime 创建时间开始
     * @param endTime   创建时间结束
     * @return 总记录数
     */
    long countAdmins(@Param("name") String name, @Param("phone") String phone,
                     @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 按条件分页查询管理员
     *
     * @param offset   起始下标
     * @param pageSize 每页条数
     * @return 当前页管理员列表
     */
    List<SysAdmin> searchAdminsPage(@Param("name") String name, @Param("phone") String phone,
                                    @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime,
                                    @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 根据id启用/禁用管理员
     * @param id 管理员ID
     * @return 受影响的行数
     */
    int toggleAdminStatus(Long id);

    /**
     * 根据手机号查询管理员数量
     * @param phone 手机号
     * @return 数量
     */
    int selectCountByPhone(String phone);

    /**
     * 修改管理员密码（入参 newPassword 必须是已 BCrypt 加密的 hash）
     * @param phone 手机号
     * @param newPassword BCrypt hash
     * @return 受影响的行数
     */
    int updatePassword(@Param("phone") String phone, @Param("newPassword") String newPassword);

    /**
     * 修改管理员名称
     * @param account 管理员账号
     * @param newName 新名称
     * @return 受影响的行数
     */
    int changeName(String account, String newName);
}