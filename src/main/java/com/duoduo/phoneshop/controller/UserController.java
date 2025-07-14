package com.duoduo.phoneshop.controller;

import com.duoduo.phoneshop.entity.User;
import com.duoduo.phoneshop.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public String login(@RequestParam String usern,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            User user = userService.login(usern, password);
            if (user != null) {
                session.setAttribute("currentUser", user);
                log.info("用户登录成功: {}", usern);

                // 根据角色跳转
                if ("ADMIN".equals(user.getRole())) {
                    return "redirect:/admin/dashboard";
                } else {
                    return "redirect:/";
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "用户名或密码错误");
                return "redirect:/user/login";
            }
        } catch (Exception e) {
            log.error("登录失败", e);
            redirectAttributes.addFlashAttribute("error", "登录失败，请重试");
            return "redirect:/user/login";
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                           @RequestParam String code,
                           RedirectAttributes redirectAttributes) {
        try {
            // 验证注册码
            if (!userService.verifyRegisterCode(user.getEmail(), code)) {
                redirectAttributes.addFlashAttribute("error", "验证码错误或已过期");
                return "redirect:/user/register";
            }

            // 注册用户
            if (userService.register(user)) {
                redirectAttributes.addFlashAttribute("success", "注册成功，请登录");
                return "redirect:/user/login";
            } else {
                redirectAttributes.addFlashAttribute("error", "用户名已存在");
                return "redirect:/user/register";
            }
        } catch (Exception e) {
            log.error("注册失败", e);
            redirectAttributes.addFlashAttribute("error", "注册失败，请重试");
            return "redirect:/user/register";
        }
    }

    /**
     * 发送注册验证码
     */
    @PostMapping("/sendCode")
    @ResponseBody
    public Map<String, Object> sendCode(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (userService.sendRegisterCode(email)) {
                result.put("success", true);
                result.put("message", "验证码已发送到您的邮箱");
            } else {
                result.put("success", false);
                result.put("message", "发送失败，请检查邮箱地址");
            }
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            result.put("success", false);
            result.put("message", "发送失败，请重试");
        }
        return result;
    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    /**
     * 个人中心
     */
    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        // 重新查询用户信息
        User user = userService.getUserById(currentUser.getId());
        model.addAttribute("user", user);

        return "user/profile";
    }

    /**
     * 更新个人信息
     */
    @PostMapping("/updateProfile")
    public String updateProfile(@ModelAttribute User user,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        try {
            user.setId(currentUser.getId());
            if (userService.updateUser(user)) {
                // 更新session中的用户信息
                User updatedUser = userService.getUserById(currentUser.getId());
                session.setAttribute("currentUser", updatedUser);
                redirectAttributes.addFlashAttribute("success", "信息更新成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "更新失败");
            }
        } catch (Exception e) {
            log.error("更新个人信息失败", e);
            redirectAttributes.addFlashAttribute("error", "更新失败，请重试");
        }

        return "redirect:/user/profile";
    }

    /**
     * 修改密码
     */
    @PostMapping("/changePassword")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        try {
            if (userService.changePassword(currentUser.getId(), oldPassword, newPassword)) {
                redirectAttributes.addFlashAttribute("success", "密码修改成功，请重新登录");
                session.invalidate();
                return "redirect:/user/login";
            } else {
                redirectAttributes.addFlashAttribute("error", "原密码错误");
            }
        } catch (Exception e) {
            log.error("修改密码失败", e);
            redirectAttributes.addFlashAttribute("error", "修改失败，请重试");
        }

        return "redirect:/user/profile";
    }

    /**
     * 充值页面
     */
    @GetMapping("/recharge")
    public String rechargePage(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        User user = userService.getUserById(currentUser.getId());
        model.addAttribute("user", user);

        return "user/recharge";
    }

    /**
     * 账户充值
     */
    @PostMapping("/recharge")
    public String recharge(@RequestParam BigDecimal amount,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        try {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("error", "充值金额必须大于0");
            } else if (userService.recharge(currentUser.getId(), amount)) {
                // 更新session中的用户信息
                User updatedUser = userService.getUserById(currentUser.getId());
                session.setAttribute("currentUser", updatedUser);
                redirectAttributes.addFlashAttribute("success", "充值成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "充值失败");
            }
        } catch (Exception e) {
            log.error("充值失败", e);
            redirectAttributes.addFlashAttribute("error", "充值失败，请重试");
        }

        return "redirect:/user/recharge";
    }
}