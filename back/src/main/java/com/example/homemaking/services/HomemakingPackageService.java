package com.example.homemaking.services;


import com.example.homemaking.dto.HomemakingPackageDTO;
import com.example.homemaking.dto.PageResult;
import com.example.homemaking.entity.HomemakingPackage;

import java.util.List;


public interface HomemakingPackageService {
    /**
     * 创建套餐
     * @param homemakingPackageDTO
     * @return
     */
    String create(HomemakingPackageDTO homemakingPackageDTO);


    /**
     * 获取套餐列表
     * @return
     */
    List<HomemakingPackage> list();

    /**
     * 分页查询套餐列表
     * @param pageNum     页码，从 1 开始；非法值按 1 处理
     * @param pageSize    每页条数；非法值按 9 处理，上限 50
     * @param serviceType 套餐类型，null 或 0 表示全部
     * @param status      状态：0上架 1下架，null 表示不过滤
     * @param packageName 套餐名模糊匹配
     * @return 分页结果
     */
    PageResult<HomemakingPackage> page(Integer pageNum, Integer pageSize,
                                       Integer serviceType, Integer status, String packageName);

    /**
     * 更新套餐
     * @param homemakingPackageDTO
     * @return
     */
    int updatePackage(HomemakingPackageDTO homemakingPackageDTO);
}
