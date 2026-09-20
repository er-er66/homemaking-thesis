package com.example.homemaking.mapper;

import com.example.homemaking.entity.SysStaff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EmpMapper {
    /**
     * 获取所有员工信息
     * @return
     */
    List<SysStaff> getAllEmp();
    /**
     * 根据id禁用或启用员工账号
     * @return 受影响的行数
     */
    int setEmpStatus(Long id);
    /**
     * 根据id查询员工信息
     * @return
     */
    SysStaff getEmpById(Long id);

    /**
     * 模糊查询员工列表
     * @param name 用户名（模糊匹配）
     * @param phone 手机号（模糊匹配）
     * @param startTime 创建时间开始
     * @param endTime 创建时间结束
     * @return 员工列表
     */
    List<SysStaff> searchEmps(@Param("name") String name, @Param("phone") String phone,
                              @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 按条件统计员工总数
     *
     * @param name      用户名（模糊匹配）
     * @param phone     手机号（模糊匹配）
     * @param startTime 创建时间开始
     * @param endTime   创建时间结束
     * @return 总记录数
     */
    long countEmps(@Param("name") String name, @Param("phone") String phone,
                   @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 按条件分页查询员工
     *
     * @param offset   起始下标
     * @param pageSize 每页条数
     * @return 当前页员工列表
     */
    List<SysStaff> searchEmpsPage(@Param("name") String name, @Param("phone") String phone,
                                  @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime,
                                  @Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 根据手机号查询员工数量(重置密码需要)
     * @param phone 手机号
     * @return 员工数量
     */
    int selectCountByPhone(String phone);

    /**
     * 修改密码（入参 newPassword 必须是已 BCrypt 加密的 hash）
     * @param phone 手机号
     * @param newPassword BCrypt hash
     * @return 修改结果
     */
    int updatePassword(@Param("phone") String phone, @Param("newPassword") String newPassword);
}