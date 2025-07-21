package com.duoduo.phoneshop.controller;

import com.alipay.api.AlipayApiException;
import com.duoduo.phoneshop.entity.Order;
import com.duoduo.phoneshop.entity.User;
import com.duoduo.phoneshop.entity.RechargeOrder;
import com.duoduo.phoneshop.mapper.RechargeOrderMapper;
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

    @Autowired
    private RechargeOrderMapper rechargeOrderMapper;

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
            // 生成订单号 - 确保添加了RECHARGE_前缀
            String outTradeNo = alipayService.generateTradeNo("RECHARGE");

            log.info("生成充值订单号: {}", outTradeNo);

            // 创建充值订单并保存到数据库
            RechargeOrder rechargeOrder = new RechargeOrder();
            rechargeOrder.setOrderNo(outTradeNo);
            rechargeOrder.setUserId(currentUser.getId());
            rechargeOrder.setAmount(amount);
            rechargeOrder.setStatus(0); // 待支付

            // 保存到数据库
            rechargeOrderMapper.insert(rechargeOrder);

            log.info("创建充值订单成功: orderNo={}, userId={}, amount={}",
                    outTradeNo, currentUser.getId(), amount);

            // 创建支付宝支付
            String subject = "多多手机商城-账户充值";
            String form = alipayService.createRechargePayment(outTradeNo, amount, subject);

            result.put("success", true);
            result.put("form", form);
            result.put("outTradeNo", outTradeNo);

        } catch (Exception e) {
            log.error("创建充值订单失败", e);
            result.put("success", false);
            result.put("message", "创建支付订单失败，请重试");
        }

        return result;
    }

    /**
     * 支付宝异步通知 - 加强版
     */
    @RequestMapping(value = "/notify", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String notifyCallback(HttpServletRequest request) {
        log.info("========== 支付宝异步通知开始 ==========");

        String method = request.getMethod();
        log.info("请求方式: {}", method);

        // 获取所有参数
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

        try {
            // 验证签名
            boolean signVerified = alipayService.verifySignature(params);
            log.info("签名验证结果: {}", signVerified);

            if (signVerified) {
                // 获取关键参数
                String outTradeNo = params.get("out_trade_no");
                String tradeStatus = params.get("trade_status");
                String tradeNo = params.get("trade_no");
                String totalAmount = params.get("total_amount");

                log.info("订单号: {}, 交易状态: {}, 支付宝交易号: {}, 金额: {}",
                        outTradeNo, tradeStatus, tradeNo, totalAmount);

                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    log.info("支付成功，开始处理业务逻辑");

                    // 判断是充值还是订单支付
                    if (outTradeNo != null && outTradeNo.startsWith("RECHARGE_")) {
                        log.info("处理充值订单");
                        processRechargeResult(outTradeNo, tradeNo, totalAmount);
                    } else if (outTradeNo != null && outTradeNo.startsWith("ORDER_")) {
                        log.info("处理订单支付");
                        processOrderPaymentResult(params);
                    } else {
                        log.warn("未知的订单类型: {}", outTradeNo);
                    }
                } else {
                    log.warn("交易状态不是成功状态: {}", tradeStatus);
                }

                log.info("异步通知处理完成，返回success");
                return "success";
            } else {
                log.error("签名验证失败！");
                return "fail";
            }
        } catch (Exception e) {
            log.error("处理异步通知异常", e);
            return "fail";
        } finally {
            log.info("========== 支付宝异步通知结束 ==========");
        }
    }

    /**
     * 处理充值结果 - 增强版
     */
    private void processRechargeResult(String outTradeNo, String tradeNo, String totalAmount) {
        log.info("开始处理充值结果 - 订单号: {}, 支付宝交易号: {}, 金额: {}",
                outTradeNo, tradeNo, totalAmount);

        try {
            // 1. 查询充值订单
            RechargeOrder rechargeOrder = rechargeOrderMapper.selectByOrderNo(outTradeNo);
            if (rechargeOrder == null) {
                log.error("充值订单不存在: {}", outTradeNo);
                return;
            }
            log.info("查询到充值订单: userId={}, amount={}, status={}",
                    rechargeOrder.getUserId(), rechargeOrder.getAmount(), rechargeOrder.getStatus());

            // 2. 检查订单状态，避免重复处理
            if (rechargeOrder.getStatus() != 0) {
                log.warn("充值订单已处理，跳过: orderNo={}, status={}",
                        outTradeNo, rechargeOrder.getStatus());
                return;
            }

            // 3. 验证金额
            BigDecimal orderAmount = rechargeOrder.getAmount();
            BigDecimal payAmount = new BigDecimal(totalAmount);
            if (orderAmount.compareTo(payAmount) != 0) {
                log.error("支付金额不匹配！订单金额: {}, 支付金额: {}", orderAmount, payAmount);
                return;
            }

            // 4. 更新充值订单状态
            log.info("更新充值订单状态为已支付");
            int updateResult = rechargeOrderMapper.updateStatus(outTradeNo, 1, tradeNo);
            log.info("更新充值订单结果: {}", updateResult);

            // 5. 查询用户当前余额
            User userBefore = userService.getUserById(rechargeOrder.getUserId());
            log.info("充值前用户余额: userId={}, balance={}",
                    userBefore.getId(), userBefore.getBalance());

            // 6. 执行用户账户充值
            boolean rechargeSuccess = userService.recharge(rechargeOrder.getUserId(), payAmount);
            log.info("用户充值结果: {}", rechargeSuccess);

            if (rechargeSuccess) {
                // 7. 再次查询确认余额
                User userAfter = userService.getUserById(rechargeOrder.getUserId());
                log.info("充值后用户余额: userId={}, balance={}",
                        userAfter.getId(), userAfter.getBalance());

                log.info("用户充值成功: userId={}, amount={}, 新余额={}",
                        rechargeOrder.getUserId(), payAmount, userAfter.getBalance());
            } else {
                log.error("用户充值失败: userId={}, amount={}",
                        rechargeOrder.getUserId(), payAmount);
                // 充值失败，更新订单状态为失败
                rechargeOrderMapper.updateStatus(outTradeNo, 2, tradeNo);
            }

        } catch (Exception e) {
            log.error("处理充值结果异常", e);
            // 发生异常，尝试更新订单状态为失败
            try {
                rechargeOrderMapper.updateStatus(outTradeNo, 2, tradeNo);
            } catch (Exception ex) {
                log.error("更新订单状态失败", ex);
            }
        }
    }

    /**
     * 支付宝同步回调（页面跳转）
     */
    @GetMapping("/return")
    public String returnCallback(HttpServletRequest request, Model model, HttpSession session) {
        log.info("========== 支付宝同步回调开始 ==========");

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
            } else if (outTradeNo.startsWith("ORDER_")) {
                model.addAttribute("type", "order");
            }

            log.info("同步回调处理成功");
        } else {
            model.addAttribute("success", false);
            model.addAttribute("message", "签名验证失败");
            log.error("同步回调签名验证失败");
        }

        log.info("========== 支付宝同步回调结束 ==========");
        return "user/pay-result";
    }

    /**
     * 处理订单支付结果
     */
    private void processOrderPaymentResult(Map<String, String> params) {
        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        String totalAmount = params.get("total_amount");
        String tradeNo = params.get("trade_no");

        log.info("处理订单支付结果 - 订单号: {}, 状态: {}, 金额: {}",
                outTradeNo, tradeStatus, totalAmount);

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // 更新订单状态为已支付
            Order order = orderService.getOrderByOrderNo(outTradeNo);
            if (order != null && order.getStatus() == 0) {
                // 调用订单支付状态更新方法
                orderService.updateOrderPayStatus(order.getId(), tradeNo, "alipay");
                log.info("订单支付成功: {}", outTradeNo);
            }
        }
    }
}