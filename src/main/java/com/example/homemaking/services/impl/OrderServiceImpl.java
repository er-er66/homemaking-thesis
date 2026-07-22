package com.example.homemaking.services.impl;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import com.example.homemaking.dto.OrderDTO;
import com.example.homemaking.entity.Order;
import com.example.homemaking.entity.SysUser;
import com.example.homemaking.mapper.OrderMapper;
import com.example.homemaking.mapper.UserMapper;
import com.example.homemaking.services.OrderService;
import com.example.homemaking.util.OrderNoUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 创建订单
     *
     * @param orderDTO
     * @return
     */
    @Override
    public String createOrder(OrderDTO orderDTO) {
        //获取到当前支付密码是否正确
        String payPassword = orderDTO.getPayPassword();
        String dbPayPassword = userMapper.getAccountPassword(orderDTO.getUserAccount());//根据账号获取数据库中的支付密码
        if (!dbPayPassword.equals(payPassword)) {
            return "支付密码错误";
        }
        Order order = new Order();

        BeanUtils.copyProperties(orderDTO, order);
        order.setOrderNo(OrderNoUtil.generateOrderNo());//生成订单号(时间戳+雪花短码)
        order.setCreateTime(LocalDateTime.now());//设置下单时间
        order.setUpdateTime(LocalDateTime.now());
        order.setIsDeleted(0);//逻辑删除：0正常 1已删除
        order.setStaffAccount("null");
        int count = orderMapper.addOrder(order);
        if (count > 0) {
            return "订单创建成功";
        }
        return "订单创建失败";
    }

    /**
     * 获取所有订单
     *
     * @return
     */
    @Override
    public List<Order> getAllOrder() {
        return orderMapper.getAllOrder();
    }

    /**
     * 根据条件查询订单
     *
     * @param orderNo     订单编号
     * @param orderStatus 订单状态
     * @return
     */
    @Override
    public List<Order> getOrders(String orderNo, Integer orderStatus) {
        return orderMapper.getOrders(orderNo, orderStatus);
    }

    /**
     * 根据id查询订单
     *
     * @param id
     * @return
     */
    @Override
    public Order getOrderById(Long id) {
        Order order = orderMapper.getOrderById(id);
        return order;
    }

    /**
     * 接单
     *
     * @return
     */
    @Override
    public String takeOrder(Long orderId, String staffAccount) {
        Integer orderStatus = 1;//1已接单 2已完成 3已取消
        int count = orderMapper.takeOrder(orderId, staffAccount, orderStatus);
        if (count > 0) {
            return "订单接单成功";
        }
        return "订单接单失败";
    }

    /**
     * 派单
     *
     * @return
     */
    @Override
    public String dispatchOrder(Long id, String staffAccount) {
        Integer orderStatus = 0;//0待接单 1已接单 2已完成 3已取消
        LocalDateTime updateTime = LocalDateTime.now();//设置派单时间
        int count = orderMapper.dispatchOrder(id, staffAccount, orderStatus, updateTime);
        if (count > 0) {
            return "订单派单成功";
        }
        return "订单派单失败";
    }

    /**
     * 拒单
     *
     * @return
     */
    @Override
    public String rejectOrder(Long id, String staffAccount) {
        staffAccount = "null";
        LocalDateTime updateTime = LocalDateTime.now();
        Integer dispatchStatus = 3;//3已取消
        String dispatchAdminAccount = "null";

        int count = orderMapper.rejectOrder(id, staffAccount, dispatchStatus, dispatchAdminAccount, updateTime);
        if (count > 0) {
            return "订单拒单成功";
        }
        return "订单拒单失败";
    }
}