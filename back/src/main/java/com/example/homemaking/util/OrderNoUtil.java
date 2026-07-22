package com.example.homemaking.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrderNoUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 生成家政订单号：HM + 年月日时分秒 + 6位雪花短码
     * 示例：HM20260707172530452189
     */
    public static String generateOrderNo() {
        String timeStr = LocalDateTime.now().format(FORMATTER);
        String shortSnow = SnowIdUtil.getShortSnow();
        return "HM" + timeStr + shortSnow;
    }
}
