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
import java.util.*;
import java.util.stream.Collectors;

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
    public List<User> searchUsers(String keyword) {
        try {
            // 如果有 UserMapper 的搜索方法，直接调用
            // return userMapper.searchUsers(keyword);

            // 如果没有数据库层面的搜索，可以先获取所有用户然后过滤
            List<User> allUsers = userMapper.selectAll();
            if (keyword == null || keyword.trim().isEmpty()) {
                return allUsers;
            }

            String lowerKeyword = keyword.toLowerCase();
            return allUsers.stream()
                    .filter(user ->
                            (user.getUsern() != null && user.getUsern().toLowerCase().contains(lowerKeyword)) ||
                                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerKeyword)) ||
                                    (user.getPhone() != null && user.getPhone().contains(keyword)) ||
                                    (user.getRealN() != null && user.getRealN().toLowerCase().contains(lowerKeyword))
                    )
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("搜索用户失败", e);
            return new ArrayList<>();
        }
    }
    @Override
    public User login(String usern, String password) {
        log.info("===== 开始登录验证 =====");
        log.info("输入的用户名: {}", usern);

        User user = userMapper.selectByUsern(usern);

        if (user == null) {
            log.error("用户不存在: {}", usern);
            return null;
        }

        log.info("查询到用户信息:");
        log.info("- 用户ID: {}", user.getId());
        log.info("- 用户名: {}", user.getUsern());
        log.info("- 邮箱: {}", user.getEmail());
        log.info("- 角色: {}", user.getRole());
        log.info("- 状态: {}", user.getStatus());
        log.info("- 数据库中的密码: {}", user.getPassword());

        // 测试密码验证
        boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
        log.info("密码验证结果: {}", passwordMatch);

        // 额外的密码验证测试
        if (!passwordMatch) {
            log.error("密码不匹配!");
            log.info("尝试验证 'admin123': {}", passwordEncoder.matches("admin123", user.getPassword()));

            // 生成新的密码供参考
            String newEncodedPassword = passwordEncoder.encode("admin123");
            log.info("'admin123' 的新加密密码: {}", newEncodedPassword);
            log.info("建议执行SQL: UPDATE user SET password = '{}' WHERE usern = 'admin';", newEncodedPassword);
        }

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            log.info("登录成功!");
            return user;
        }

        log.error("登录失败!");
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