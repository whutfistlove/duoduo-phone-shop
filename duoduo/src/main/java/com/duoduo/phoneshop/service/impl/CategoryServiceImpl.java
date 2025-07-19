package com.duoduo.phoneshop.service.impl;

import com.duoduo.phoneshop.entity.Category;
import com.duoduo.phoneshop.mapper.CategoryMapper;
import com.duoduo.phoneshop.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类服务实现类
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.selectAll();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean addCategory(Category category) {
        try {
            category.setStatus(1); // 默认启用
            return categoryMapper.insert(category) > 0;
        } catch (Exception e) {
            log.error("添加分类失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateCategory(Category category) {
        try {
            return categoryMapper.update(category) > 0;
        } catch (Exception e) {
            log.error("更新分类失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteCategory(Long id) {
        try {
            // 检查分类下是否有商品
            int productCount = categoryMapper.countProducts(id);
            if (productCount > 0) {
                log.warn("分类下存在商品，不能删除");
                return false;
            }

            return categoryMapper.delete(id) > 0;
        } catch (Exception e) {
            log.error("删除分类失败", e);
            return false;
        }
    }
}