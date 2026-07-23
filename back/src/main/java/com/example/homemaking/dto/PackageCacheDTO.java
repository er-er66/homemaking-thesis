package com.example.homemaking.dto;

import com.example.homemaking.entity.HomemakingPackage;
import lombok.Data;

import java.io.Serializable;
@Data
//Serializable序列化和反序列化接口
public class PackageCacheDTO implements Serializable {
    //套餐原始数据
    private HomemakingPackage packageInfo;

    //Redis缓存过期时间（毫秒）
    //用来控制这条Redis最多保存多长时间，防止滞后

    private Long cacheExpireAt;
}
