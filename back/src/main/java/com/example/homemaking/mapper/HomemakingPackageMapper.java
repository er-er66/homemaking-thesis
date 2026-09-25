package com.example.homemaking.mapper;

import com.example.homemaking.entity.HomemakingPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomemakingPackageMapper {

    HomemakingPackage selectById(Long id);

    /**
     * 按ID集合批量查询未删除的套餐（下单时取价格快照用）
     *
     * @param ids 套餐ID集合，调用方保证非空
     */
    List<HomemakingPackage> selectByIds(@Param("ids") List<Long> ids);

    int insert(HomemakingPackage homemakingPackage);

    List<HomemakingPackage> selectList();

    int updateById(HomemakingPackage homemakingPackage);

    /**
     * 切换套餐上下架状态：0(上架) ↔ 1(下架)
     * <p>用一条 SQL 原子翻转，避免「先查后写」在并发下互相覆盖。</p>
     *
     * @param id 套餐ID
     * @return 受影响的行数，0 表示套餐不存在或已逻辑删除
     */
    int toggleStatus(@Param("id") Long id);

    /**
     * 显式设置套餐上下架状态（幂等）
     *
     * @param id     套餐ID
     * @param status 0上架 1下架
     * @return 受影响的行数，0 表示套餐不存在或已逻辑删除
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

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
