package com.example.homemaking.services.impl;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import com.example.homemaking.dto.HomemakingOrderImgDTO;
import com.example.homemaking.dto.OrderDTO;
import com.example.homemaking.entity.HomemakingOrderImg;
import com.example.homemaking.entity.Order;
import com.example.homemaking.entity.SysUser;
import com.example.homemaking.entity.UserImg;
import com.example.homemaking.mapper.HomemakingOrderImgMapper;
import com.example.homemaking.mapper.OrderMapper;
import com.example.homemaking.mapper.UserImgMapper;
import com.example.homemaking.mapper.UserMapper;
import com.example.homemaking.services.OrderService;
import com.example.homemaking.util.OrderNoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserImgMapper userImgMapper;

    @Autowired
    private HomemakingOrderImgMapper homemakingOrderImgMapper;

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
        if (order.getOrderStatus() == null) {//用户的发布订单（默认待接单）
            order.setOrderStatus(0);
        }
        order.setOrderNo(OrderNoUtil.generateOrderNo());//生成订单号(时间戳+雪花短码)
        order.setCreateTime(LocalDateTime.now());//设置下单时间
        order.setUpdateTime(LocalDateTime.now());
        order.setIsDeleted(0);//逻辑删除：0正常 1已删除
        order.setStaffAccount("null");

        // 保存订单封面图片
        if (orderDTO.getCoverUrl() != null && !orderDTO.getCoverUrl().isEmpty()) {
            UserImg userImg = new UserImg();
            userImg.setOrderNo(order.getOrderNo());
            userImg.setUserAccount(order.getUserAccount());
            userImg.setStaffAccount(order.getStaffAccount());
            userImg.setUserOrderUrl(orderDTO.getCoverUrl());
            int count1 = userImgMapper.addUserImg(userImg);
            if (count1 <= 0) {
                return "用户订单封面上传失败";
            }
        }

        int count2 = orderMapper.addOrder(order);
        if (count2 > 0) {
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
        List<Order> orders = orderMapper.getAllOrder();
        // 填充封面URL
        for (Order order : orders) {
            UserImg userImg = userImgMapper.selectByOrderNo(order.getOrderNo());
            if (userImg != null) {
                order.setCoverUrl(userImg.getUserOrderUrl());
            }
        }
        return orders;
    }

    /**
     * 根据条件查询订单
     *
     * @param orderNo     订单编号
     * @param orderStatus 订单状态
     * @return
     */
    @Override
    public List<Order> getOrders(String orderNo, Integer orderStatus, String userAccount, String staffAccount) {
        List<Order> orders = orderMapper.getOrders(orderNo, orderStatus, userAccount, staffAccount);
        // 填充封面URL
        for (Order order : orders) {
            UserImg userImg = userImgMapper.selectByOrderNo(order.getOrderNo());
            if (userImg != null) {
                order.setCoverUrl(userImg.getUserOrderUrl());
            }
        }
        return orders;
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
        if (order != null) {
            UserImg userImg = userImgMapper.selectByOrderNo(order.getOrderNo());
            if (userImg != null) {
                order.setCoverUrl(userImg.getUserOrderUrl());
            }
        }
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
        Integer dispatchStatus = 0;//0未派单 1已派单 2已拒单
        LocalDateTime updateTime = LocalDateTime.now();//设置接单时间
        int count = orderMapper.takeOrder(orderId, staffAccount, orderStatus, dispatchStatus, updateTime);
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
    public String dispatchOrder(Long id, String staffAccount, String dispatchAdminAccount) {
        Integer orderStatus = 0;//0待接单 1已接单 2已完成 3已取消
        Integer dispatchStatus = 1;//0未派单 1已派单 2已拒单
        LocalDateTime updateTime = LocalDateTime.now();//设置派单时间
        int count = orderMapper.dispatchOrder(id, staffAccount, orderStatus, updateTime, dispatchStatus, dispatchAdminAccount);
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

    /**
     * 取消订单
     *
     * @param orderNo      订单编号
     * @param staffAccount
     * @return
     */
    @Override
    public String cancelOrder(String orderNo, String staffAccount) {
        int orderStatus = 4;//3家政人员已取消
        int dispatchStatus = 0;//0未派单
        LocalDateTime updateTime = LocalDateTime.now();//设置取消时间
        int count = orderMapper.cancelOrder(orderNo, staffAccount, orderStatus, updateTime, dispatchStatus);
        if (count > 0) {
            return "订单取消成功";
        }
        return "订单取消失败";
    }

    @Transactional //开启事务
    @Override
    public String CompletedOrder(HomemakingOrderImgDTO homemakingOrderImgDTO) {
        String orderNo = homemakingOrderImgDTO.getOrderNo();
        String staffAccount = homemakingOrderImgDTO.getStaffAccount();

        HomemakingOrderImg homemakingOrderImg = new HomemakingOrderImg();
        BeanUtils.copyProperties(homemakingOrderImgDTO, homemakingOrderImg);
        LocalDateTime now = LocalDateTime.now();
        homemakingOrderImg.setFinishTime(now);//设置服务已完成时间
        homemakingOrderImg.setCreateTime(now);//设置上传时间
        homemakingOrderImg.setUpdateTime(now);//设置更新时间
        homemakingOrderImg.setIsDeleted(0);//0正常 1已删除

        int orderStatus = 2;//2已完成 3已取消
        int dispatchStatus = 0;//0未派单

        //更新homemaking_order表中的字段
        int count = orderMapper.updateOrderStatus(orderNo, staffAccount, orderStatus, dispatchStatus, now);
        if (count <= 0) {
            return "订单已完成失败";
        }

        //清理前图片 imgType=0
        if (!saveOrderImgs(homemakingOrderImgDTO.getBeforeCleanImgs(), homemakingOrderImg, 0)) {
            return "订单已完成失败";
        }
        //清理后图片 imgType=1
        if (!saveOrderImgs(homemakingOrderImgDTO.getAfterCleanImgs(), homemakingOrderImg, 1)) {
            return "订单已完成失败";
        }

        return "订单已完成成功";
    }

    /**
     * 保存订单服务图片（单图一条记录）
     *
     * @param imgUrls 图片地址数组，允许为空
     * @param img     复用的图片实体
     * @param imgType 0打扫前 1打扫后
     * @return 是否全部保存成功
     */
    private boolean saveOrderImgs(String[] imgUrls, HomemakingOrderImg img, int imgType) {
        if (imgUrls == null || imgUrls.length == 0) {
            return true;
        }
        img.setImgType(imgType);
        for (String imgUrl : imgUrls) {
            if (imgUrl == null || imgUrl.trim().isEmpty()) {
                continue;
            }
            img.setImgUrl(imgUrl);
            if (!Boolean.TRUE.equals(homemakingOrderImgMapper.CompletedOrderImg(img))) {
                return false;
            }
        }
        return true;
    }
}