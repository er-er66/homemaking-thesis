package com.example.homemaking.mapper;

import com.example.homemaking.entity.SysStaffAmountRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SysStaffAmountRecordMapper {

    /**
     * 写入一条资金流水
     * <p>靠 uk_order_staff_type 唯一键做幂等：重复写入时走
     * {@code on duplicate key update id = id}，返回 0。</p>
     *
     * @param record 流水记录
     * @return 1=本次新写入，0=已存在（调用方应据此跳过加钱）
     */
    int insertRecord(SysStaffAmountRecord record);

    /**
     * 查询到期可结算的收入流水
     *
     * @param beforeTime 只取 create_time <= beforeTime 的记录（now - N天）
     * @param limit      单批上限，避免一次锁太多行
     */
    List<SysStaffAmountRecord> selectPending(@Param("beforeTime") LocalDateTime beforeTime,
                                             @Param("limit") int limit);

    /**
     * 标记流水已结算
     * <p>{@code and settle_status = 0} 保证并发下不会重复结算，
     * 返回 0 说明已被别的线程处理过。</p>
     */
    int markSettled(@Param("id") Long id, @Param("settleTime") LocalDateTime settleTime);

    /**
     * 流水分页查询
     *
     * @param staffId     家政人员ID，可空
     * @param staffAccount 工号模糊匹配，可空
     * @param settleStatus 结算状态，可空
     * @param recordType   流水类型，可空
     * @param offset       limit 起始下标，null 表示不分页
     * @param limit        每页条数
     */
    List<SysStaffAmountRecord> searchRecords(@Param("staffId") Long staffId,
                                             @Param("staffAccount") String staffAccount,
                                             @Param("settleStatus") Integer settleStatus,
                                             @Param("recordType") Integer recordType,
                                             @Param("offset") Integer offset,
                                             @Param("limit") Integer limit);

    /**
     * 流水总数，条件必须与 {@link #searchRecords} 完全一致
     */
    long countRecords(@Param("staffId") Long staffId,
                      @Param("staffAccount") String staffAccount,
                      @Param("settleStatus") Integer settleStatus,
                      @Param("recordType") Integer recordType);

    /**
     * 按订单号 + 员工ID查收入流水，用于排查重复记账
     */
    SysStaffAmountRecord selectByOrder(@Param("orderNo") String orderNo,
                                       @Param("staffId") Long staffId);
}
