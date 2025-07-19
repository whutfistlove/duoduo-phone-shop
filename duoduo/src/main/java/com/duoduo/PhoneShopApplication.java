package com.duoduo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 多多购手机网站主应用程序
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@SpringBootApplication
@MapperScan("com.duoduo.phoneshop.mapper")
public class PhoneShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhoneShopApplication.class, args);
        System.out.println("========================================");
        System.out.println("   多多购手机网站启动成功！");
        System.out.println("   访问地址: http://localhost:8080");
        System.out.println("========================================");
    }
}