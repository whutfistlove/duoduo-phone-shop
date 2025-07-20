package com.duoduo.phoneshop.mapper;

import com.duoduo.phoneshop.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单数据访问接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     * @param order 订单信息
     * @return 影响行数
     */
    int insert(Order order);

    /**
     * 根据ID查询订单
     * @param id 订单ID
     * @return 订单信息
     */
    Order selectById(@Param("id") Long id);

    /**
     * 根据用户ID查询订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Order> selectByUser(@Param("userId") Long userId);

    /**
     * 查询所有订单
     * @return 订单列表
     */
    List<Order> selectAll();

    /**
     * 根据状态查询订单
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> selectByStatus(@Param("status") Integer status);

    /**
     * 更新订单
     * @param order 订单信息
     * @return 影响行数
     */
    int update(Order order);

    /**
     * 删除订单
     * @param id 订单ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);

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

    /**
     * 根据订单号查询订单
     * @param orderNo 订单号
     * @return 订单信息
     */
    Order selectByOrderNo(@Param("orderNo") String orderNo);
}