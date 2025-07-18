package com.duoduo.phoneshop.service;

import com.duoduo.phoneshop.entity.User;
import java.math.BigDecimal;
import java.util.List;

/**
 * 用户服务接口
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
public interface UserService {

    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册结果
     */
    boolean register(User user);

    /**
     * 用户登录
     * @param usern 用户名
     * @param password 密码
     * @return 用户信息
     */
    User login(String usern, String password);

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    User getUserById(Long id);

    /**
     * 根据用户名查询用户
     * @param usern 用户名
     * @return 用户信息
     */
    User getUserByUsern(String usern);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新结果
     */
    boolean updateUser(User user);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 账户充值
     * @param userId 用户ID
     * @param amount 充值金额
     * @return 充值结果
     */
    boolean recharge(Long userId, BigDecimal amount);

    /**
     * 账户扣款
     * @param userId 用户ID
     * @param amount 扣款金额
     * @return 扣款结果
     */
    boolean deductBalance(Long userId, BigDecimal amount);

    /**
     * 查询所有用户(管理员)
     * @return 用户列表
     */
    List<User> getAllUsers();

    /**
     * 删除用户(管理员)
     * @param id 用户ID
     * @return 删除结果
     */
    boolean deleteUser(Long id);

    /**
     * 发送注册验证码
     * @param email 邮箱
     * @return 发送结果
     */
    boolean sendRegisterCode(String email);

    /**
     * 验证注册码
     * @param email 邮箱
     * @param code 验证码
     * @return 验证结果
     */
    boolean verifyRegisterCode(String email, String code);
}