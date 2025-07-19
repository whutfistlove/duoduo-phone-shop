package com.duoduo.phoneshop.mapper;

import com.duoduo.phoneshop.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车数据访问接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Mapper
public interface CartMapper {

    /**
     * 插入购物车项
     * @param cart 购物车信息
     * @return 影响行数
     */
    int insert(Cart cart);

    /**
     * 根据ID查询购物车项
     * @param id 购物车ID
     * @return 购物车信息
     */
    Cart selectById(@Param("id") Long id);

    /**
     * 根据用户ID查询购物车列表
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<Cart> selectByUser(@Param("userId") Long userId);

    /**
     * 根据用户ID和商品ID查询购物车项
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 购物车信息
     */
    Cart selectByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 查询用户选中的购物车项
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<Cart> selectSelectedByUser(@Param("userId") Long userId);

    /**
     * 更新购物车项
     * @param cart 购物车信息
     * @return 影响行数
     */
    int update(Cart cart);

    /**
     * 删除购物车项
     * @param id 购物车ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);

    /**
     * 删除用户所有购物车项
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUser(@Param("userId") Long userId);

    /**
     * 统计用户购物车商品数量
     * @param userId 用户ID
     * @return 商品数量
     */
    int countByUser(@Param("userId") Long userId);

    /**
     * 更新用户所有购物车项的选中状态
     * @param userId 用户ID
     * @param selected 选中状态
     * @return 影响行数
     */
    int updateSelectAll(@Param("userId") Long userId, @Param("selected") Integer selected);
}