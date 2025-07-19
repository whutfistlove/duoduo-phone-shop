package com.duoduo.phoneshop.service.impl;

import com.duoduo.phoneshop.entity.Cart;
import com.duoduo.phoneshop.entity.Product;
import com.duoduo.phoneshop.mapper.CartMapper;
import com.duoduo.phoneshop.mapper.ProductMapper;
import com.duoduo.phoneshop.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车服务实现类
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public boolean addToCart(Long userId, Long productId, Integer quantity) {
        try {
            // 检查商品是否存在
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStatus() != 1) {
                log.warn("商品不存在或已下架，ID: {}", productId);
                return false;
            }

            // 检查库存
            if (product.getStock() < quantity) {
                log.warn("商品库存不足，ID: {}", productId);
                return false;
            }

            // 检查购物车中是否已存在该商品
            Cart existCart = cartMapper.selectByUserAndProduct(userId, productId);
            if (existCart != null) {
                // 更新数量
                existCart.setQuantity(existCart.getQuantity() + quantity);
                return cartMapper.update(existCart) > 0;
            } else {
                // 新增购物车项
                Cart cart = new Cart();
                cart.setUserId(userId);
                cart.setProductId(productId);
                cart.setQuantity(quantity);
                cart.setSelected(1);
                return cartMapper.insert(cart) > 0;
            }
        } catch (Exception e) {
            log.error("添加购物车失败", e);
            return false;
        }
    }

    @Override
    public List<Cart> getCartList(Long userId) {
        List<Cart> cartList = cartMapper.selectByUser(userId);
        // 计算每项的小计
        for (Cart cart : cartList) {
            if (cart.getProduct() != null) {
                BigDecimal price = cart.getProduct().getPrice();
                BigDecimal quantity = new BigDecimal(cart.getQuantity());
                cart.setSubtotal(price.multiply(quantity));
            }
        }
        return cartList;
    }

    @Override
    @Transactional
    public boolean updateQuantity(Long cartId, Integer quantity) {
        try {
            if (quantity <= 0) {
                return false;
            }

            Cart cart = cartMapper.selectById(cartId);
            if (cart == null) {
                return false;
            }

            // 检查库存
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || product.getStock() < quantity) {
                log.warn("商品库存不足");
                return false;
            }

            cart.setQuantity(quantity);
            return cartMapper.update(cart) > 0;
        } catch (Exception e) {
            log.error("更新购物车数量失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteCartItem(Long cartId) {
        try {
            return cartMapper.delete(cartId) > 0;
        } catch (Exception e) {
            log.error("删除购物车项失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean clearCart(Long userId) {
        try {
            return cartMapper.deleteByUser(userId) > 0;
        } catch (Exception e) {
            log.error("清空购物车失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateSelected(Long cartId, Integer selected) {
        try {
            Cart cart = new Cart();
            cart.setId(cartId);
            cart.setSelected(selected);
            return cartMapper.update(cart) > 0;
        } catch (Exception e) {
            log.error("更新选中状态失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean selectAll(Long userId, Integer selected) {
        try {
            return cartMapper.updateSelectAll(userId, selected) > 0;
        } catch (Exception e) {
            log.error("全选/取消全选失败", e);
            return false;
        }
    }

    @Override
    public Integer getCartCount(Long userId) {
        return cartMapper.countByUser(userId);
    }

    @Override
    public BigDecimal calculateTotalAmount(Long userId) {
        List<Cart> cartList = getSelectedCartItems(userId);
        BigDecimal total = BigDecimal.ZERO;
        for (Cart cart : cartList) {
            if (cart.getProduct() != null) {
                BigDecimal price = cart.getProduct().getPrice();
                BigDecimal quantity = new BigDecimal(cart.getQuantity());
                total = total.add(price.multiply(quantity));
            }
        }
        return total;
    }

    @Override
    public List<Cart> getSelectedCartItems(Long userId) {
        return cartMapper.selectSelectedByUser(userId);
    }
}