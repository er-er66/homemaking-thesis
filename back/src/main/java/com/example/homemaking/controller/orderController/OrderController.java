package com.example.homemaking.controller.orderController;

import com.example.homemaking.dto.HomemakingOrderImgDTO;
import com.example.homemaking.dto.OrderDTO;
import com.example.homemaking.entity.HomemakingPackage;
import com.example.homemaking.entity.Order;
import com.example.homemaking.result.Result;
import com.example.homemaking.services.OrderService;
import com.example.homemaking.util.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OssUtil ossUtil;

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
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(required = false) String userAccount,
            @RequestParam(required = false) String staffAccount
    ) {
        log.info("获取订单列表，订单编号：{}，订单状态：{}，用户账号：{}，员工账号：{}", orderNo, orderStatus, userAccount, staffAccount);
        List<Order> orders = orderService.getOrders(orderNo, orderStatus, userAccount, staffAccount);

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
        String result = orderService.dispatchOrder(orderDTO.getId(), orderDTO.getStaffAccount(),orderDTO.getDispatchAdminAccount());
        log.info("订单派单成功，订单id：{}，派单家政人员账号：{}，派单管理员账号：{}", orderDTO.getId(), orderDTO.getStaffAccount(),orderDTO.getDispatchAdminAccount());
        if (result.equals("订单派单成功")) {
            return Result.success(result);
        }
        else {
            return Result.error(result);
        }
    }
    /**
     * 拒绝订单
     *
     * @param orderDTO
     * @return
     */
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
    /**
     * 取消订单
     *
     * @param orderDTO
     * @return
     */
    @PostMapping("/order/cancel")
    public Result<String> cancelOrder(@RequestBody OrderDTO orderDTO) {
        String result = orderService.cancelOrder(orderDTO.getOrderNo(), orderDTO.getStaffAccount());
        log.info("订单取消成功，订单id：{}，取消家政人员账号：{}", orderDTO.getOrderNo(), orderDTO.getStaffAccount());
        if (result.equals("订单取消成功")) {
            return Result.success(result);
        }
        else {
            return Result.error(result);
        }
    }

    /**
     * 上传订单服务图片（打扫前/打扫后）
     */
    @PostMapping("/upload-order-img")
    public Result<String> uploadOrderImg(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("请选择要上传的图片");
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("图片大小不能超过10MB");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }

            String imgUrl = ossUtil.orderImg(file);
            return Result.success(imgUrl);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error("图片上传失败：" + e.getMessage());
        }
    }

    /**
     * 完成订单
     *
     * @param homemakingOrderImgDTO
     * @return
     */
    @PostMapping("/order/Completed")
    public Result<String> CompletedOrder(@RequestBody HomemakingOrderImgDTO homemakingOrderImgDTO) {
        String result = orderService.CompletedOrder(homemakingOrderImgDTO);
        if (result.equals("订单已完成成功")) {
            return Result.success(result);
        }
        else {
            return Result.error(result);
        }
    }

}