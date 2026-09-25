package com.example.homemaking.mapper;

import com.example.homemaking.dto.StaffPackageRefDTO;
import com.example.homemaking.entity.HomemakingPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SysStaffPackageMapper {

    /**
     * 批量绑定员工可服务套餐
     * <p>调用方必须保证 packageIds 非空且已去重，否则会撞 uk_staff_package 唯一约束</p>
     *
     * @param staffAccount 家政人员账号
     * @param packageIds   套餐ID集合
     * @param createTime   创建时间
     * @return 受影响行数
     */
    int batchInsert(@Param("staffAccount") String staffAccount,
                    @Param("packageIds") List<Long> packageIds,
                    @Param("createTime") LocalDateTime createTime);

    /**
     * 解绑某个员工的全部套餐
     *
     * @param staffAccount 家政人员账号
     * @return 受影响行数
     */
    int deleteByStaffAccount(@Param("staffAccount") String staffAccount);

    /**
     * 查询某个员工绑定的套餐ID
     *
     * @param staffAccount 家政人员账号
     */
    List<Long> selectPackageIdsByStaffAccount(@Param("staffAccount") String staffAccount);

    /**
     * 查询某个员工绑定的套餐明细
     *
     * @param staffAccount 家政人员账号
     */
    List<HomemakingPackage> selectPackagesByStaffAccount(@Param("staffAccount") String staffAccount);

    /**
     * 批量查询多个员工的套餐绑定关系（列表展示用，一次查询代替 N+1）
     *
     * @param staffAccounts 家政人员账号集合
     */
    List<StaffPackageRefDTO> selectPackagesByStaffAccounts(@Param("staffAccounts") List<String> staffAccounts);
}
