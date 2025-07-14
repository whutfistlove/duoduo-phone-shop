package com.duoduo.phoneshop.service.impl;

import com.duoduo.phoneshop.entity.User;
import com.duoduo.phoneshop.mapper.UserMapper;
import com.duoduo.phoneshop.service.UserService;
import com.duoduo.phoneshop.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 用户服务实现类
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 存储验证码，实际项目中应使用Redis
    private static final Map<String, String> CODE_MAP = new HashMap<>();

    @Override
    @Transactional
    public boolean register(User user) {
        try {
            // 检查用户名是否已存在
            User existUser = userMapper.selectByUsern(user.getUsern());
            if (existUser != null) {
                log.warn("用户名已存在: {}", user.getUsern());
                return false;
            }

            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            // 设置默认值
            user.setRole("USER");
            user.setStatus(1);
            user.setBalance(new BigDecimal("0"));

            return userMapper.insert(user) > 0;
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return false;
        }
    }

    @Override
    public User login(String usern, String password) {
        User user = userMapper.selectByUsern(usern);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getUserByUsern(String usern) {
        return userMapper.selectByUsern(usern);
    }

    @Override
    @Transactional
    public boolean updateUser(User user) {
        try {
            return userMapper.update(user) > 0;
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return false;
            }

            // 验证旧密码
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                log.warn("旧密码错误");
                return false;
            }

            // 更新新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            return userMapper.update(user) > 0;
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean recharge(Long userId, BigDecimal amount) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            user.setBalance(user.getBalance().add(amount));
            return userMapper.update(user) > 0;
        } catch (Exception e) {
            log.error("账户充值失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deductBalance(Long userId, BigDecimal amount) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            // 检查余额是否充足
            if (user.getBalance().compareTo(amount) < 0) {
                log.warn("账户余额不足");
                return false;
            }

            user.setBalance(user.getBalance().subtract(amount));
            return userMapper.update(user) > 0;
        } catch (Exception e) {
            log.error("账户扣款失败", e);
            return false;
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        try {
            return userMapper.delete(id) > 0;
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return false;
        }
    }

    @Override
    public boolean sendRegisterCode(String email) {
        try {
            // 生成6位验证码
            String code = String.format("%06d", new Random().nextInt(999999));
            CODE_MAP.put(email, code);

            // 发送邮件
            String subject = "多多购手机网站-注册验证码";
            String content = "您的注册验证码是：" + code + "，有效期10分钟。";

            return emailUtil.sendEmail(email, subject, content);
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            return false;
        }
    }

    @Override
    public boolean verifyRegisterCode(String email, String code) {
        String savedCode = CODE_MAP.get(email);
        if (savedCode != null && savedCode.equals(code)) {
            CODE_MAP.remove(email);
            return true;
        }
        return false;
    }
}