package com.example.homemaking.util;

public class SnowIdUtil {
    // 机器ID 0~31
    private static final long WORKER_ID = 1L;
    // 数据中心ID 0~31
    private static final long DATA_CENTER_ID = 1L;
    private static Snowflake snowflake = new Snowflake(WORKER_ID, DATA_CENTER_ID);

    public static long nextId() {
        return snowflake.nextId();
    }

    /**
     * 截取雪花ID后6位，缩短订单号长度
     */
    public static String getShortSnow() {
        long id = nextId();
        return String.format("%06d", id % 1000000);
    }

    static class Snowflake {
        private final long workerId;
        private final long dataCenterId;
        private long sequence = 0L;
        private long lastTimestamp = -1L;

        private static final long WORKER_ID_BITS = 5L;
        private static final long DATA_CENTER_ID_BITS = 5L;
        private static final long SEQUENCE_BITS = 12L;

        private static final long MAX_WORKER_ID = (1L << WORKER_ID_BITS) - 1;
        private static final long MAX_DATA_CENTER_ID = (1L << DATA_CENTER_ID_BITS) - 1;
        private static final long SEQUENCE_MASK = (1L << SEQUENCE_BITS) - 1;

        private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
        private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
        private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

        private static final long START_TIME = 1609459200000L; // 2021-01-01

        public Snowflake(long workerId, long dataCenterId) {
            if (workerId > MAX_WORKER_ID || workerId < 0)
                throw new IllegalArgumentException("workerId 超出范围");
            if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0)
                throw new IllegalArgumentException("dataCenterId 超出范围");
            this.workerId = workerId;
            this.dataCenterId = dataCenterId;
        }

        public synchronized long nextId() {
            long timestamp = System.currentTimeMillis();
            if (timestamp < lastTimestamp) {
                throw new RuntimeException("时钟回拨，无法生成ID");
            }
            if (timestamp == lastTimestamp) {
                sequence = (sequence + 1) & SEQUENCE_MASK;
                if (sequence == 0) {
                    while (timestamp <= lastTimestamp) timestamp = System.currentTimeMillis();
                }
            } else {
                sequence = 0L;
            }
            lastTimestamp = timestamp;
            return ((timestamp - START_TIME) << TIMESTAMP_SHIFT)
                    | (dataCenterId << DATA_CENTER_ID_SHIFT)
                    | (workerId << WORKER_ID_SHIFT)
                    | sequence;
        }
    }
}
