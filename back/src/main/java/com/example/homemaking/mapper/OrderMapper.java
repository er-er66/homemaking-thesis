package com.example.homemaking.mapper;

import com.example.homemaking.entity.Order;
import org.apache.ibatis.annotations.Mapper;

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
    int takeOrder(Long orderId, String staffAccount, Integer orderStatus);

    /**
     * 派单
     *
     * @param id
     * @param staffAccount
     * @return
     */
    int dispatchOrder(Long id, String staffAccount, Integer orderStatus, LocalDateTime updateTime);

    /**
     * 拒单
     *
     * @param id
     * @param staffAccount
     * @return
     */
    int rejectOrder(Long id, String staffAccount, Integer dispatchStatus, String dispatchAdminAccount, LocalDateTime updateTime);
}