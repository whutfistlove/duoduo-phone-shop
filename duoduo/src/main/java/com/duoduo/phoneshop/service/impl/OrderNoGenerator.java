package com.duoduo.phoneshop.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 订单号生成器
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
public class OrderNoGenerator {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    /**
     * 生成订单号
     * 格式：时间戳(14位) + 随机数(6位)
     *
     * @return 订单号
     */
    public static String generate() {
        // 时间戳部分
        String timestamp = DATE_FORMAT.format(new Date());

        // 随机数部分
        StringBuilder randomPart = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            randomPart.append(RANDOM.nextInt(10));
        }

        return timestamp + randomPart.toString();
    }

    /**
     * 生成带前缀的订单号
     *
     * @param prefix 前缀
     * @return 订单号
     */
    public static String generate(String prefix) {
        return prefix + generate();
    }
}