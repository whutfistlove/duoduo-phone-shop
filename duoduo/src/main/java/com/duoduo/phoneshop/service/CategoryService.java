package com.duoduo.phoneshop.service;

import com.duoduo.phoneshop.entity.Category;
import java.util.List;

/**
 * 分类服务接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
public interface CategoryService {

    /**
     * 获取所有分类
     * @return 分类列表
     */
    List<Category> getAllCategories();

    /**
     * 根据ID查询分类
     * @param id 分类ID
     * @return 分类信息
     */
    Category getCategoryById(Long id);

    /**
     * 添加分类
     * @param category 分类信息
     * @return 添加结果
     */
    boolean addCategory(Category category);

    /**
     * 更新分类
     * @param category 分类信息
     * @return 更新结果
     */
    boolean updateCategory(Category category);

    /**
     * 删除分类
     * @param id 分类ID
     * @return 删除结果
     */
    boolean deleteCategory(Long id);
}