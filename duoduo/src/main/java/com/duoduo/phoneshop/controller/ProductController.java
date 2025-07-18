package com.duoduo.phoneshop.controller;

import com.duoduo.phoneshop.entity.Address;
import com.duoduo.phoneshop.entity.User;
import com.duoduo.phoneshop.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地址管理控制器
 * 注：根据URL映射推测，ProductController实际处理地址管理功能
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Controller
@RequestMapping("/address")
public class ProductController {

    @Autowired
    private AddressService addressService;

    /**
     * 地址列表页面
     */
    @GetMapping("")
    public String addressList(Model model, HttpSession session) {
        User u = (User) session.getAttribute("currentUser");
        if (u == null) {
            return "redirect:/user/login";
        }
        List<Address> list = addressService.getUserAddresses(u.getId());
        model.addAttribute("addresses", list);
        // 新增时用一个空对象
        model.addAttribute("address", new Address());
        return "user/address";
    }

    /**
     * 获取地址详情（用于编辑）- AJAX接口
     */
    @GetMapping("/get/{id}")
    @ResponseBody
    public Map<String, Object> getAddress(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Address address = addressService.getAddressById(id);
            if (address == null || !address.getUserId().equals(currentUser.getId())) {
                result.put("success", false);
                result.put("message", "地址不存在");
                return result;
            }

            result.put("success", true);
            result.put("data", address);
        } catch (Exception e) {
            log.error("获取地址失败", e);
            result.put("success", false);
            result.put("message", "获取地址失败");
        }

        return result;
    }

    /**
     * 添加地址页面
     */
    @GetMapping("/add")
    public String addAddressPage(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        return "user/address-add";
    }

    /**
     * 添加地址
     */
    @PostMapping("/add")
    public String addAddress(@ModelAttribute Address address,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        try {
            address.setUserId(currentUser.getId());
            if (addressService.addAddress(address)) {
                redirectAttributes.addFlashAttribute("success", "地址添加成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "添加失败");
            }
        } catch (Exception e) {
            log.error("添加地址失败", e);
            redirectAttributes.addFlashAttribute("error", "添加失败，请重试");
        }

        return "redirect:/address";
    }

    /**
     * 编辑地址页面 - 已废弃，改用AJAX方式
     */
    @Deprecated
    @GetMapping("/edit/{id}")
    public String editAddressPage(@PathVariable Long id, Model model, HttpSession session) {
        User u = (User) session.getAttribute("currentUser");
        if (u == null) {
            return "redirect:/user/login";
        }
        Address a = addressService.getAddressById(id);
        if (a == null || !a.getUserId().equals(u.getId())) {
            return "redirect:/address";
        }
        List<Address> list = addressService.getUserAddresses(u.getId());
        model.addAttribute("addresses", list);
        // 编辑时塞入要回显的实体
        model.addAttribute("address", a);
        return "user/address";
    }

    /**
     * 更新地址
     */
    @PostMapping("/update")
    public String updateAddress(@ModelAttribute Address address,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        try {
            // 验证地址ID不为空
            if (address.getId() == null) {
                redirectAttributes.addFlashAttribute("error", "更新失败：地址ID不能为空");
                return "redirect:/address";
            }

            // 验证地址属于当前用户
            Address existingAddress = addressService.getAddressById(address.getId());
            if (existingAddress == null || !existingAddress.getUserId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "更新失败：地址不存在");
                return "redirect:/address";
            }

            address.setUserId(currentUser.getId());
            if (addressService.updateAddress(address)) {
                redirectAttributes.addFlashAttribute("success", "地址更新成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "更新失败");
            }
        } catch (Exception e) {
            log.error("更新地址失败", e);
            redirectAttributes.addFlashAttribute("error", "更新失败，请重试");
        }

        return "redirect:/address";
    }

    /**
     * 删除地址
     */
    @PostMapping("/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteAddress(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            if (addressService.deleteAddress(id, currentUser.getId())) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "删除失败");
            }
        } catch (Exception e) {
            log.error("删除地址失败", e);
            result.put("success", false);
            result.put("message", "操作失败");
        }

        return result;
    }

    /**
     * 设置默认地址
     */
    @PostMapping("/setDefault/{id}")
    @ResponseBody
    public Map<String, Object> setDefaultAddress(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            if (addressService.setDefaultAddress(id, currentUser.getId())) {
                result.put("success", true);
                result.put("message", "设置成功");
            } else {
                result.put("success", false);
                result.put("message", "设置失败");
            }
        } catch (Exception e) {
            log.error("设置默认地址失败", e);
            result.put("success", false);
            result.put("message", "操作失败");
        }

        return result;
    }
}