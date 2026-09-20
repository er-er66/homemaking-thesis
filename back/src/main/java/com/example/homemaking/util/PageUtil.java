package com.example.homemaking.util;

/**
 * 分页参数规整工具
 * <p>前端双模式分页约定：pageNum/pageSize 同时传才启用服务端分页，
 * 任一为空则由调用方回退到旧的全量数组行为。</p>
 */
public final class PageUtil {

    /** 默认每页条数 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 每页条数上限，防止一次拉取过多数据 */
    public static final int MAX_PAGE_SIZE = 200;

    private PageUtil() {
    }

    /**
     * 是否启用服务端分页：pageNum 与 pageSize 都传了才算
     */
    public static boolean enabled(Integer pageNum, Integer pageSize) {
        return pageNum != null && pageSize != null;
    }

    /**
     * 规整页码，非法值（null / <1）按 1 处理
     */
    public static int normalizePageNum(Integer pageNum) {
        return (pageNum == null || pageNum < 1) ? 1 : pageNum;
    }

    /**
     * 规整每页条数，非法值（null / <1 / >上限）按默认值处理
     */
    public static int normalizePageSize(Integer pageSize) {
        return (pageSize == null || pageSize < 1 || pageSize > MAX_PAGE_SIZE) ? DEFAULT_PAGE_SIZE : pageSize;
    }

    /**
     * 由页码和每页条数换算 limit 起始下标
     */
    public static int offset(int pageNum, int pageSize) {
        return (pageNum - 1) * pageSize;
    }
}
