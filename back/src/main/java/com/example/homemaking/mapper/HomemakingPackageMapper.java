package com.example.homemaking.mapper;

import com.example.homemaking.entity.HomemakingPackage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HomemakingPackageMapper {

    HomemakingPackage selectById(Long id);

    int insert(HomemakingPackage homemakingPackage);

    List<HomemakingPackage> selectList();

    int updateById(HomemakingPackage homemakingPackage);
}