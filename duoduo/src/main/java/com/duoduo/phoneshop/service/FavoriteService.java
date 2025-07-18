package com.duoduo.phoneshop.service;

import com.duoduo.phoneshop.entity.Favorite;
import java.util.List;

/**
 * 收藏服务接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
public interface FavoriteService {

    /**
     * 添加收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 添加结果
     */
    boolean addFavorite(Long userId, Long productId);

    /**
     * 获取用户收藏列表
     * @param userId 用户ID
     * @return 收藏列表
     */
    List<Favorite> getUserFavorites(Long userId);

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 取消结果
     */
    boolean removeFavorite(Long userId, Long productId);

    /**
     * 清空收藏
     * @param userId 用户ID
     * @return 清空结果
     */
    boolean clearFavorites(Long userId);

    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 是否已收藏
     */
    boolean isFavorite(Long userId, Long productId);

    /**
     * 获取收藏数量
     * @param userId 用户ID
     * @return 收藏数量
     */
    Integer getFavoriteCount(Long userId);
}