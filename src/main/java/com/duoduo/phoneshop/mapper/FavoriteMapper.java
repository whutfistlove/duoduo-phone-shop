package com.duoduo.phoneshop.mapper;

import com.duoduo.phoneshop.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏数据访问接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Mapper
public interface FavoriteMapper {

    /**
     * 插入收藏
     * @param favorite 收藏信息
     * @return 影响行数
     */
    int insert(Favorite favorite);

    /**
     * 根据用户ID查询收藏列表
     * @param userId 用户ID
     * @return 收藏列表
     */
    List<Favorite> selectByUser(@Param("userId") Long userId);

    /**
     * 删除收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 影响行数
     */
    int deleteByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 删除用户所有收藏
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUser(@Param("userId") Long userId);

    /**
     * 统计用户收藏数量
     * @param userId 用户ID
     * @return 收藏数量
     */
    int countByUser(@Param("userId") Long userId);

    /**
     * 统计用户对某商品的收藏数
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 收藏数(0或1)
     */
    int countByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}