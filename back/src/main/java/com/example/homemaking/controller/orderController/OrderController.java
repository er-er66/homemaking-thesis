package com.example.homemaking.controller.orderController;

import com.example.homemaking.dto.OrderDTO;
import com.example.homemaking.entity.Order;
import com.example.homemaking.result.Result;
import com.example.homemaking.services.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     *
     * @param orderDTO
     * @return
     */
    @PostMapping("/order/create")
    public Result<String> createOrder(@RequestBody OrderDTO orderDTO) {
        String result = orderService.createOrder(orderDTO);
        if (result.equals("支付密码错误")) {
            return Result.error(result);
        }
        return Result.success(result);
    }

    /**
     * 获取订单列表（支持条件查询）
     *
     * @param orderNo 订单编号（可选）
     * @param orderStatus 订单状态（可选）
     * @return
     */
    @GetMapping("/order")
    public Result<List<Order>> getOrder(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer orderStatus
    ) {
        log.info("获取订单列表，订单编号：{}，订单状态：{}", orderNo, orderStatus);
        List<Order> orders = orderService.getOrders(orderNo, orderStatus);

        return Result.success(orders);
    }
    /**
     * 更具id查询订单
     *
     * @param id
     * @return
     */

    @GetMapping("/order/{id}")
    public Result<Order> getOrderById(@PathVariable Long id) {

        Order order = orderService.getOrderById(id);
        return Result.success(order);

    }
    /**
     * 接单订单
     *

     * @return
     */
    @PostMapping("/order/take")
    public Result<String> takeOrder(@RequestBody OrderDTO orderDTO) {
        String result = orderService.takeOrder(orderDTO.getId(), orderDTO.getStaffAccount());
        log.info("订单接单成功，订单id：{}，接单家政人员账号：{}", orderDTO.getId(), orderDTO.getStaffAccount());
        if (result.equals("订单接单成功")) {
            return Result.success(result);
             }
        else {
            return Result.error(result);
        }

    }

    /**
     * 派单
     *
     * @param orderDTO
     * @return
     */
    @PostMapping("/order/dispatch")
    public Result<String> dispatchOrder(@RequestBody OrderDTO orderDTO) {
        String result = orderService.dispatchOrder(orderDTO.getId(), orderDTO.getStaffAccount());
        log.info("订单派单成功，订单id：{}，派单家政人员账号：{}", orderDTO.getId(), orderDTO.getStaffAccount());
        if (result.equals("订单派单成功")) {
            return Result.success(result);
        }
        else {
            return Result.error(result);
        }
    }
    @PostMapping("/order/reject")
    public Result<String> rejectOrder(@RequestBody OrderDTO orderDTO) {
        String result = orderService.rejectOrder(orderDTO.getId(), orderDTO.getStaffAccount());
        log.info("订单拒绝成功，订单id：{}，拒绝家政人员账号：{}", orderDTO.getId(), orderDTO.getStaffAccount());
        if (result.equals("订单拒绝成功")) {
            return Result.success(result);
        }
        else {
            return Result.error(result);
        }
    }
}