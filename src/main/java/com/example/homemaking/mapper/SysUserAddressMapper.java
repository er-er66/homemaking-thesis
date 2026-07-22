package com.example.homemaking.mapper;

import com.example.homemaking.dto.AddressSaveDTO;
import com.example.homemaking.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserAddressMapper {
    /**
     * 保存地址
     * @param userAddress 地址保存DTO
     * @return
     */
    int updateAddress(UserAddress userAddress);
    /**
     * 删除地址
     * @param id 地址id
     * @return
     */
    int deleteAddress(Long id);
}
