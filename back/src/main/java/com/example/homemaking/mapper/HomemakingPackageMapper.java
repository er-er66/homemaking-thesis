package com.example.homemaking.mapper;

import com.example.homemaking.entity.HomemakingPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomemakingPackageMapper {

    HomemakingPackage selectById(Long id);

    int insert(HomemakingPackage homemakingPackage);

    List<HomemakingPackage> selectList();

    int updateById(HomemakingPackage homemakingPackage);

    /**
     * 按条件统计套餐总数（不含已逻辑删除）
     *
     * @param serviceType 套餐类型，为 null 或 0 时不过滤
     * @param status      状态：0上架 1下架，为 null 时不过滤
     * @param packageName 套餐名模糊匹配，为空时不过滤
     */
    long countByCondition(@Param("serviceType") Integer serviceType,
                          @Param("status") Integer status,
                          @Param("packageName") String packageName);

    /**
     * 按条件分页查询套餐
     *
     * @param offset   起始下标，(pageNum - 1) * pageSize
     * @param pageSize 每页条数
     */
    List<HomemakingPackage> selectPage(@Param("serviceType") Integer serviceType,
                                       @Param("status") Integer status,
                                       @Param("packageName") String packageName,
                                       @Param("offset") int offset,
                                       @Param("pageSize") int pageSize);
}