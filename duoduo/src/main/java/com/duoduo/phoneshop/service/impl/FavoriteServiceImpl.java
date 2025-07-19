package com.duoduo.phoneshop.service.impl;

import com.duoduo.phoneshop.entity.Favorite;
import com.duoduo.phoneshop.entity.Product;
import com.duoduo.phoneshop.mapper.FavoriteMapper;
import com.duoduo.phoneshop.mapper.ProductMapper;
import com.duoduo.phoneshop.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收藏服务实现类
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional
    public boolean addFavorite(Long userId, Long productId) {
        try {
            // 检查商品是否存在
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStatus() != 1) {
                log.warn("商品不存在或已下架，ID: {}", productId);
                return false;
            }

            // 检查是否已收藏
            if (isFavorite(userId, productId)) {
                log.warn("商品已收藏，用户ID: {}, 商品ID: {}", userId, productId);
                return false;
            }

            // 添加收藏
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);

            return favoriteMapper.insert(favorite) > 0;
        } catch (Exception e) {
            log.error("添加收藏失败", e);
            return false;
        }
    }

    @Override
    public List<Favorite> getUserFavorites(Long userId) {
        List<Favorite> favorites = favoriteMapper.selectByUser(userId);
        // 填充商品信息
        for (Favorite favorite : favorites) {
            Product product = productMapper.selectById(favorite.getProductId());
            favorite.setProduct(product);
        }
        return favorites;
    }

    @Override
    @Transactional
    public boolean removeFavorite(Long userId, Long productId) {
        try {
            return favoriteMapper.deleteByUserAndProduct(userId, productId) > 0;
        } catch (Exception e) {
            log.error("取消收藏失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean clearFavorites(Long userId) {
        try {
            return favoriteMapper.deleteByUser(userId) > 0;
        } catch (Exception e) {
            log.error("清空收藏失败", e);
            return false;
        }
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        return favoriteMapper.countByUserAndProduct(userId, productId) > 0;
    }

    @Override
    public Integer getFavoriteCount(Long userId) {
        return favoriteMapper.countByUser(userId);
    }
}