package com.example.homemaking.mapper;

import com.example.homemaking.entity.Order;
import com.example.homemaking.entity.SysStaffAmount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface SysStaffAmountMapper {

    /**
     * 记一笔收入：账户不存在则新建，存在则累加
     * <p>total_income 与 pending_amount 同时增加；total_income 只增不减，便于对账。</p>
     *
     * @param staffId 家政人员ID（sys_staff.id）
     * @param income  本次收入金额
     * @return 受影响行数（1=新建，2=更新）
     */
    int upsertIncome(@Param("staffId") Long staffId, @Param("income") BigDecimal income);

    /**
     * 结算：待结算金额转已结算金额
     *
     * @param staffId 家政人员ID
     * @param amount  结算金额
     * @return 受影响行数，0 表示账户不存在或已删除
     */
    int addSettled(@Param("staffId") Long staffId, @Param("amount") BigDecimal amount);

    /**
     * 按家政人员ID查资金账户（join sys_staff 带出工号与姓名）
     */
    SysStaffAmount selectByStaffId(@Param("staffId") Long staffId);

    /**
     * 按家政人员工号查资金账户
     */
    SysStaffAmount selectByStaffAccount(@Param("staffAccount") String staffAccount);

    /**
     * 资金账户分页列表
     *
     * @param staffAccount 工号模糊匹配，可空
     * @param staffName    姓名模糊匹配，可空
     * @param offset       limit 起始下标，null 表示不分页
     * @param limit        每页条数
     */
    List<SysStaffAmount> search(@Param("staffAccount") String staffAccount,
                                @Param("staffName") String staffName,
                                @Param("offset") Integer offset,
                                @Param("limit") Integer limit);

    /**
     * 资金账户总数，条件必须与 {@link #search} 完全一致，否则 total 与 records 不符
     */
    long count(@Param("staffAccount") String staffAccount,
               @Param("staffName") String staffName);

    /**
     * 工号 → 家政人员ID（sys_staff.id），查不到返回 null
     */
    Long selectStaffIdByAccount(@Param("account") String account);

    /**
     * 按订单号查记账所需的最小订单信息
     * <p>只取记账需要的列：service_time 是 datetime，映射到 String 会多一次隐式转换，这里不取。</p>
     */
    Order selectOrderByNo(@Param("orderNo") String orderNo);
}
