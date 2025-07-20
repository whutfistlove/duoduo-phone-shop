package com.duoduo.phoneshop.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.duoduo.phoneshop.config.AlipayConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 支付宝支付服务
 *
 * @author DuoDuo
 * @date 2025/01/21
 */
@Slf4j
@Service
public class AlipayService {

    @Autowired
    private AlipayConfig alipayConfig;

    private AlipayClient alipayClient;

    /**
     * 获取支付宝客户端
     */
    private AlipayClient getAlipayClient() {
        if (alipayClient == null) {
            // 添加空值检查（强烈推荐）
            if (StringUtils.isBlank(alipayConfig.getPrivateKey())) {
                throw new IllegalStateException("支付宝私钥未配置");
            }

            alipayClient = new DefaultAlipayClient(
                    alipayConfig.getGatewayUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(), // 修改为 getPrivateKey()
                    "json",
                    alipayConfig.getCharset(),
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getSignType()
            );
        }
        return alipayClient;
    }

    /**
     * 创建支付宝充值订单
     *
     * @param outTradeNo 商户订单号
     * @param amount     充值金额
     * @param subject    订单标题
     * @return 支付表单HTML
     */
    public String createRechargePayment(String outTradeNo, BigDecimal amount, String subject) throws AlipayApiException {
        // 创建API对应的request
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

        // 设置同步回调地址
        request.setReturnUrl(alipayConfig.getReturnUrl());
        // 设置异步通知地址
        request.setNotifyUrl(alipayConfig.getNotifyUrl());

        // 构建请求参数
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(outTradeNo);
        model.setSubject(subject);
        model.setTotalAmount(amount.toString());
        model.setBody("多多手机商城账户充值");
        model.setProductCode("FAST_INSTANT_TRADE_PAY");

        // 设置超时时间
        model.setTimeoutExpress("30m");

        request.setBizModel(model);

        try {
            // 调用SDK生成表单
            AlipayClient client = getAlipayClient();
            AlipayTradePagePayResponse response = client.pageExecute(request);
            if (response.isSuccess()) {
                log.info("支付宝充值订单创建成功: {}", outTradeNo);
                return response.getBody();
            } else {
                log.error("支付宝充值订单创建失败: {}", response.getMsg());
                throw new RuntimeException("创建支付订单失败");
            }
        } catch (AlipayApiException e) {
            log.error("创建支付订单异常", e);
            throw e;
        }
    }

    /**
     * 创建支付宝订单支付
     *
     * @param orderNo     订单号
     * @param amount      支付金额
     * @param subject     订单标题
     * @param body        订单描述
     * @return 支付表单HTML
     */
    public String createOrderPayment(String orderNo, BigDecimal amount, String subject, String body) throws AlipayApiException {
        // 创建API对应的request
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

        // 设置同步回调地址（订单支付成功后跳转）
        request.setReturnUrl(alipayConfig.getReturnUrl() + "?type=order");
        // 设置异步通知地址
        request.setNotifyUrl(alipayConfig.getNotifyUrl());

        // 构建请求参数
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(orderNo);
        model.setSubject(subject);
        model.setTotalAmount(amount.toString());
        model.setBody(body);
        model.setProductCode("FAST_INSTANT_TRADE_PAY");

        // 设置超时时间
        model.setTimeoutExpress("30m");

        request.setBizModel(model);

        try {
            // 调用SDK生成表单
            AlipayClient client = getAlipayClient();
            AlipayTradePagePayResponse response = client.pageExecute(request);
            if (response.isSuccess()) {
                log.info("支付宝订单支付创建成功: {}", orderNo);
                return response.getBody();
            } else {
                log.error("支付宝订单支付创建失败: {}", response.getMsg());
                throw new RuntimeException("创建支付订单失败");
            }
        } catch (AlipayApiException e) {
            log.error("创建支付订单异常", e);
            throw e;
        }
    }

    /**
     * 验证回调签名
     *
     * @param params 回调参数
     * @return 验证结果
     */
    public boolean verifySignature(Map<String, String> params) {
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );

            if (signVerified) {
                log.info("支付宝回调签名验证成功");
                return true;
            } else {
                log.error("支付宝回调签名验证失败");
                return false;
            }
        } catch (AlipayApiException e) {
            log.error("验证签名异常", e);
            return false;
        }
    }

    /**
     * 查询订单状态
     *
     * @param outTradeNo 商户订单号
     * @return 查询结果
     */
    public AlipayTradeQueryResponse queryOrder(String outTradeNo) throws AlipayApiException {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{\"out_trade_no\":\"" + outTradeNo + "\"}");

        try {
            AlipayClient client = getAlipayClient();
            AlipayTradeQueryResponse response = client.execute(request);
            if (response.isSuccess()) {
                log.info("订单查询成功: {}, 状态: {}", outTradeNo, response.getTradeStatus());
                return response;
            } else {
                log.error("订单查询失败: {}", response.getMsg());
                return null;
            }
        } catch (AlipayApiException e) {
            log.error("查询订单异常", e);
            throw e;
        }
    }

    /**
     * 生成订单号
     *
     * @param prefix 前缀（如：RECHARGE、ORDER）
     * @return 订单号
     */
    public String generateTradeNo(String prefix) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        int random = (int) ((Math.random() * 9 + 1) * 100000);
        return prefix + "_" + timestamp + random;
    }
}