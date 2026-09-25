package com.example.homemaking.services.impl;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import com.example.homemaking.dto.HomemakingOrderImgDTO;
import com.example.homemaking.dto.OrderDTO;
import com.example.homemaking.dto.OrderPackageItemDTO;
import com.example.homemaking.dto.PageResult;
import com.example.homemaking.entity.HomemakingOrderImg;
import com.example.homemaking.entity.HomemakingOrderPackage;
import com.example.homemaking.entity.HomemakingPackage;
import com.example.homemaking.entity.Order;
import com.example.homemaking.entity.SysUser;
import com.example.homemaking.entity.UserImg;
import com.example.homemaking.mapper.HomemakingOrderImgMapper;
import com.example.homemaking.mapper.HomemakingOrderPackageMapper;
import com.example.homemaking.mapper.HomemakingPackageMapper;
import com.example.homemaking.mapper.OrderMapper;
import com.example.homemaking.mapper.UserImgMapper;
import com.example.homemaking.mapper.UserMapper;
import com.example.homemaking.services.OrderService;
import com.example.homemaking.util.OrderNoUtil;
import com.example.homemaking.util.PageUtil;
import com.example.homemaking.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private HomemakingOrderPackageMapper orderPackageMapper;

    @Autowired
    private HomemakingPackageMapper homemakingPackageMapper;

    /**
     * 创建订单
     *
     * @param orderDTO
     * @return
     */
    @Transactional
    @Override
    public String createOrder(OrderDTO orderDTO) {
        //获取到当前支付密码是否正确（库中存的是 BCrypt hash，走 matches 校验）
        String payPassword = orderDTO.getPayPassword();
        if (payPassword == null || payPassword.isEmpty()) {
            return "支付密码不能为空";
        }
        String dbPayPassword = userMapper.getAccountPassword(orderDTO.getUserAccount());//根据账号获取数据库中的支付密码
        if (!PasswordUtil.matches(payPassword, dbPayPassword)) {
            return "支付密码错误";
        }
        Order order = new Order();
        BeanUtils.copyProperties(orderDTO, order);
        //pay_password 是敏感字段，不透传到订单表（订单表原有的 pay_password 列不再写入）
        order.setPayPassword(null);
        if (order.getOrderStatus() == null) {//用户的发布订单（默认待接单）
            order.setOrderStatus(0);
        }
        order.setOrderNo(OrderNoUtil.generateOrderNo());//生成订单号(时间戳+雪花短码)
        order.setCreateTime(LocalDateTime.now());//设置下单时间
        order.setUpdateTime(LocalDateTime.now());
        order.setIsDeleted(0);//逻辑删除：0正常 1已删除
        order.setStaffAccount("null");

        // 下单时选的套餐（可能为空：老前端只传 service_item 文本名，不传套餐）
        List<HomemakingOrderPackage> orderPackages = buildOrderPackages(orderDTO, order.getOrderNo());

        if (!orderPackages.isEmpty()) {
            // 套餐算出来的金额优先于前端传的金额（防止前端算错或漏传 unit_price 快照）
            BigDecimal packageAmount = orderPackages.stream()
                    .map(ip -> ip.getUnitPrice().multiply(BigDecimal.valueOf(ip.getPackageNum())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setOrderAmount(packageAmount);
            if (order.getServiceItem() == null || order.getServiceItem().trim().isEmpty()) {
                // service_item 是 NOT NULL，套餐场景下用套餐名兜底
                order.setServiceItem(buildServiceItemText(orderPackages));
            }
        }

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
        if (count2 <= 0) {
            return "订单创建失败";
        }

        // 订单必须先落库拿到 order_no，才能写中间表 —— 否则撞 fk_order_pkg_order_no 外键
        if (!orderPackages.isEmpty()) {
            int count3 = orderPackageMapper.batchInsert(orderPackages);
            if (count3 <= 0) {
                // 抛异常而不是 return：@Transactional 默认只对 RuntimeException 回滚，
                // 直接 return 会把已经插入的订单留下，中间表却空，数据不一致。
                throw new IllegalStateException("订单套餐关联写入失败，orderNo=" + order.getOrderNo());
            }
        }
        return "订单创建成功";
    }

    /**
     * 把 {@code OrderDTO} 上的套餐入参整理成中间表实体。
     * <p>支持两种写法：</p>
     * <ul>
     *   <li>{@code packages: [{packageId, packageNum}]} —— 推荐，可指定数量</li>
     *   <li>{@code packageIds: [1,2]} —— 简写，数量默认 1；仅当 packages 为空时生效</li>
     * </ul>
     * <p>同一套餐出现多次会被合并数量：{@code uk_order_package(order_no, package_id)}
     * 唯一键会拦下重复插入。</p>
     * <p>套餐不存在或已被逻辑删除时直接抛异常（触发事务回滚），不静默跳过。</p>
     */
    private List<HomemakingOrderPackage> buildOrderPackages(OrderDTO orderDTO, String orderNo) {
        // 归一化：packageId -> packageNum
        Map<Long, Integer> merged = new LinkedHashMap<>();
        if (orderDTO.getPackages() != null) {
            for (OrderPackageItemDTO item : orderDTO.getPackages()) {
                if (item == null || item.getPackageId() == null) {
                    continue;
                }
                int num = item.getPackageNum() == null || item.getPackageNum() <= 0 ? 1 : item.getPackageNum();
                merged.merge(item.getPackageId(), num, Integer::sum);
            }
        }
        if (merged.isEmpty() && orderDTO.getPackageIds() != null) {
            for (Long id : orderDTO.getPackageIds()) {
                if (id != null) {
                    merged.merge(id, 1, Integer::sum);
                }
            }
        }
        if (merged.isEmpty()) {
            return List.of();
        }

        // 一次 IN 查询拿价格快照，避免 N 次查询
        List<HomemakingPackage> packages =
                homemakingPackageMapper.selectByIds(new ArrayList<>(merged.keySet()));
        Map<Long, HomemakingPackage> packageMap = packages.stream()
                .collect(Collectors.toMap(HomemakingPackage::getId, p -> p));

        List<Long> missing = merged.keySet().stream()
                .filter(id -> !packageMap.containsKey(id))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("套餐不存在或已下架：" + missing);
        }

        LocalDateTime now = LocalDateTime.now();
        List<HomemakingOrderPackage> result = new ArrayList<>(merged.size());
        for (Map.Entry<Long, Integer> e : merged.entrySet()) {
            HomemakingPackage pkg = packageMap.get(e.getKey());
            HomemakingOrderPackage op = new HomemakingOrderPackage();
            op.setOrderNo(orderNo);
            op.setPackageId(e.getKey());
            op.setPackageNum(e.getValue());
            op.setUnitPrice(pkg.getPackagePrice());// 下单价格快照，防止后续调价影响历史订单
            op.setCreateTime(now);
            result.add(op);
        }
        return result;
    }

    /**
     * 套餐名为空时给 service_item 兜底，例如「日常钟点保洁 x2,整理收纳服务 x1」
     */
    private String buildServiceItemText(List<HomemakingOrderPackage> orderPackages) {
        List<Long> ids = orderPackages.stream()
                .map(HomemakingOrderPackage::getPackageId)
                .collect(Collectors.toList());
        Map<Long, String> nameMap = homemakingPackageMapper.selectByIds(ids).stream()
                .collect(Collectors.toMap(HomemakingPackage::getId, HomemakingPackage::getPackageName));
        return orderPackages.stream()
                .map(op -> {
                    String name = nameMap.getOrDefault(op.getPackageId(), "套餐" + op.getPackageId());
                    return op.getPackageNum() != null && op.getPackageNum() > 1
                            ? name + " x" + op.getPackageNum()
                            : name;
                })
                .collect(Collectors.joining(","));
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
        fillCoverUrl(orders);
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
        fillCoverUrl(orders);
        return orders;
    }

    /**
     * 分页查询订单（只对当前页 10 条填充封面，比全量查更省）
     *
     * @param pageNum  页码，从 1 开始
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<Order> getOrdersPage(String orderNo, Integer orderStatus, String userAccount, String staffAccount,
                                           Integer pageNum, Integer pageSize) {
        int num = PageUtil.normalizePageNum(pageNum);
        int size = PageUtil.normalizePageSize(pageSize);
        long total = orderMapper.countOrders(orderNo, orderStatus, userAccount, staffAccount);
        List<Order> records = total == 0 ? List.of()
                : orderMapper.getOrdersPage(orderNo, orderStatus, userAccount, staffAccount,
                PageUtil.offset(num, size), size);
        fillCoverUrl(records);
        return PageResult.of(total, num, size, records);
    }

    /**
     * 填充订单封面URL
     *
     * @param orders 订单列表，允许为空
     */
    private void fillCoverUrl(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        for (Order order : orders) {
            UserImg userImg = userImgMapper.selectByOrderNo(order.getOrderNo());
            if (userImg != null) {
                order.setCoverUrl(userImg.getUserOrderUrl());
            }
        }
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