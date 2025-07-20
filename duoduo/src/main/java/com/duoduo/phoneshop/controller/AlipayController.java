package com.duoduo.phoneshop.controller;

import com.alipay.api.AlipayApiException;
import com.duoduo.phoneshop.entity.Order;
import com.duoduo.phoneshop.entity.User;
import com.duoduo.phoneshop.service.AlipayService;
import com.duoduo.phoneshop.service.OrderService;
import com.duoduo.phoneshop.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付控制器
 *
 * @author DuoDuo
 * @date 2025/01/21
 */
@Slf4j
@Controller
@RequestMapping("/alipay")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    /**
     * 创建充值支付订单
     */
    @PostMapping("/recharge/create")
    @ResponseBody
    public Map<String, Object> createRechargeOrder(
            @RequestParam BigDecimal amount,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            // 生成订单号
            String outTradeNo = alipayService.generateTradeNo("RECHARGE");

            // 保存充值订单信息到session（实际项目应该保存到数据库）
            Map<String, Object> rechargeInfo = new HashMap<>();
            rechargeInfo.put("outTradeNo", outTradeNo);
            rechargeInfo.put("userId", currentUser.getId());
            rechargeInfo.put("amount", amount);
            rechargeInfo.put("status", "PENDING");
            session.setAttribute("recharge_" + outTradeNo, rechargeInfo);

            // 创建支付宝支付
            String subject = "多多手机商城-账户充值";
            String form = alipayService.createRechargePayment(outTradeNo, amount, subject);

            result.put("success", true);
            result.put("form", form);
            result.put("outTradeNo", outTradeNo);

        } catch (AlipayApiException e) {
            log.error("创建充值订单失败", e);
            result.put("success", false);
            result.put("message", "创建支付订单失败，请重试");
        }

        return result;
    }

    /**
     * 创建订单支付
     */
    @PostMapping("/order/create")
    @ResponseBody
    public Map<String, Object> createOrderPayment(
            @RequestParam Long orderId,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            // 获取订单信息
            Order order = orderService.getOrderById(orderId);
            if (order == null || !order.getUserId().equals(currentUser.getId())) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }

            if (order.getStatus() != 0) {
                result.put("success", false);
                result.put("message", "订单状态错误");
                return result;
            }

            // 创建支付宝支付
            String subject = "多多手机商城-订单支付";
            String body = "订单号：" + order.getOrderNo();
            String form = alipayService.createOrderPayment(order.getOrderNo(), order.getPayAmount(), subject, body);

            result.put("success", true);
            result.put("form", form);
            result.put("orderNo", order.getOrderNo());

        } catch (Exception e) {
            log.error("创建订单支付失败", e);
            result.put("success", false);
            result.put("message", "创建支付订单失败，请重试");
        }

        return result;
    }

    /**
     * 支付宝同步回调（页面跳转）
     */
    @GetMapping("/return")
    public String returnCallback(HttpServletRequest request, Model model, HttpSession session) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        log.info("支付宝同步回调参数: {}", params);

        // 验证签名
        boolean signVerified = alipayService.verifySignature(params);

        if (signVerified) {
            // 获取订单信息
            String outTradeNo = params.get("out_trade_no");
            String totalAmount = params.get("total_amount");
            String tradeNo = params.get("trade_no");

            model.addAttribute("success", true);
            model.addAttribute("outTradeNo", outTradeNo);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("tradeNo", tradeNo);

            // 判断是充值还是订单支付
            if (outTradeNo.startsWith("RECHARGE_")) {
                model.addAttribute("type", "recharge");
                // 处理充值结果
                processRechargeResult(params, session);
            } else if (outTradeNo.startsWith("ORDER_")) {
                model.addAttribute("type", "order");
                // 订单支付结果处理在异步通知中进行
            }

        } else {
            model.addAttribute("success", false);
            model.addAttribute("message", "签名验证失败");
        }

        return "user/pay-result";
    }

    /**
     * 支付宝异步通知 - 同时支持GET和POST请求
     *
     * 注意：虽然支付宝文档说明异步通知使用POST方式，但在某些情况下
     * （如支付宝验证通知URL时）可能会发送GET请求，所以这里同时支持两种方式
     */
    @RequestMapping(value = "/notify", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String notifyCallback(HttpServletRequest request, HttpSession session) {
        String method = request.getMethod();
        log.info("支付宝异步通知请求方式: {}", method);

        // 如果是GET请求，可能是支付宝在验证URL，直接返回success
        if ("GET".equalsIgnoreCase(method)) {
            log.info("支付宝GET请求验证通知URL");
            return "success";
        }

        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        log.info("支付宝异步通知参数: {}", params);

        // 验证签名
        boolean signVerified = alipayService.verifySignature(params);

        if (signVerified) {
            // 处理支付结果
            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");

            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 判断是充值还是订单支付
                if (outTradeNo.startsWith("RECHARGE_")) {
                    // 处理充值
                    processRechargeResult(params, session);
                } else {
                    // 处理订单支付
                    processOrderPaymentResult(params);
                }
            }

            // 返回success告诉支付宝已收到通知
            return "success";
        } else {
            log.error("异步通知签名验证失败");
            return "fail";
        }
    }

    /**
     * 处理充值结果
     */
    private void processRechargeResult(Map<String, String> params, HttpSession session) {
        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        String totalAmount = params.get("total_amount");

        log.info("处理充值结果 - 订单号: {}, 状态: {}, 金额: {}", outTradeNo, tradeStatus, totalAmount);

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // 从session获取充值信息（实际项目应该从数据库获取）
            Map<String, Object> rechargeInfo = (Map<String, Object>) session.getAttribute("recharge_" + outTradeNo);
            if (rechargeInfo != null && "PENDING".equals(rechargeInfo.get("status"))) {
                Long userId = (Long) rechargeInfo.get("userId");
                BigDecimal amount = new BigDecimal(totalAmount);

                // 执行充值
                if (userService.recharge(userId, amount)) {
                    rechargeInfo.put("status", "SUCCESS");
                    session.setAttribute("recharge_" + outTradeNo, rechargeInfo);

                    // 更新session中的用户信息
                    User user = userService.getUserById(userId);
                    if (user != null) {
                        session.setAttribute("currentUser", user);
                    }

                    log.info("用户充值成功: userId={}, amount={}", userId, amount);
                }
            }
        }
    }

    /**
     * 处理订单支付结果
     */
    private void processOrderPaymentResult(Map<String, String> params) {
        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        String totalAmount = params.get("total_amount");
        String tradeNo = params.get("trade_no");

        log.info("处理订单支付结果 - 订单号: {}, 状态: {}, 金额: {}", outTradeNo, tradeStatus, totalAmount);

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // 更新订单状态为已支付
            Order order = orderService.getOrderByOrderNo(outTradeNo);
            if (order != null && order.getStatus() == 0) {
                // 这里应该调用一个新的方法来更新订单支付状态，而不是直接扣余额
                orderService.updateOrderPayStatus(order.getId(), tradeNo, "alipay");
                log.info("订单支付成功: {}", outTradeNo);
            }
        }
    }
}