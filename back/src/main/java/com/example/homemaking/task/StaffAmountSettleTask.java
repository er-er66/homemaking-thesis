package com.example.homemaking.task;

import com.example.homemaking.services.StaffAmountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 家政人员资金自动结算
 * <p>订单完成满 N 天后，把待结算金额转入已结算。
 * N 由 {@code staff.settle.delay-days} 配置，执行时间由 {@code staff.settle.cron} 配置。</p>
 * <p>主启动类已有 {@code @EnableScheduling}，无需额外开启。</p>
 */
@Slf4j
@Component
public class StaffAmountSettleTask {

    @Autowired
    private StaffAmountService staffAmountService;

    /**
     * 默认每天凌晨 1 点执行
     */
    @Scheduled(cron = "${staff.settle.cron:0 0 1 * * ?}")
    public void settleDaily() {
        log.info("资金自动结算任务开始");
        try {
            int count = staffAmountService.settlePending(null);
            log.info("资金自动结算任务结束，结算 {} 笔", count);
        } catch (Exception e) {
            // 定时任务吞掉异常，避免调度线程被中断后不再触发后续执行
            log.error("资金自动结算任务失败", e);
        }
    }
}
