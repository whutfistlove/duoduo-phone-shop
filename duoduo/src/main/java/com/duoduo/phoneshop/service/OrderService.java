package com.duoduo.phoneshop.service;

import com.duoduo.phoneshop.entity.Order;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单服务接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
public interface OrderService {

    /**
     * 创建订单
     * @param userId 用户ID
     * @param addressId 收货地址ID
     * @param remark 备注
     * @return 订单信息
     */
    Order createOrder(Long userId, Long addressId, String remark);

    /**
     * 支付订单
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 支付结果
     */
    boolean payOrder(Long orderId, Long userId);

    /**
     * 取消订单
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 取消结果
     */
    boolean cancelOrder(Long orderId, Long userId);

    /**
     * 发货
     * @param orderId 订单ID
     * @return 发货结果
     */
    boolean deliverOrder(Long orderId);

    /**
     * 确认收货
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 确认结果
     */
    boolean confirmReceive(Long orderId, Long userId);

    /**
     * 根据ID查询订单
     * @param orderId 订单ID
     * @return 订单信息
     */
    Order getOrderById(Long orderId);

    /**
     * 查询用户订单
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Order> getUserOrders(Long userId);

    /**
     * 查询所有订单(管理员)
     * @return 订单列表
     */
    List<Order> getAllOrders();

    /**
     * 根据状态查询订单
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> getOrdersByStatus(Integer status);

    /**
     * 删除订单
     * @param orderId 订单ID
     * @return 删除结果
     */
    boolean deleteOrder(Long orderId);

    /**
     * 获取总销售额
     * @return 总销售额
     */
    BigDecimal getTotalSales();

    /**
     * 获取今日订单数
     * @return 订单数
     */
    Integer getTodayOrderCount();

    /**
     * 获取本月订单数
     * @return 订单数
     */
    Integer getMonthOrderCount();
}