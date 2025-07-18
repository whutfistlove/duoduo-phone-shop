package com.duoduo.phoneshop.mapper;

import com.duoduo.phoneshop.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品数据访问接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Mapper
public interface ProductMapper {

    /**
     * 插入商品
     * @param product 商品信息
     * @return 影响行数
     */
    int insert(Product product);

    /**
     * 根据ID查询商品
     * @param id 商品ID
     * @return 商品信息
     */
    Product selectById(@Param("id") Long id);

    /**
     * 查询所有商品
     * @return 商品列表
     */
    List<Product> selectAll();

    /**
     * 根据分类查询商品
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<Product> selectByCategory(@Param("categoryId") Long categoryId);

    /**
     * 搜索商品
     * @param keyword 关键词
     * @return 商品列表
     */
    List<Product> search(@Param("keyword") String keyword);

    /**
     * 更新商品
     * @param product 商品信息
     * @return 影响行数
     */
    int update(Product product);

    /**
     * 删除商品
     * @param id 商品ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);

    /**
     * 更新库存
     * @param id 商品ID
     * @param quantity 数量变化(负数表示减少)
     * @return 影响行数
     */
    int updateStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 查询热门商品
     * @param limit 数量限制
     * @return 商品列表
     */
    List<Product> selectHotProducts(@Param("limit") Integer limit);
}