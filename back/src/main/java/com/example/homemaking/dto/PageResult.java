package com.example.homemaking.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 通用分页返回体
 * 前端据 data 中是否存在 records 判定是否已启用服务端分页
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 当前页码，从 1 开始 */
    private int pageNum;

    /** 每页条数 */
    private int pageSize;

    /** 当前页数据 */
    private List<T> records;

    public static <T> PageResult<T> of(long total, int pageNum, int pageSize, List<T> records) {
        PageResult<T> r = new PageResult<>();
        r.setTotal(total);
        r.setPageNum(pageNum);
        r.setPageSize(pageSize);
        r.setRecords(records);
        return r;
    }
}
