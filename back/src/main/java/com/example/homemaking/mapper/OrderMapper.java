package com.example.homemaking.mapper;

import com.example.homemaking.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    /**
     * 添加订单
     *
     * @param order
     * @return
     */
    int addOrder(Order order);

    /**
     * 获取所有订单
     *
     * @return
     */
    List<Order> getAllOrder();

    /**
     * 根据条件查询订单
     *
     * @param orderNo 订单编号
     * @param orderStatus 订单状态
     * @return
     */
    List<Order> getOrders(String orderNo, Integer orderStatus, String userAccount, String staffAccount);

    /**
     * 根据id获取订单
     *
     * @param id
     * @return
     */
    Order getOrderById(Long id);

    /**
     * 接单
     *
     * @param orderId
     * @param staffAccount
     * @return
     */
    int takeOrder(Long orderId, String staffAccount, Integer orderStatus, Integer dispatchStatus, LocalDateTime updateTime);

    /**
     * 派单
     *
     * @param id
     * @param staffAccount
     * @return
     */
    int dispatchOrder(Long id, String staffAccount, Integer orderStatus, LocalDateTime updateTime,Integer dispatchStatus,String dispatchAdminAccount);

    /**
     * 拒单
     *
     * @param id
     * @param staffAccount
     * @return
     */
    int rejectOrder(Long id, String staffAccount, Integer dispatchStatus, String dispatchAdminAccount, LocalDateTime updateTime);

    /**
     * 取消订单
     *
     * @param orderNo 订单编号
     * @param staffAccount 员工账号
     * @return
     */
    int cancelOrder(String orderNo, String staffAccount,Integer orderStatus,LocalDateTime updateTime,Integer dispatchStatus);

    /**
     * 更新订单状态,完成订单时修改homemaking_order表中的字段
     *
     * 注意：HomemakingOrderImg 实体里并没有 orderStatus / dispatchStatus 字段，
     * 所以这里必须用 @Param 显式命名，不能传实体（否则 XML 里的 #{} 解析不到属性）。
     *
     * @param orderNo        订单编号
     * @param staffAccount   接单家政人员账号
     * @param orderStatus    订单状态：2服务完成
     * @param dispatchStatus 派单状态：0未派单
     * @param updateTime     更新时间
     * @return 影响行数
     */
    int updateOrderStatus(@Param("orderNo") String orderNo,
                          @Param("staffAccount") String staffAccount,
                          @Param("orderStatus") Integer orderStatus,
                          @Param("dispatchStatus") Integer dispatchStatus,
                          @Param("updateTime") LocalDateTime updateTime);
}