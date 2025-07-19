package com.duoduo.phoneshop.mapper;

import com.duoduo.phoneshop.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Mapper
public interface UserMapper {

    /**
     * 插入用户
     * @param user 用户信息
     * @return 影响行数
     */
    int insert(User user);
    // 在 UserMapper 接口中添加
    /**
     * 搜索用户
     * @param keyword 关键字
     * @return 用户列表
     */
    List<User> searchUsers(@Param("keyword") String keyword);
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     * @param usern 用户名
     * @return 用户信息
     */
    User selectByUsern(@Param("usern") String usern);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 影响行数
     */
    int update(User user);

    /**
     * 删除用户
     * @param id 用户ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> selectAll();

    /**
     * 根据角色查询用户
     * @param role 角色
     * @return 用户列表
     */
    List<User> selectByRole(@Param("role") String role);
}