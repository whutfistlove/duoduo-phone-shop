package com.duoduo.phoneshop.service;

import com.duoduo.phoneshop.entity.Cart;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车服务接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
public interface CartService {

    /**
     * 添加商品到购物车
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @return 添加结果
     */
    boolean addToCart(Long userId, Long productId, Integer quantity);

    /**
     * 获取用户购物车列表
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<Cart> getCartList(Long userId);

    /**
     * 更新购物车商品数量
     * @param cartId 购物车ID
     * @param quantity 新数量
     * @return 更新结果
     */
    boolean updateQuantity(Long cartId, Integer quantity);

    /**
     * 删除购物车商品
     * @param cartId 购物车ID
     * @return 删除结果
     */
    boolean deleteCartItem(Long cartId);

    /**
     * 清空用户购物车
     * @param userId 用户ID
     * @return 清空结果
     */
    boolean clearCart(Long userId);

    /**
     * 选中/取消选中购物车商品
     * @param cartId 购物车ID
     * @param selected 是否选中
     * @return 更新结果
     */
    boolean updateSelected(Long cartId, Integer selected);

    /**
     * 全选/取消全选
     * @param userId 用户ID
     * @param selected 是否选中
     * @return 更新结果
     */
    boolean selectAll(Long userId, Integer selected);

    /**
     * 获取购物车商品数量
     * @param userId 用户ID
     * @return 商品数量
     */
    Integer getCartCount(Long userId);

    /**
     * 计算购物车选中商品总金额
     * @param userId 用户ID
     * @return 总金额
     */
    BigDecimal calculateTotalAmount(Long userId);

    /**
     * 获取选中的购物车商品
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<Cart> getSelectedCartItems(Long userId);
}