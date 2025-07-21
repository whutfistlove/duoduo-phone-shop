package com.duoduo.phoneshop.mapper;

import com.duoduo.phoneshop.entity.RechargeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 充值订单数据访问接口
 *
 * @author DuoDuo
 * @date 2025/01/23
 */
@Mapper
public interface RechargeOrderMapper {

    /**
     * 插入充值订单
     * @param rechargeOrder 充值订单信息
     * @return 影响行数
     */
    int insert(RechargeOrder rechargeOrder);

    /**
     * 根据订单号查询充值订单
     * @param orderNo 订单号
     * @return 充值订单信息
     */
    RechargeOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 更新充值订单状态
     * @param orderNo 订单号
     * @param status 状态
     * @param tradeNo 支付宝交易号
     * @return 影响行数
     */
    int updateStatus(@Param("orderNo") String orderNo,
                     @Param("status") Integer status,
                     @Param("tradeNo") String tradeNo);
}