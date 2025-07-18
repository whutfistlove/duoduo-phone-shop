package com.duoduo.phoneshop.mapper;

import com.duoduo.phoneshop.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单项数据访问接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Mapper
public interface OrderItemMapper {

    /**
     * 插入订单项
     * @param orderItem 订单项信息
     * @return 影响行数
     */
    int insert(OrderItem orderItem);

    /**
     * 根据订单ID查询订单项列表
     * @param orderId 订单ID
     * @return 订单项列表
     */
    List<OrderItem> selectByOrder(@Param("orderId") Long orderId);

    /**
     * 删除订单的所有订单项
     * @param orderId 订单ID
     * @return 影响行数
     */
    int deleteByOrder(@Param("orderId") Long orderId);
}