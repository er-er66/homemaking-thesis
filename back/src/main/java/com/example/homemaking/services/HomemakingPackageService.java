package com.example.homemaking.services;


import com.example.homemaking.dto.HomemakingPackageDTO;
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
     * 更新套餐
     * @param homemakingPackageDTO
     * @return
     */
    int updatePackage(HomemakingPackageDTO homemakingPackageDTO);

    /**
     * 获取家政人员已接订单列表
     * @param account
     * @param orderStatus
     * @return
     */
    List<HomemakingPackage> getOrderList(int account, int orderStatus);
}
