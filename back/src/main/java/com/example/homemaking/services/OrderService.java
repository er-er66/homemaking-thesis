package com.example.homemaking.services;

import com.example.homemaking.dto.HomemakingOrderImgDTO;
import com.example.homemaking.dto.OrderDTO;
import com.example.homemaking.dto.PageResult;
import com.example.homemaking.entity.Order;

import java.util.List;

public interface OrderService {
/**
 * 创建订单
 * @param orderDTO
 * @return
 */
    String createOrder(OrderDTO orderDTO);

    /**
     * 获取所有订单
     * @return
     */
    List<Order> getAllOrder();

    /**
     * 根据条件查询订单
     * @param orderNo 订单编号
     * @param orderStatus 订单状态
     * @return
     */
    List<Order> getOrders(String orderNo, Integer orderStatus, String userAccount, String staffAccount);

    /**
     * 分页查询订单（同时填充封面URL）
     *
     * @param orderNo     订单编号
     * @param orderStatus 订单状态
     * @param userAccount 用户账号
     * @param staffAccount 员工账号
     * @param pageNum     页码，从 1 开始
     * @param pageSize    每页条数
     * @return 分页结果
     */
    PageResult<Order> getOrdersPage(String orderNo, Integer orderStatus, String userAccount, String staffAccount,
                                    Integer pageNum, Integer pageSize);

    /**
 * 根据id获取订单
 * @param id
 * @return
 */
    Order getOrderById(Long id);

    /**
 * 接单
 * @param orderId
 * @param staffAccount
 * @return
 */
    String takeOrder(Long orderId, String staffAccount);

    /**
 * 派单
 * @param id
 * @param staffAccount
 * @return
 */
    String dispatchOrder(Long id, String staffAccount,String dispatchAdminAccount);

    /**
 * 拒单
 * @param id
 * @param staffAccount
 * @return
 */
    String rejectOrder(Long id, String staffAccount);

    /**
 * 取消订单
 * @param orderNo 订单编号
 * @param staffAccount
 * @return
 */
    String cancelOrder(String orderNo, String staffAccount);

    String CompletedOrder(HomemakingOrderImgDTO homemakingOrderImgDTO);
}