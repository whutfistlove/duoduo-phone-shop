package com.duoduo.phoneshop.service;

import com.duoduo.phoneshop.entity.Product;
import java.util.List;

/**
 * 商品服务接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
public interface ProductService {

    /**
     * 获取所有商品
     * @return 商品列表
     */
    List<Product> getAllProducts();

    /**
     * 根据分类ID查询商品
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<Product> getProductsByCategory(Long categoryId);

    /**
     * 根据关键词搜索商品
     * @param keyword 关键词
     * @return 商品列表
     */
    List<Product> searchProducts(String keyword);

    /**
     * 根据ID查询商品
     * @param id 商品ID
     * @return 商品信息
     */
    Product getProductById(Long id);

    /**
     * 添加商品
     * @param product 商品信息
     * @return 添加结果
     */
    boolean addProduct(Product product);

    /**
     * 更新商品
     * @param product 商品信息
     * @return 更新结果
     */
    boolean updateProduct(Product product);

    /**
     * 删除商品
     * @param id 商品ID
     * @return 删除结果
     */
    boolean deleteProduct(Long id);

    /**
     * 更新商品库存
     * @param productId 商品ID
     * @param quantity 数量(负数表示减少)
     * @return 更新结果
     */
    boolean updateStock(Long productId, Integer quantity);

    /**
     * 获取热门商品
     * @param limit 数量限制
     * @return 商品列表
     */
    List<Product> getHotProducts(Integer limit);
}