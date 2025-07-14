package com.duoduo.phoneshop.service.impl;

import com.duoduo.phoneshop.entity.Product;
import com.duoduo.phoneshop.mapper.ProductMapper;
import com.duoduo.phoneshop.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品服务实现类
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> getAllProducts() {
        return productMapper.selectAll();
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productMapper.selectByCategory(categoryId);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        return productMapper.search(keyword);
    }

    @Override
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean addProduct(Product product) {
        try {
            product.setStatus(1); // 默认上架
            return productMapper.insert(product) > 0;
        } catch (Exception e) {
            log.error("添加商品失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateProduct(Product product) {
        try {
            return productMapper.update(product) > 0;
        } catch (Exception e) {
            log.error("更新商品失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteProduct(Long id) {
        try {
            return productMapper.delete(id) > 0;
        } catch (Exception e) {
            log.error("删除商品失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateStock(Long productId, Integer quantity) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null) {
                return false;
            }

            int newStock = product.getStock() + quantity;
            if (newStock < 0) {
                log.warn("库存不足，商品ID: {}", productId);
                return false;
            }

            return productMapper.updateStock(productId, quantity) > 0;
        } catch (Exception e) {
            log.error("更新库存失败", e);
            return false;
        }
    }

    @Override
    public List<Product> getHotProducts(Integer limit) {
        return productMapper.selectHotProducts(limit);
    }
}