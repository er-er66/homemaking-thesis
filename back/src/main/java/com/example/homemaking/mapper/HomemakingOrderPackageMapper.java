package com.example.homemaking.mapper;

import com.example.homemaking.entity.HomemakingOrderPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomemakingOrderPackageMapper {

    /**
     * 批量写入订单-套餐关联
     * <p>必须在 homemaking_order 落库拿到 order_no 之后调用，否则外键 fk_order_pkg_order_no 报错</p>
     * <p>调用方需保证同一批 packageId 不重复，否则撞 uk_order_package</p>
     * <p>orderNo / createTime 由调用方填在每条 item 上，不额外传参。</p>
     *
     * @param items 关联明细（orderNo + packageId + packageNum + unitPrice + createTime）
     * @return 受影响行数
     */
    int batchInsert(@Param("items") List<HomemakingOrderPackage> items);

    /**
     * 查询某订单包含的套餐明细（join 套餐表带出名称、单位、封面）
     *
     * @param orderNo 订单编号
     */
    List<HomemakingOrderPackage> selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 批量查询多个订单的套餐明细（订单列表用，一次查询代替 N+1）
     *
     * @param orderNos 订单编号集合
     */
    List<HomemakingOrderPackage> selectByOrderNos(@Param("orderNos") List<String> orderNos);

    /**
     * 删除某订单的全部套餐关联
     *
     * @param orderNo 订单编号
     */
    int deleteByOrderNo(@Param("orderNo") String orderNo);
}
