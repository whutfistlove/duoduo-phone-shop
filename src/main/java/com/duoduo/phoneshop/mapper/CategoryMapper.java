package com.duoduo.phoneshop.mapper;

import com.duoduo.phoneshop.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类数据访问接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Mapper
public interface CategoryMapper {

    /**
     * 插入分类
     * @param category 分类信息
     * @return 影响行数
     */
    int insert(Category category);

    /**
     * 根据ID查询分类
     * @param id 分类ID
     * @return 分类信息
     */
    Category selectById(@Param("id") Long id);

    /**
     * 查询所有分类
     * @return 分类列表
     */
    List<Category> selectAll();

    /**
     * 更新分类
     * @param category 分类信息
     * @return 影响行数
     */
    int update(Category category);

    /**
     * 删除分类
     * @param id 分类ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);

    /**
     * 统计分类下的商品数量
     * @param id 分类ID
     * @return 商品数量
     */
    int countProducts(@Param("id") Long id);
}