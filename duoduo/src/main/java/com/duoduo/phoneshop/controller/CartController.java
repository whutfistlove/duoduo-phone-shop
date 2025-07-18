package com.duoduo.phoneshop.controller;

import com.duoduo.phoneshop.entity.Cart;
import com.duoduo.phoneshop.entity.User;
import com.duoduo.phoneshop.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 购物车页面
     */
    @GetMapping("")
    public String cartList(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        List<Cart> cartList = cartService.getCartList(currentUser.getId());
        BigDecimal totalAmount = cartService.calculateTotalAmount(currentUser.getId());

        model.addAttribute("cartList", cartList);
        model.addAttribute("totalAmount", totalAmount);

        return "user/cart";
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addToCart(@RequestParam Long productId,
                                         @RequestParam(defaultValue = "1") Integer quantity,
                                         HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            if (cartService.addToCart(currentUser.getId(), productId, quantity)) {
                result.put("success", true);
                result.put("message", "添加成功");
                result.put("cartCount", cartService.getCartCount(currentUser.getId()));
            } else {
                result.put("success", false);
                result.put("message", "添加失败，请检查商品库存");
            }
        } catch (Exception e) {
            log.error("添加购物车失败", e);
            result.put("success", false);
            result.put("message", "操作失败");
        }

        return result;
    }

    /**
     * 更新购物车商品数量
     */
    @PostMapping("/updateQuantity")
    @ResponseBody
    public Map<String, Object> updateQuantity(@RequestParam Long cartId,
                                              @RequestParam Integer quantity,
                                              HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            if (cartService.updateQuantity(cartId, quantity)) {
                BigDecimal totalAmount = cartService.calculateTotalAmount(currentUser.getId());
                result.put("success", true);
                result.put("totalAmount", totalAmount);
                result.put("message", "更新成功");
            } else {
                result.put("success", false);
                result.put("message", "更新失败，请检查商品库存");
            }
        } catch (Exception e) {
            log.error("更新购物车数量失败", e);
            result.put("success", false);
            result.put("message", "操作失败");
        }

        return result;
    }

    /**
     * 删除购物车商品
     */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteCartItem(@RequestParam Long cartId,
                                              HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            if (cartService.deleteCartItem(cartId)) {
                BigDecimal totalAmount = cartService.calculateTotalAmount(currentUser.getId());
                result.put("success", true);
                result.put("totalAmount", totalAmount);
                result.put("cartCount", cartService.getCartCount(currentUser.getId()));
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "删除失败");
            }
        } catch (Exception e) {
            log.error("删除购物车商品失败", e);
            result.put("success", false);
            result.put("message", "操作失败");
        }

        return result;
    }

    /**
     * 清空购物车
     */
    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        try {
            if (cartService.clearCart(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("success", "购物车已清空");
            } else {
                redirectAttributes.addFlashAttribute("error", "清空失败");
            }
        } catch (Exception e) {
            log.error("清空购物车失败", e);
            redirectAttributes.addFlashAttribute("error", "操作失败");
        }

        return "redirect:/cart";
    }

    /**
     * 更新选中状态
     */
    @PostMapping("/updateSelected")
    @ResponseBody
    public Map<String, Object> updateSelected(@RequestParam Long cartId,
                                              @RequestParam Integer selected,
                                              HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            if (cartService.updateSelected(cartId, selected)) {
                BigDecimal totalAmount = cartService.calculateTotalAmount(currentUser.getId());
                result.put("success", true);
                result.put("totalAmount", totalAmount);
            } else {
                result.put("success", false);
                result.put("message", "更新失败");
            }
        } catch (Exception e) {
            log.error("更新选中状态失败", e);
            result.put("success", false);
            result.put("message", "操作失败");
        }

        return result;
    }

    /**
     * 全选/取消全选
     */
    @PostMapping("/selectAll")
    @ResponseBody
    public Map<String, Object> selectAll(@RequestParam Integer selected,
                                         HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            if (cartService.selectAll(currentUser.getId(), selected)) {
                BigDecimal totalAmount = cartService.calculateTotalAmount(currentUser.getId());
                result.put("success", true);
                result.put("totalAmount", totalAmount);
            } else {
                result.put("success", false);
                result.put("message", "操作失败");
            }
        } catch (Exception e) {
            log.error("全选/取消全选失败", e);
            result.put("success", false);
            result.put("message", "操作失败");
        }

        return result;
    }

    /**
     * 获取购物车数量
     */
    @GetMapping("/count")
    @ResponseBody
    public Integer getCartCount(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return 0;
        }

        return cartService.getCartCount(currentUser.getId());
    }

    /**
     * 去结算
     */
    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        List<Cart> selectedItems = cartService.getSelectedCartItems(currentUser.getId());
        if (selectedItems.isEmpty()) {
            return "redirect:/cart";
        }

        BigDecimal totalAmount = cartService.calculateTotalAmount(currentUser.getId());

        model.addAttribute("selectedItems", selectedItems);
        model.addAttribute("totalAmount", totalAmount);

        return "redirect:/order/create";
    }
}