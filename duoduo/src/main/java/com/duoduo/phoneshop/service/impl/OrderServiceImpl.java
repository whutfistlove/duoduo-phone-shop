package com.duoduo.phoneshop.service.impl;

import com.duoduo.phoneshop.entity.*;
import com.duoduo.phoneshop.mapper.*;
import com.duoduo.phoneshop.service.OrderService;
import com.duoduo.phoneshop.service.ProductService;
import com.duoduo.phoneshop.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单服务实现类
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public Order createOrder(Long userId, Long addressId, String remark) {
        try {
            // 获取选中的购物车商品
            List<Cart> cartItems = cartMapper.selectSelectedByUser(userId);
            if (cartItems.isEmpty()) {
                log.warn("购物车中没有选中的商品");
                return null;
            }

            // 计算订单总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            for (Cart cart : cartItems) {
                Product product = cart.getProduct();
                if (product == null || product.getStatus() != 1) {
                    log.warn("商品不存在或已下架，ID: {}", cart.getProductId());
                    return null;
                }

                // 检查库存
                if (product.getStock() < cart.getQuantity()) {
                    log.warn("商品库存不足，ID: {}", product.getId());
                    return null;
                }

                // 创建订单项
                OrderItem item = new OrderItem();
                item.setProductId(product.getId());
                item.setProductName(product.getN());
                item.setProductImage(product.getImage());
                item.setPrice(product.getPrice());
                item.setQuantity(cart.getQuantity());
                item.setTotalAmount(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));

                orderItems.add(item);
                totalAmount = totalAmount.add(item.getTotalAmount());
            }

            // 创建订单
            Order order = new Order();
            order.setOrderNo(OrderNoGenerator.generate());
            order.setUserId(userId);
            order.setAddressId(addressId);
            order.setTotalAmount(totalAmount);
            order.setPayAmount(totalAmount);
            order.setStatus(0); // 待支付
            order.setRemark(remark);

            // 保存订单
            orderMapper.insert(order);

            // 保存订单项
            for (OrderItem item : orderItems) {
                item.setOrderId(order.getId());
                orderItemMapper.insert(item);
            }

            // 清空已选中的购物车商品
            for (Cart cart : cartItems) {
                cartMapper.delete(cart.getId());
            }

            order.setOrderItems(orderItems);
            return order;

        } catch (Exception e) {
            log.error("创建订单失败", e);
            throw new RuntimeException("创建订单失败", e);
        }
    }

    @Override
    @Transactional
    public boolean payOrder(Long orderId, Long userId) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null || !order.getUserId().equals(userId)) {
                log.warn("订单不存在或不属于当前用户");
                return false;
            }

            if (order.getStatus() != 0) {
                log.warn("订单状态不正确，无法支付");
                return false;
            }

            // 扣减用户余额
            if (!userService.deductBalance(userId, order.getPayAmount())) {
                log.warn("用户余额不足");
                return false;
            }

            // 扣减商品库存
            List<OrderItem> orderItems = orderItemMapper.selectByOrder(orderId);
            for (OrderItem item : orderItems) {
                if (!productService.updateStock(item.getProductId(), -item.getQuantity())) {
                    log.error("扣减库存失败，商品ID: {}", item.getProductId());
                    throw new RuntimeException("库存不足");
                }
            }

            // 更新订单状态
            order.setStatus(1); // 已支付
            order.setPayTime(new Date());

            return orderMapper.update(order) > 0;

        } catch (Exception e) {
            log.error("支付订单失败", e);
            throw new RuntimeException("支付失败", e);
        }
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId, Long userId) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null || !order.getUserId().equals(userId)) {
                return false;
            }

            if (order.getStatus() != 0) {
                log.warn("只能取消待支付的订单");
                return false;
            }

            order.setStatus(4); // 已取消
            return orderMapper.update(order) > 0;

        } catch (Exception e) {
            log.error("取消订单失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deliverOrder(Long orderId) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null || order.getStatus() != 1) {
                return false;
            }

            order.setStatus(2); // 已发货
            order.setDeliveryTime(new Date());
            return orderMapper.update(order) > 0;

        } catch (Exception e) {
            log.error("发货失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean confirmReceive(Long orderId, Long userId) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null || !order.getUserId().equals(userId)) {
                return false;
            }

            if (order.getStatus() != 2) {
                log.warn("只能确认已发货的订单");
                return false;
            }

            order.setStatus(3); // 已完成
            order.setReceiveTime(new Date());
            return orderMapper.update(order) > 0;

        } catch (Exception e) {
            log.error("确认收货失败", e);
            return false;
        }
    }

    @Override
    public Order getOrderById(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order != null) {
            List<OrderItem> items = orderItemMapper.selectByOrder(orderId);
            order.setOrderItems(items);
        }
        return order;
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        // 获取用户的所有订单
        List<Order> orders = orderMapper.selectByUser(userId);

        // 为每个订单加载订单项
        for (Order order : orders) {
            List<OrderItem> items = orderItemMapper.selectByOrder(order.getId());
            order.setOrderItems(items);
        }

        return orders;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderMapper.selectAll();
    }

    @Override
    public List<Order> getOrdersByStatus(Integer status) {
        return orderMapper.selectByStatus(status);
    }

    @Override
    @Transactional
    public boolean deleteOrder(Long orderId) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null || order.getStatus() == 1 || order.getStatus() == 2) {
                log.warn("不能删除已支付或已发货的订单");
                return false;
            }

            // 删除订单项
            orderItemMapper.deleteByOrder(orderId);
            // 删除订单
            return orderMapper.delete(orderId) > 0;

        } catch (Exception e) {
            log.error("删除订单失败", e);
            return false;
        }
    }

    @Override
    public BigDecimal getTotalSales() {
        return orderMapper.getTotalSales();
    }

    @Override
    public Integer getTodayOrderCount() {
        return orderMapper.getTodayOrderCount();
    }

    @Override
    public Integer getMonthOrderCount() {
        return orderMapper.getMonthOrderCount();
    }
}