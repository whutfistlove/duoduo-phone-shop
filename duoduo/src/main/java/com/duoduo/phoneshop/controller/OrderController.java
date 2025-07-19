package com.duoduo.phoneshop.controller;

import com.duoduo.phoneshop.entity.Address;
import com.duoduo.phoneshop.entity.Cart;
import com.duoduo.phoneshop.entity.Order;
import com.duoduo.phoneshop.entity.User;
import com.duoduo.phoneshop.service.AddressService;
import com.duoduo.phoneshop.service.CartService;
import com.duoduo.phoneshop.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单控制器
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private AddressService addressService;

    /**
     * 创建订单页面
     */
    @GetMapping("/create")
    public String createOrderPage(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        // 获取选中的购物车商品
        List<Cart> selectedItems = cartService.getSelectedCartItems(currentUser.getId());
        if (selectedItems.isEmpty()) {
            return "redirect:/cart";
        }

        // 获取用户地址列表
        List<Address> addresses = addressService.getUserAddresses(currentUser.getId());

        // 计算总金额
        BigDecimal totalAmount = cartService.calculateTotalAmount(currentUser.getId());

        model.addAttribute("selectedItems", selectedItems);
        model.addAttribute("addresses", addresses);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("userBalance", currentUser.getBalance());

        return "user/order-create";
    }

    /**
     * 提交订单
     */
    @PostMapping("/submit")
    public String submitOrder(@RequestParam Long addressId,
                              @RequestParam(required = false) String remark,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        try {
            Order order = orderService.createOrder(currentUser.getId(), addressId, remark);
            if (order != null) {
                redirectAttributes.addFlashAttribute("orderId", order.getId());
                return "redirect:/order/pay/" + order.getId();
            } else {
                redirectAttributes.addFlashAttribute("error", "创建订单失败，请检查商品库存");
                return "redirect:/cart";
            }
        } catch (Exception e) {
            log.error("创建订单失败", e);
            redirectAttributes.addFlashAttribute("error", "创建订单失败，请重试");
            return "redirect:/cart";
        }
    }

    /**
     * 支付页面
     */
    @GetMapping("/pay/{orderId}")
    public String payPage(@PathVariable Long orderId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null || !order.getUserId().equals(currentUser.getId())) {
            return "redirect:/order/list";
        }

        if (order.getStatus() != 0) {
            return "redirect:/order/detail/" + orderId;
        }

        model.addAttribute("order", order);
        model.addAttribute("userBalance", currentUser.getBalance());

        return "user/order-pay";
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay")
    public String payOrder(@RequestParam Long orderId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        try {
            if (orderService.payOrder(orderId, currentUser.getId())) {
                // 更新session中的用户余额
                User updatedUser = (User) session.getAttribute("currentUser");
                session.setAttribute("currentUser", updatedUser);

                redirectAttributes.addFlashAttribute("success", "支付成功");
                return "redirect:/order/detail/" + orderId;
            } else {
                redirectAttributes.addFlashAttribute("error", "支付失败，请检查余额是否充足");
                return "redirect:/order/pay/" + orderId;
            }
        } catch (Exception e) {
            log.error("支付订单失败", e);
            redirectAttributes.addFlashAttribute("error", "支付失败，请重试");
            return "redirect:/order/pay/" + orderId;
        }
    }

    /**
     * 订单列表
     */
    @GetMapping("/list")
    public String orderList(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        List<Order> orders = orderService.getUserOrders(currentUser.getId());
        model.addAttribute("orders", orders);

        return "user/order-list";
    }

    /**
     * 订单详情
     */
    @GetMapping("/detail/{orderId}")
    public String orderDetail(@PathVariable Long orderId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null || !order.getUserId().equals(currentUser.getId())) {
            return "redirect:/order/list";
        }

        model.addAttribute("order", order);

        return "user/order-detail";
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public String cancelOrder(@RequestParam Long orderId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        try {
            if (orderService.cancelOrder(orderId, currentUser.getId())) {
                redirectAttributes.addFlashAttribute("success", "订单已取消");
            } else {
                redirectAttributes.addFlashAttribute("error", "取消失败，只能取消待支付的订单");
            }
        } catch (Exception e) {
            log.error("取消订单失败", e);
            redirectAttributes.addFlashAttribute("error", "取消失败，请重试");
        }

        return "redirect:/order/detail/" + orderId;
    }

    /**
     * 确认收货
     */
    @PostMapping("/confirm")
    public String confirmReceive(@RequestParam Long orderId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        try {
            if (orderService.confirmReceive(orderId, currentUser.getId())) {
                redirectAttributes.addFlashAttribute("success", "确认收货成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "确认失败，请检查订单状态");
            }
        } catch (Exception e) {
            log.error("确认收货失败", e);
            redirectAttributes.addFlashAttribute("error", "确认失败，请重试");
        }

        return "redirect:/order/detail/" + orderId;
    }
}